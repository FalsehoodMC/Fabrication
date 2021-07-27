package com.unascribed.fabrication.mixin.e_mechanics.obsidian_tears;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.ObsidianTears;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

@Mixin(PotionItem.class)
@EligibleIf(configEnabled="*.obsidian_tears")
public class MixinPotionItem {

	@Inject(at=@At("HEAD"), method="finishUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;")
	public void finishUsing(ItemStack stack, World world, LivingEntity quaffer, CallbackInfoReturnable<ItemStack> ci) {
		if (!MixinConfigPlugin.isEnabled("*.obsidian_tears")) return;
		if (quaffer instanceof ServerPlayerEntity && !world.isClient && stack.hasNbt() && stack.getNbt().getBoolean("fabrication:ObsidianTears")) {
			ObsidianTears.setSpawnPoint((ServerPlayerEntity)quaffer, stack);
		}
	}
	
}
