package com.unascribed.fabrication.mixin.b_utility.item_despawn;

import com.unascribed.fabrication.interfaces.SetFromPlayerDeath;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerInventory.class)
@EligibleIf(configAvailable="*.item_despawn")
public abstract class MixinPlayerInventory {

	@ModifyReturn(target={"net/minecraft/entity/player/PlayerEntity.dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", "Lnet/minecraft/class_1657;method_7329(Lnet/minecraft/class_1799;ZZ)Lnet/minecraft/class_1542;"},
			method={"dropAll()V", "method_7388()V"})
	private static ItemEntity fabrication$tagDroppedItem(ItemEntity e) {
		if (e instanceof SetFromPlayerDeath) {
			((SetFromPlayerDeath)e).fabrication$setFromPlayerDeath(true);
		}
		return e;
	}


}
