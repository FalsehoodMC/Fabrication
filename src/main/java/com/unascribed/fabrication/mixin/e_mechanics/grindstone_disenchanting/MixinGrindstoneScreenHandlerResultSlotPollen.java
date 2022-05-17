package com.unascribed.fabrication.mixin.e_mechanics.grindstone_disenchanting;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.SetOwner;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GrindstoneScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(targets="net.minecraft.screen.GrindstoneScreenHandler$4")
@EligibleIf(configAvailable="*.grindstone_disenchanting", modNotLoaded="fabric:grindenchantments", modLoaded="pollen")
public class MixinGrindstoneScreenHandlerResultSlotPollen implements SetOwner<GrindstoneScreenHandler> {
	@Unique
	private GrindstoneScreenHandler fabrication$owner;

	@Override
	public void fabrication$setOwner(GrindstoneScreenHandler owner) {
		fabrication$owner = owner;
	}

	@Inject(at=@At("HEAD"), method="onTakeItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V", cancellable=true)
	public void onTakeItemPre(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.grindstone_disenchanting")) return;
		ItemStack bookStack = fabrication$owner.getSlot(1).getStack();
		if (bookStack.getItem() != Items.BOOK) return;
		ItemStack disenchantStack = fabrication$owner.getSlot(0).getStack();
		if (disenchantStack.hasEnchantments()) {
			if (bookStack.getItem() != Items.ENCHANTED_BOOK) {
				bookStack = new ItemStack(Items.ENCHANTED_BOOK);
			}
			for (Map.Entry<Enchantment, Integer> en : EnchantmentHelper.get(disenchantStack).entrySet()) {
				if (en.getKey().isCursed()) continue;
				EnchantedBookItem.addEnchantment(bookStack, new EnchantmentLevelEntry(en.getKey(), en.getValue()));
			}
			((AccessorGrindstoneScreenHandler)fabrication$owner).fabrication$getContext().run((world, pos) -> world.syncWorldEvent(1042, pos, 0));
			fabrication$owner.getSlot(0).setStack(ItemStack.EMPTY);
			fabrication$owner.getSlot(1).setStack(bookStack);
			ci.cancel();
		}
	}
}
