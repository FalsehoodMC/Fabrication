package com.unascribed.fabrication.mixin.e_mechanics.grindstone_disenchanting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;

@Mixin(targets="net.minecraft.screen.GrindstoneScreenHandler$3")
@EligibleIf(configEnabled="*.grindstone_disenchanting")
public class MixinGrindstoneScreenHandlerInputSlot2 extends Slot {

	public MixinGrindstoneScreenHandlerInputSlot2(Inventory inventory, int index, int x, int y) {
		super(inventory, index, x, y);
	}

	@Inject(at=@At("HEAD"), method="canInsert(Lnet/minecraft/item/ItemStack;)Z", cancellable=true, expect=1)
	public void canInsert(ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
		if (MixinConfigPlugin.isEnabled("*.grindstone_disenchanting") && stack.getItem() == Items.BOOK) {
			ci.setReturnValue(true);
		}
	}
	
}
