package com.unascribed.fabrication.mixin.f_balance.disable_prior_work_penalty;

import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.support.injection.HijackReturn;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

@Mixin(AnvilScreenHandler.class)
@EligibleIf(anyConfigAvailable={"*.disable_prior_work_penalty", "*.anvil_no_xp_cost"})
public class MixinAnvilScreenHandler {

	private static final Predicate<ItemStack> fabrication$disablePriorWorkPenalty = ConfigPredicates.getFinalPredicate("*.disable_prior_work_penalty");
	@Hijack(method="updateResult()V", target="Lnet/minecraft/item/ItemStack;set(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;")
	public HijackReturn updateResult(ItemStack stack, ComponentType type) {
		if (!DataComponentTypes.REPAIR_COST.equals(type)) return null;
		if (!(
				FabConf.isEnabled("*.disable_prior_work_penalty") && fabrication$disablePriorWorkPenalty.test(stack)
				|| FabConf.isEnabled("*.anvil_no_xp_cost")
		)) return null;
		return new HijackReturn(stack.set(DataComponentTypes.REPAIR_COST, 0));
	}

}
