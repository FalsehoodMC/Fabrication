package com.unascribed.fabrication.mixin.f_balance.tools_in_bundles;

import java.util.stream.Stream;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;

@Mixin(BundleItem.class)
@EligibleIf(configAvailable="*.tools_in_bundles")
public class MixinBundleItem {

	@Inject(at=@At(value="INVOKE_ASSIGN", target="net/minecraft/item/BundleItem.getItemOccupancy(Lnet/minecraft/item/ItemStack;)I"),
			method="onStackClicked(Lnet/minecraft/item/ItemStack;Lnet/minecraft/screen/slot/Slot;Lnet/minecraft/util/ClickType;Lnet/minecraft/entity/player/PlayerEntity;)Z", cancellable=true)
	public void onStackClicked(ItemStack bundle, Slot slot, ClickType clickType, PlayerEntity player, CallbackInfoReturnable<Boolean> ci) {
		// takeStackRange gets called without checking the return value of addToBundle
		// this results in item deletion for a bundle containing tools if not prevented here
		if (FabConf.isEnabled("*.tools_in_bundles")) {
			ItemStack stack = slot.getStack();
			if (!fabrication$isCompatible(bundle, stack)) {
				ci.setReturnValue(true);
			}
		}
	}

	@Inject(at=@At("HEAD"), method="net/minecraft/item/BundleItem.getItemOccupancy(Lnet/minecraft/item/ItemStack;)I", cancellable=true)
	private static void getItemOccupancy(ItemStack stack, CallbackInfoReturnable<Integer> ci) {
		if (FabConf.isEnabled("*.tools_in_bundles") && stack.getMaxCount() == 1)
			ci.setReturnValue(8);
	}

	@Inject(at=@At("HEAD"), method="addToBundle(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)I", cancellable=true)
	private static void addToBundle(ItemStack bundle, ItemStack stack, CallbackInfoReturnable<Integer> ci) {
		if (FabConf.isEnabled("*.tools_in_bundles") && !stack.isEmpty()) {
			if (!fabrication$isCompatible(bundle, stack)) {
				ci.setReturnValue(0);
			}
		}
	}

	private static boolean fabrication$isCompatible(ItemStack bundle, ItemStack stack) {
		boolean empty = getBundledStacks(bundle).findFirst().isEmpty();
		if (empty) return true;
		boolean containsTool = getBundledStacks(bundle).anyMatch(is -> is.getMaxCount() == 1);
		boolean isTool = stack.getMaxCount() == 1;
		return containsTool == isTool;
	}

	@Shadow
	private static Stream<ItemStack> getBundledStacks(ItemStack stack) { throw new AbstractMethodError(); }

}
