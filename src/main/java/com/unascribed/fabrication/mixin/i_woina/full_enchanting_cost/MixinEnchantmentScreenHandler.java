package com.unascribed.fabrication.mixin.i_woina.full_enchanting_cost;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(EnchantmentScreenHandler.class)
@EligibleIf(configAvailable="*.full_enchanting_cost")
public class MixinEnchantmentScreenHandler {

	@Shadow @Final private Inventory inventory;

	@Shadow @Final public int[] enchantmentPower;

	@FabInject(method="method_17410(Lnet/minecraft/item/ItemStack;ILnet/minecraft/entity/player/PlayerEntity;ILnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V",
			at=@At(value="INVOKE", target="Lnet/minecraft/entity/player/PlayerEntity;applyEnchantmentCosts(Lnet/minecraft/item/ItemStack;I)V"))
	private void fullExperienceCost(ItemStack itemStack, int i, PlayerEntity playerEntity, int j, ItemStack itemStack2, World world, BlockPos pos, CallbackInfo ci) {
		if (FabConf.isEnabled("*.full_enchanting_cost"))
			playerEntity.applyEnchantmentCosts(this.inventory.getStack(0), this.enchantmentPower[i] -j);
	}

}
