package com.unascribed.fabrication.mixin.g_weird_tweaks.blaze_fertilizer;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.BlazeFertilizerDispencerBehavior;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DispenserBlock.class)
@EligibleIf(configAvailable="*.blaze_fertilizer")
public class MixinDispenserBlock {

	@FabInject(at=@At("HEAD"), method="getBehaviorForItem(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/block/dispenser/DispenserBehavior;", cancellable=true)
	public void getBehaviorForItem(ItemStack stack, CallbackInfoReturnable<DispenserBehavior> ci) {
		if (!FabConf.isEnabled("*.blaze_fertilizer")) return;
		if (stack.getItem() == Items.BLAZE_POWDER) {
			ci.setReturnValue(BlazeFertilizerDispencerBehavior.INSTANCE);
		}
	}

}
