package com.unascribed.fabrication.mixin.f_balance.disable_prior_work_penalty;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.function.Predicate;

@Mixin(GrindstoneScreenHandler.class)
@EligibleIf(anyConfigAvailable={"*.disable_prior_work_penalty", "*.anvil_no_xp_cost"})
public class MixinGrindstoneScreenHandler {

	private static final Predicate<ItemStack> fabrication$disablePriorWorkPenalty = ConfigPredicates.getFinalPredicate("*.disable_prior_work_penalty");
	@ModifyArgs(method="grind(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", at=@At(value="INVOKE", target="Lnet/minecraft/item/ItemStack;set(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
	public void grind(Args args, ItemStack stack) {
		if (!DataComponentTypes.REPAIR_COST.equals(args.get(0))) return;
		if (!(
				FabConf.isEnabled("*.disable_prior_work_penalty") && fabrication$disablePriorWorkPenalty.test(stack)
				|| FabConf.isEnabled("*.anvil_no_xp_cost")
		)) return;
		args.set(1, 0);
	}

}
