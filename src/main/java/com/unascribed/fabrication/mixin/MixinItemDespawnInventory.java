package com.unascribed.fabrication.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.unascribed.fabrication.interfaces.SetFromPlayerDeath;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.SpecialEligibility;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

@Mixin(PlayerInventory.class)
@EligibleIf(specialConditions=SpecialEligibility.ITEM_DESPAWN_NOT_ALL_UNSET)
public abstract class MixinItemDespawnInventory {

	@Redirect(at=@At(value="INVOKE", target="net/minecraft/entity/player/PlayerEntity.dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;"),
			method="dropAll()V")
	public ItemEntity dropItem(PlayerEntity subject, ItemStack stack, boolean throwRandomly, boolean retainOwnership) {
		ItemEntity e = subject.dropItem(stack, throwRandomly, retainOwnership);
		if (e instanceof SetFromPlayerDeath) {
			((SetFromPlayerDeath)e).fabrication$setFromPlayerDeath(true);
		}
		return e;
	}
	
	
}
