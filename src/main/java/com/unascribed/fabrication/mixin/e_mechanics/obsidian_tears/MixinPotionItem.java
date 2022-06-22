package com.unascribed.fabrication.mixin.e_mechanics.obsidian_tears;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.logic.ObsidianTears;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

@Mixin(PotionItem.class)
@EligibleIf(configAvailable="*.obsidian_tears")
public class MixinPotionItem {

	@FabInject(at=@At("HEAD"), method="finishUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;")
	public void finishUsing(ItemStack stack, World world, LivingEntity quaffer, CallbackInfoReturnable<ItemStack> ci) {
		if (!FabConf.isEnabled("*.obsidian_tears")) return;
		if (quaffer instanceof ServerPlayerEntity && !world.isClient && stack.hasNbt() && stack.getNbt().getBoolean("fabrication:ObsidianTears")) {
			ObsidianTears.setSpawnPoint((ServerPlayerEntity)quaffer, stack);
		}
	}

}
