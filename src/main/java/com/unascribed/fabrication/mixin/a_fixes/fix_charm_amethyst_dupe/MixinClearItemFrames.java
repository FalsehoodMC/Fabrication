package com.unascribed.fabrication.mixin.a_fixes.fix_charm_amethyst_dupe;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.interfaces.SetInvisibleByCharm;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import svenhjol.charm.module.clear_item_frames.ClearItemFrames;

@Mixin(value=ClearItemFrames.class)
@EligibleIf(configAvailable="*.fix_charm_amethyst_dupe", modLoaded="charm")
public class MixinClearItemFrames {

	private boolean fabrication$wasInvisible;

	@Inject(at=@At("HEAD"), method="handleUseEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/util/Hand;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/hit/EntityHitResult;)Lnet/minecraft/util/ActionResult;")
	private void handleUseEntityHead(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult, CallbackInfoReturnable<ActionResult> ci) {
		fabrication$wasInvisible = entity.isInvisible();
	}

	@Inject(at=@At("RETURN"), method="handleUseEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/util/Hand;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/hit/EntityHitResult;)Lnet/minecraft/util/ActionResult;")
	private void handleUseEntityTail(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult, CallbackInfoReturnable<ActionResult> ci) {
		if (entity.isInvisible() && !fabrication$wasInvisible) {
			((SetInvisibleByCharm)entity).fabrication$setInvisibleByCharm(true);
		}
	}

	@Inject(at=@At("HEAD"), method="handleAttackEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/util/Hand;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/hit/EntityHitResult;)Lnet/minecraft/util/ActionResult;",
			cancellable=true)
	public void handleAttackEntity(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult, CallbackInfoReturnable<ActionResult> ci) {
		if (!((SetInvisibleByCharm)entity).fabrication$isInvisibleByCharm()) {
			ci.setReturnValue(ActionResult.PASS);
		}
	}

}
