package com.unascribed.fabrication.mixin.c_tweaks.recipe_book_auto_craft;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

@Mixin(InventoryScreen.class)
@EligibleIf(configAvailable="*.recipe_book_auto_craft", envMatches=Env.CLIENT)
public abstract class MixinInventoryScreen extends AbstractInventoryScreen<PlayerScreenHandler> {

	public MixinInventoryScreen(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
		super(screenHandler, playerInventory, text);
	}

	@Override
	@Shadow protected abstract void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType);

	@Inject(method="mouseClicked(DDI)Z", at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;setFocused(Lnet/minecraft/client/gui/Element;)V"))
	private void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir){
		if (FabConf.isEnabled("*.recipe_book_auto_craft") && button == 0)
			onMouseClick(handler.getSlot(handler.getCraftingResultSlotIndex()), handler.getCraftingResultSlotIndex(), 0, hasShiftDown() ? SlotActionType.QUICK_MOVE : SlotActionType.PICKUP);
	}
}
