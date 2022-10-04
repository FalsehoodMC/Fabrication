package com.unascribed.fabrication.mixin.a_fixes.fix_charm_amethyst_dupe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
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

	@FabInject(at=@At("HEAD"), method="handleUseEntity(Lnet/minecraft/class_1657;Lnet/minecraft/class_1937;Lnet/minecraft/class_1268;Lnet/minecraft/class_1297;Lnet/minecraft/class_3966;)Lnet/minecraft/class_1269;")
	private void handleUseEntityHead(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult, CallbackInfoReturnable<ActionResult> ci) {
		fabrication$wasInvisible = entity.isInvisible();
	}

	@FabInject(at=@At("RETURN"), method="handleUseEntity(Lnet/minecraft/class_1657;Lnet/minecraft/class_1937;Lnet/minecraft/class_1268;Lnet/minecraft/class_1297;Lnet/minecraft/class_3966;)Lnet/minecraft/class_1269;")
	private void handleUseEntityTail(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult, CallbackInfoReturnable<ActionResult> ci) {
		if (entity.isInvisible() && !fabrication$wasInvisible) {
			((SetInvisibleByCharm)entity).fabrication$setInvisibleByCharm(true);
		}
	}

	@FabInject(at=@At("HEAD"), method="handleAttackEntity(Lnet/minecraft/class_1657;Lnet/minecraft/class_1937;Lnet/minecraft/class_1268;Lnet/minecraft/class_1297;Lnet/minecraft/class_3966;)Lnet/minecraft/class_1269;",
			cancellable=true)
	public void handleAttackEntity(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult, CallbackInfoReturnable<ActionResult> ci) {
		if (!((SetInvisibleByCharm)entity).fabrication$isInvisibleByCharm()) {
			ci.setReturnValue(ActionResult.PASS);
		}
	}

}
