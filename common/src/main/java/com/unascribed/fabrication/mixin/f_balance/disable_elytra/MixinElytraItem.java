package com.unascribed.fabrication.mixin.f_balance.disable_elytra;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(ElytraItem.class)
@EligibleIf(configAvailable="*.disable_elytra")
public class MixinElytraItem {

	private static final Predicate<ItemStack> fabrication$disableElytraPredicate = ConfigPredicates.getFinalPredicate("*.disable_elytra");
	@FabInject(at=@At("HEAD"), method="isUsable(Lnet/minecraft/item/ItemStack;)Z", cancellable=true)
	private static void isUsable(ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
		if (FabConf.isEnabled("*.disable_elytra") && fabrication$disableElytraPredicate.test(stack)) {
			ci.setReturnValue(false);
		}
	}

}
