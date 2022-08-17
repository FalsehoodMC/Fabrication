package com.unascribed.fabrication.mixin.f_balance.no_filled_inventories_in_shulkers;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.ShulkerBoxSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleItem.class)
@EligibleIf(configAvailable="*.no_filled_inventories_in_shulkers")
public class MixinBundleItem {


	@FabInject(at=@At("HEAD"), cancellable=true,
			method="onClicked(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/screen/slot/Slot;Lnet/minecraft/util/ClickType;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/inventory/StackReference;)Z")
	public void onStackClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference, CallbackInfoReturnable<Boolean> cir) {
		if (!FabConf.isEnabled("*.no_filled_inventories_in_shulkers")) return;

		if (slot instanceof ShulkerBoxSlot) {
			cir.setReturnValue(false);
			cir.cancel();
		}
	}

}
