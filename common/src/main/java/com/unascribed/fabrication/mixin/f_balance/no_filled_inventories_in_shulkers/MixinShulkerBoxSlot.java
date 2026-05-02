package com.unascribed.fabrication.mixin.f_balance.no_filled_inventories_in_shulkers;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.ItemNbtScanner;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.ShulkerBoxSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxSlot.class)
@EligibleIf(configAvailable="*.no_filled_inventories_in_shulkers")
public class MixinShulkerBoxSlot {

	@FabInject(method="canInsert(Lnet/minecraft/item/ItemStack;)Z", at=@At("HEAD"), cancellable=true)
	private void preventClosingScreen(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (!FabConf.isEnabled("*.no_filled_inventories_in_shulkers")) return;
		if (ItemNbtScanner.hasItemInvNBT(stack)) {
			cir.setReturnValue(false);
			cir.cancel();
			return;
		}
		if ((Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock)) {
			cir.setReturnValue(true);
			cir.cancel();
		}
	}
}
