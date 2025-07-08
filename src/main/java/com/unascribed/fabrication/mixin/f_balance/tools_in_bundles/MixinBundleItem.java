package com.unascribed.fabrication.mixin.f_balance.tools_in_bundles;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.BundleHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleItem.class)
@EligibleIf(configAvailable="*.tools_in_bundles")
public class MixinBundleItem {


	@FabInject(at=@At(value="INVOKE", target="Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;"), cancellable=true,
			method="onStackClicked(Lnet/minecraft/item/ItemStack;Lnet/minecraft/screen/slot/Slot;Lnet/minecraft/util/ClickType;Lnet/minecraft/entity/player/PlayerEntity;)Z")
	public void onStackClicked(ItemStack bundle, Slot slot, ClickType clickType, PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
		if (FabConf.isEnabled("*.tools_in_bundles")) {
			ItemStack stack = slot.getStack();
			if (!BundleHelper.isCompatible(bundle, stack)) {
				cir.setReturnValue(false);
			}
		}
	}

	@FabInject(at=@At(value="INVOKE", target="Lnet/minecraft/component/type/BundleContentsComponent$Builder;add(Lnet/minecraft/item/ItemStack;)I"), cancellable=true,
		method="onClicked(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/screen/slot/Slot;Lnet/minecraft/util/ClickType;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/inventory/StackReference;)Z")
	public void onClicked(ItemStack bundle, ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference, CallbackInfoReturnable<Boolean> cir) {
		if (FabConf.isEnabled("*.tools_in_bundles")) {
			if (!BundleHelper.isCompatible(bundle, stack)) {
				cir.setReturnValue(false);
			}
		}
	}

}
