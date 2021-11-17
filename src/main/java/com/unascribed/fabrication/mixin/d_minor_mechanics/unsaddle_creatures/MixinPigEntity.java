package com.unascribed.fabrication.mixin.d_minor_mechanics.unsaddle_creatures;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.entity.Saddleable;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

@Mixin(PigEntity.class)
@EligibleIf(configEnabled="*.unsaddle_creatures")
public abstract class MixinPigEntity implements Saddleable {
	@Shadow
	private SaddledComponent saddledComponent;

	@Inject(method = "interactMob(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;",
			at = @At("HEAD"), cancellable = true)
	public void interactMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		if (MixinConfigPlugin.isEnabled("*.unsaddle_creatures") && this.isSaddled() && player.isSneaky() && player.getStackInHand(hand).isEmpty()) {
			saddledComponent.setSaddled(false);
			player.setStackInHand(hand, Items.SADDLE.getDefaultStack());
			cir.setReturnValue(ActionResult.SUCCESS);
		}
	}
}
