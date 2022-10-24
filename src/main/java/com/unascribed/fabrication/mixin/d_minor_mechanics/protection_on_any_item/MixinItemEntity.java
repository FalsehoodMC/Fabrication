package com.unascribed.fabrication.mixin.d_minor_mechanics.protection_on_any_item;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
@EligibleIf(configAvailable="*.protection_on_any_item")
public abstract class MixinItemEntity {

	@Shadow
	public abstract ItemStack getStack();

	@FabInject(at=@At("HEAD"), method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", cancellable=true)
	public void isFireImmune(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (FabConf.isEnabled("*.protection_on_any_item")) {
			int lvl = EnchantmentHelper.getLevel(Enchantments.PROTECTION, getStack());
			if (lvl>3) lvl = 4;
			switch (lvl){
				case 4: if (source.isExplosive()) cir.setReturnValue(false);
				case 3: if (source == DamageSource.LAVA) cir.setReturnValue(false);
				case 2: if (source == DamageSource.IN_FIRE || source == DamageSource.ON_FIRE) cir.setReturnValue(false);
				case 1: if (source == DamageSource.CACTUS) cir.setReturnValue(false);
			}
		}
	}

}
