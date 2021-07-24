package com.unascribed.fabrication.mixin.i_woina.instant_eat;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
@EligibleIf(configEnabled="*.instant_eat", envMatches=Env.CLIENT)
public class MixinItem {

	@Inject(method="getMaxUseTime(Lnet/minecraft/item/ItemStack;)I", at=@At(value="HEAD"), cancellable = true)
	private void getMaxUseTime(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (MixinConfigPlugin.isEnabled("*.instant_eat") && stack.getItem().isFood()) cir.setReturnValue(1);
	}
}
