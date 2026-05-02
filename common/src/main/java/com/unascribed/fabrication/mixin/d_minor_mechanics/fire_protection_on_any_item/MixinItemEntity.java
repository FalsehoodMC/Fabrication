package com.unascribed.fabrication.mixin.d_minor_mechanics.fire_protection_on_any_item;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
@EligibleIf(configAvailable="*.fire_protection_on_any_item")
public abstract class MixinItemEntity {

	@Shadow
	public abstract ItemStack getStack();

	@FabInject(at=@At("HEAD"), method="isFireImmune()Z", cancellable=true)
	public void isFireImmune(CallbackInfoReturnable<Boolean> cir) {
		if (FabConf.isEnabled("*.fire_protection_on_any_item") && EnchantmentHelper.getLevel(Enchantments.FIRE_PROTECTION, getStack()) > 0) {
			cir.setReturnValue(true);
		}
	}

}
