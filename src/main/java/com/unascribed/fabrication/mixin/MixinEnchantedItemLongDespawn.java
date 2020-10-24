package com.unascribed.fabrication.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;

@Mixin(ItemEntity.class)
@EligibleIf(configEnabled="*.enchanted_item_long_despawn")
public class MixinEnchantedItemLongDespawn {

	private boolean fabrication$enchanted;
	
	@Shadow
	private int age;
	
	@Inject(at=@At("TAIL"), method="setStack(Lnet/minecraft/item/ItemStack;)V")
	public void setStack(ItemStack stack, CallbackInfo ci) {
		if (!RuntimeChecks.check("*.enchanted_item_long_despawn")) return;
		boolean enchanted = stack.hasEnchantments();
		if (enchanted && !fabrication$enchanted) {
			this.age -= 30000;
		} else if (!enchanted && fabrication$enchanted) {
			this.age += 30000;
		}
		fabrication$enchanted = enchanted;
	}
	
}
