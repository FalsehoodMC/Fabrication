package com.unascribed.fabrication.mixin.b_utility.extract_furnace_xp;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenHandler.class)
@EligibleIf(configAvailable="*.extract_furnace_xp")
public class MixinScreenHandler {

	@FabInject(at=@At("HEAD"), method="onSlotClick(IILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/item/ItemStack;")
	public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfoReturnable<ItemStack> cir) {
		if (!FabConf.isEnabled("*.extract_furnace_xp")) return;
		Object self = this;
		if (self instanceof AbstractFurnaceScreenHandler) {
			AbstractFurnaceScreenHandler afsh = (AbstractFurnaceScreenHandler)self;
			if (slotIndex == 2 && (actionType == SlotActionType.PICKUP || actionType == SlotActionType.QUICK_MOVE)) {
				Slot s = afsh.getSlot(slotIndex);
				if (s != null && !s.hasStack() && s.inventory instanceof AbstractFurnaceBlockEntity && player instanceof ServerPlayerEntity) {
					((AbstractFurnaceBlockEntity)s.inventory).dropExperience(player);
				}
			}
		}
	}

}
