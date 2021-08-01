package com.unascribed.fabrication.mixin.h_situational.disable_elytra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;

@Mixin(ElytraItem.class)
@EligibleIf(configAvailable="*.disable_elytra")
public class MixinElytraItem {

	@Inject(at=@At("HEAD"), method="isUsable(Lnet/minecraft/item/ItemStack;)Z", cancellable=true)
	private static void isUsable(ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
		if (MixinConfigPlugin.isEnabled("*.disable_elytra")) {
			ci.setReturnValue(false);
		}
	}
	
}
