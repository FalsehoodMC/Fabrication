package com.unascribed.fabrication.mixin.c_tweaks.no_sneak_bypass;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Entity.class)
@EligibleIf(configAvailable="*.no_sneak_bypass")
public abstract class MixinEntity {

	@Shadow
	public abstract boolean bypassesSteppingEffects();

	@Shadow
	public World world;

	@Inject(at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;bypassesSteppingEffects()Z"),
			method="move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V", locals=LocalCapture.CAPTURE_FAILHARD)
	public void dontBypassSteppingEffects(MovementType movementType, Vec3d movement, CallbackInfo ci, Vec3d vec, BlockPos pos, BlockState state, Vec3d vec2, Block block) {
		if (!(MixinConfigPlugin.isEnabled("*.no_sneak_bypass") && ConfigPredicates.shouldRun("*.no_sneak_bypass", (Entity)(Object)this))) return;
		if (this.bypassesSteppingEffects()) block.onSteppedOn(this.world, pos, state, ((Entity)(Object)this));
	}
}
