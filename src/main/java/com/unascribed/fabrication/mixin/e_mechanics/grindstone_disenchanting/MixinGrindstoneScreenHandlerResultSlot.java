package com.unascribed.fabrication.mixin.e_mechanics.grindstone_disenchanting;

import java.util.Map;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
import net.minecraft.world.World;

@Mixin(targets="net.minecraft.screen.GrindstoneScreenHandler$4")
@EligibleIf(configAvailable="*.grindstone_disenchanting", modNotLoaded={"fabric:grindenchantments", "pollen"})
public class MixinGrindstoneScreenHandlerResultSlot implements SetOwner<GrindstoneScreenHandler> {

	@Unique
	private ItemStack fabrication$storedResultBook;
	@Unique
	private GrindstoneScreenHandler fabrication$owner;

	@Override
	public void fabrication$setOwner(GrindstoneScreenHandler owner) {
		fabrication$owner = owner;
	}

	@Inject(at=@At("HEAD"), method="onTakeItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V")
	public void onTakeItemPre(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
		fabrication$storedResultBook = null;
		if (FabConf.isEnabled("*.grindstone_disenchanting") && fabrication$owner.getSlot(1).getStack().getItem() == Items.BOOK) {
			fabrication$storedResultBook = fabrication$owner.getSlot(1).getStack();
			for (Map.Entry<Enchantment, Integer> en : EnchantmentHelper.get(fabrication$owner.getSlot(0).getStack()).entrySet()) {
				if (en.getKey().isCursed()) continue;
				if (fabrication$storedResultBook.getItem() != Items.ENCHANTED_BOOK) {
					fabrication$storedResultBook = new ItemStack(Items.ENCHANTED_BOOK);
				}
				EnchantedBookItem.addEnchantment(fabrication$storedResultBook, new EnchantmentLevelEntry(en.getKey(), en.getValue()));
			}
		}
	}

	@Inject(at=@At("TAIL"), method="onTakeItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V")
	public void onTakeItemPost(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
		if (fabrication$storedResultBook != null) {
			fabrication$owner.getSlot(1).setStack(fabrication$storedResultBook);
			fabrication$storedResultBook = null;
		}
	}

	@Inject(at=@At("HEAD"), method="getExperience(Lnet/minecraft/world/World;)I", cancellable=true)
	private void getExperience(World world, CallbackInfoReturnable<Integer> ci) {
		if (fabrication$storedResultBook != null) ci.setReturnValue(0);
	}

}
