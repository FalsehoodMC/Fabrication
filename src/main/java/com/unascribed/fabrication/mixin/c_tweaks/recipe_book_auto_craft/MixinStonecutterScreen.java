package com.unascribed.fabrication.mixin.c_tweaks.recipe_book_auto_craft;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.FabRefl;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.StonecutterScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StonecutterScreen.class)
@EligibleIf(configAvailable="*.recipe_book_auto_craft", envMatches=Env.CLIENT)
public abstract class MixinStonecutterScreen extends HandledScreen<StonecutterScreenHandler> {

	public MixinStonecutterScreen(StonecutterScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
	}

	@FabInject(method="mouseClicked(DDI)Z", at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerInteractionManager;clickButton(II)V", shift=At.Shift.AFTER))
	private void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir){
		if (FabConf.isEnabled("*.recipe_book_auto_craft") && (button == 0 || button == 1) && !hasControlDown()) {
			if (hasShiftDown()) {
				onMouseClick(handler.getSlot(1), 1, 0, SlotActionType.QUICK_MOVE);
				return;
			}
			ItemStack stack = handler.getSlot(1).getStack();
			onMouseClick(handler.getSlot(1), 1, 0, SlotActionType.PICKUP);
			PlayerInventory pInv = MinecraftClient.getInstance().player.inventory;

			int count = stack.getCount();
			for(int i = 0; i < pInv.main.size(); ++i) {
				ItemStack insStack = pInv.main.get(i);
				if (stack.getItem() == insStack.getItem() && (stack.getTag() == insStack.getTag() || (stack.getTag() != null && stack.getTag().equals(insStack.getTag())))) {
					int oi = fabricationCutterIndex(pInv, i);
					if (oi == -1) continue;
					onMouseClick(handler.getSlot(oi), i, 0, SlotActionType.PICKUP);
					if (count - (insStack.getMaxCount()-insStack.getCount()) <= 0) {
						return;
					}
				}
			}
			for(int i = 0; i < pInv.main.size(); ++i) {
				ItemStack insStack = pInv.main.get(i);
				if (insStack.isEmpty()) {
					int oi = fabricationCutterIndex(pInv, i);
					if (oi == -1) continue;
					onMouseClick(handler.getSlot(oi), i, 0, SlotActionType.PICKUP);
					return;
				}
			}
		}
	}
	private int fabricationCutterIndex(Inventory inventory, int index) {
		for(int i = 0; i < handler.slots.size(); ++i) {
			Slot slot = handler.slots.get(i);
			if (slot.inventory == inventory && index == FabRefl.getSlotIndex(slot)) {
				return i;
			}
		}
		return -1;
	}
}
