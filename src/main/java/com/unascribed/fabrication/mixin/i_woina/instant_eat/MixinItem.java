package com.unascribed.fabrication.mixin.i_woina.instant_eat;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Mixin(Item.class)
@EligibleIf(configAvailable="*.instant_eat")
public class MixinItem {

	@Inject(method="getMaxUseTime(Lnet/minecraft/item/ItemStack;)I", at=@At(value="HEAD"), cancellable = true)
	private void getMaxUseTime(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (FabConf.isEnabled("*.instant_eat") && stack.getItem().isFood()) cir.setReturnValue(1);
	}
}
