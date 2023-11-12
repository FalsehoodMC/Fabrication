package com.unascribed.fabrication.mixin.c_tweaks.recipe_book_auto_craft;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.StonecutterScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

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
			PlayerInventory pInv = MinecraftClient.getInstance().player.getInventory();

			int count = stack.getCount();
			for(int i = 0; i < pInv.main.size(); ++i) {
				ItemStack insStack = pInv.main.get(i);
				if (ItemStack.canCombine(stack, insStack)) {
					OptionalInt oi = handler.getSlotIndex(pInv, i);
					if (!oi.isPresent()) continue;
					onMouseClick(handler.getSlot(oi.getAsInt()), i, 0, SlotActionType.PICKUP);
					if (count - (insStack.getMaxCount()-insStack.getCount()) <= 0) {
						return;
					}
				}
			}
			for(int i = 0; i < pInv.main.size(); ++i) {
				ItemStack insStack = pInv.main.get(i);
				if (insStack.isEmpty()) {
					OptionalInt oi = handler.getSlotIndex(pInv, i);
					if (!oi.isPresent()) continue;
					onMouseClick(handler.getSlot(oi.getAsInt()), i, 0, SlotActionType.PICKUP);
					return;
				}
			}
		}
	}
}
