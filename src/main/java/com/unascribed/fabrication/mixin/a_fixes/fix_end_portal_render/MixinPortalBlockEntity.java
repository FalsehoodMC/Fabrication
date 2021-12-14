package com.unascribed.fabrication.mixin.a_fixes.fix_end_portal_render;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndPortalBlockEntity.class)
@EligibleIf(configAvailable="*.fix_end_portal_render", envMatches=Env.CLIENT)
public abstract class MixinPortalBlockEntity extends BlockEntity {

	public MixinPortalBlockEntity(BlockEntityType<?> type) {
		super(type);
	}

	@Inject(at=@At("HEAD"), method="shouldDrawSide(Lnet/minecraft/util/math/Direction;)Z", cancellable=true)
	public void renderAllSides(Direction direction, CallbackInfoReturnable<Boolean> cir) {
		if (MixinConfigPlugin.isEnabled("*.fix_end_portal_render"))
			cir.setReturnValue(Block.shouldDrawSide(this.getCachedState(), this.world, this.getPos(), direction));
	}

}
