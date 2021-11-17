package com.unascribed.fabrication.mixin.e_mechanics.grindstone_disenchanting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.interfaces.SetOwner;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

@Mixin(GrindstoneScreenHandler.class)
@EligibleIf(configAvailable="*.grindstone_disenchanting", modNotLoaded="fabric:grindenchantments")
public abstract class MixinGrindstoneScreenHandler extends ScreenHandler {

	protected MixinGrindstoneScreenHandler(ScreenHandlerType<?> type, int syncId) {
		super(type, syncId);
	}

	@Shadow
	private ItemStack grind(ItemStack item, int damage, int amount) { return null; }

	@Inject(at=@At("TAIL"), method="<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V")
	private void construct(CallbackInfo ci) {
		for (Slot s : slots) {
			if (s instanceof SetOwner) {
				((SetOwner)s).fabrication$setOwner(this);
			}
		}
	}

	@Inject(at=@At("HEAD"), method="updateResult()V", cancellable=true)
	private void updateResult(CallbackInfo ci) {
		if (getSlot(1).getStack().getItem() == Items.BOOK && getSlot(1).getStack().getCount() == 1 && getSlot(0).getStack().hasEnchantments()) {
			getSlot(2).setStack(grind(getSlot(0).getStack(), getSlot(0).getStack().getDamage(), getSlot(0).getStack().getCount()));
			sendContentUpdates();
			ci.cancel();
		}
	}

}
