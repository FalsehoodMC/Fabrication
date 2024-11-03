package com.unascribed.fabrication.mixin.z_combined.trident_enchantments;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.EnchantmentHelperHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(Enchantment.class)
@EligibleIf(anyConfigAvailable={"*.tridents_accept_power", "*.tridents_accept_sharpness"})
public abstract class MixinEnchantment {

	@FabInject(at=@At("HEAD"), method="canBeCombined(Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/registry/entry/RegistryEntry;)Z", cancellable=true)
	private static void canCombine(RegistryEntry<Enchantment> first, RegistryEntry<Enchantment> second, CallbackInfoReturnable<Boolean> cir) {
		if (!(FabConf.isEnabled("*.tridents_accept_sharpness") ||  FabConf.isEnabled("*.tridents_accept_power"))) return;
		Predicate<RegistryKey<Enchantment>> predicate = new Predicate<RegistryKey<Enchantment>>() {
			@Override
			public boolean test(RegistryKey<Enchantment> enchantment) {
				return enchantment == Enchantments.SHARPNESS || enchantment == Enchantments.IMPALING || enchantment == Enchantments.POWER;
			}
		};
		if (first.matches(predicate) && second.matches(predicate)) cir.setReturnValue(false);

	}
	@FabInject(at=@At("HEAD"), method="isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z", cancellable=true)
	private void isAcceptable(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (stack.getItem() != Items.TRIDENT) return;
		if (FabConf.isEnabled("*.tridents_accept_sharpness") && EnchantmentHelperHelper.matches(this, Enchantments.SHARPNESS)) {
			cir.setReturnValue(true);
		}
		if (FabConf.isEnabled("*.tridents_accept_power") && EnchantmentHelperHelper.matches(this, Enchantments.POWER)) {
			cir.setReturnValue(true);
		}
	}
	@FabInject(at=@At("HEAD"), method="modifyDamage(Lnet/minecraft/server/world/ServerWorld;ILnet/minecraft/item/ItemStack;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lorg/apache/commons/lang3/mutable/MutableFloat;)V")
	private void modify(ServerWorld world, int level, ItemStack stack, Entity user, DamageSource damageSource, MutableFloat damage, CallbackInfo ci) {
		if (stack.getItem() != Items.TRIDENT) return;
		if (FabConf.isEnabled("*.bedrock_impaling") && EnchantmentHelperHelper.matches(this, Enchantments.IMPALING) && level > 0 && user.isWet()) {
			damage.add(level*2.5f);
		}
		if (damageSource.getSource() instanceof TridentEntity) {
			if (FabConf.isEnabled("*.tridents_accept_sharpness") && EnchantmentHelperHelper.matches(this, Enchantments.SHARPNESS) && level > 0) {
				damage.add(.5f+.5f*level);
			}
			if (FabConf.isEnabled("*.tridents_accept_power") && EnchantmentHelperHelper.matches(this, Enchantments.POWER) && level > 0) {
				float powerMul = 1 + (0.25f * (level + 1));
				damage.add(damage.getValue()*powerMul + 8f*powerMul);
			}
		}
	}
}
