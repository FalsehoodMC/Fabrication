package com.unascribed.fabrication.mixin.b_utility.extract_furnace_xp;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ScreenHandler.class)
@EligibleIf(configAvailable="*.extract_furnace_xp")
public class MixinScreenHandler {

	@Inject(at=@At("HEAD"), method="onSlotClick(IILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V")
	public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.extract_furnace_xp")) return;
		Object self = this;
		if (self instanceof AbstractFurnaceScreenHandler) {
			AbstractFurnaceScreenHandler afsh = (AbstractFurnaceScreenHandler)self;
			if (slotIndex == 2 && (actionType == SlotActionType.PICKUP || actionType == SlotActionType.QUICK_MOVE)) {
				Slot s = afsh.getSlot(slotIndex);
				if (s != null && !s.hasStack() && s.inventory instanceof AbstractFurnaceBlockEntity && player instanceof ServerPlayerEntity) {
					((AbstractFurnaceBlockEntity)s.inventory).dropExperienceForRecipesUsed((ServerPlayerEntity)player);
				}
			}
		}
	}

}
