package com.unascribed.fabrication.mixin.e_mechanics.grindstone_disenchanting;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.SetOwner;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.injection.FabInject;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.screen.GrindstoneScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets="net.minecraft.screen.GrindstoneScreenHandler$4")
@EligibleIf(configAvailable="*.grindstone_disenchanting", modLoaded="pollen")
@FailOn(modLoaded="fabric:grindenchantments")
public class MixinGrindstoneScreenHandlerResultSlotPollen implements SetOwner<GrindstoneScreenHandler> {
	@Unique
	private GrindstoneScreenHandler fabrication$owner;

	@Override
	public void fabrication$setOwner(GrindstoneScreenHandler owner) {
		fabrication$owner = owner;
	}

	@FabInject(at=@At("HEAD"), method="onTakeItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V", cancellable=true)
	public void onTakeItemPre(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.grindstone_disenchanting")) return;
		ItemStack bookStack = fabrication$owner.getSlot(1).getStack();
		if (bookStack.getItem() != Items.BOOK) return;
		ItemStack disenchantStack = fabrication$owner.getSlot(0).getStack();
		if (disenchantStack.hasEnchantments()) {
			if (bookStack.getItem() != Items.ENCHANTED_BOOK) {
				bookStack = new ItemStack(Items.ENCHANTED_BOOK);
			}
			for (Object2IntMap.Entry<RegistryEntry<Enchantment>> en : EnchantmentHelper.getEnchantments(disenchantStack).getEnchantmentEntries()) {
				if (en.getKey().isIn(EnchantmentTags.CURSE)) continue;
				bookStack.addEnchantment(en.getKey(), en.getIntValue());
			}
			((AccessorGrindstoneScreenHandler)fabrication$owner).fabrication$getContext().run((world, pos) -> world.syncWorldEvent(1042, pos, 0));
			fabrication$owner.getSlot(0).setStack(ItemStack.EMPTY);
			fabrication$owner.getSlot(1).setStack(bookStack);
			ci.cancel();
		}
	}
}
