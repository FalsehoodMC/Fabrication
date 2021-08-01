package com.unascribed.fabrication.mixin.b_utility.item_despawn;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.unascribed.fabrication.interfaces.SetFromPlayerDeath;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

@Mixin(PlayerInventory.class)
@EligibleIf(configAvailable="*.item_despawn")
public abstract class MixinPlayerInventory {

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
