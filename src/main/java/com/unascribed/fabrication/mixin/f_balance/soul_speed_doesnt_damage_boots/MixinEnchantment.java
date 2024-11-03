package com.unascribed.fabrication.mixin.f_balance.soul_speed_doesnt_damage_boots;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.EnchantmentHelperHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Enchantment.class)
@EligibleIf(configAvailable="*.soul_speed_doesnt_damage_boots")
public class MixinEnchantment {

	@FabInject(at=@At("HEAD"), method="modifyItemDamage(Lnet/minecraft/server/world/ServerWorld;ILnet/minecraft/item/ItemStack;Lorg/apache/commons/lang3/mutable/MutableFloat;)V", cancellable=true)
	private void modify(ServerWorld world, int level, ItemStack stack, MutableFloat itemDamage, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.soul_speed_doesnt_damage_boots")) return;
		if (EnchantmentHelperHelper.matches(this, Enchantments.SOUL_SPEED)) ci.cancel();
	}

}
