package com.unascribed.fabrication.mixin.e_mechanics.grindstone_disenchanting;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.injection.FabInject;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.interfaces.SetOwner;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.world.World;

@Mixin(targets="net.minecraft.screen.GrindstoneScreenHandler$4")
@EligibleIf(configAvailable="*.grindstone_disenchanting", modNotLoaded="pollen")
@FailOn(modLoaded="fabric:grindenchantments")
public class MixinGrindstoneScreenHandlerResultSlot implements SetOwner<GrindstoneScreenHandler> {

	@Unique
	private ItemStack fabrication$storedResultBook;
	@Unique
	private GrindstoneScreenHandler fabrication$owner;

	@Override
	public void fabrication$setOwner(GrindstoneScreenHandler owner) {
		fabrication$owner = owner;
	}

	@FabInject(at=@At("HEAD"), method="onTakeItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V")
	public void onTakeItemPre(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
		fabrication$storedResultBook = null;
		if (FabConf.isEnabled("*.grindstone_disenchanting") && fabrication$owner.getSlot(1).getStack().getItem() == Items.BOOK) {
			fabrication$storedResultBook = fabrication$owner.getSlot(1).getStack();
			for (Object2IntMap.Entry<RegistryEntry<Enchantment>> en : EnchantmentHelper.getEnchantments(fabrication$owner.getSlot(0).getStack()).getEnchantmentEntries()) {
				if (en.getKey().isIn(EnchantmentTags.CURSE)) continue;
				if (fabrication$storedResultBook.getItem() != Items.ENCHANTED_BOOK) {
					fabrication$storedResultBook = new ItemStack(Items.ENCHANTED_BOOK);
				}
				fabrication$storedResultBook.addEnchantment(en.getKey(), en.getIntValue());
			}
		}
	}

	@FabInject(at=@At("TAIL"), method="onTakeItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V")
	public void onTakeItemPost(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
		if (fabrication$storedResultBook != null) {
			fabrication$owner.getSlot(1).setStack(fabrication$storedResultBook);
			fabrication$storedResultBook = null;
		}
	}

	@FabInject(at=@At("HEAD"), method="getExperience(Lnet/minecraft/world/World;)I", cancellable=true)
	private void getExperience(World world, CallbackInfoReturnable<Integer> ci) {
		if (fabrication$storedResultBook != null) ci.setReturnValue(0);
	}

}
