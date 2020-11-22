package com.unascribed.fabrication.mixin.g_weird_tweaks.gold_tools_useful_in_nether;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.features.FeatureGoldToolsUsefulInNether;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(MiningToolItem.class)
@EligibleIf(configEnabled="*.gold_tools_useful_in_nether")
public class MixinMiningToolItem {
	
	@Inject(at=@At("HEAD"), method="postMine(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/LivingEntity;)Z",
			cancellable=true)
	public void postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner, CallbackInfoReturnable<Boolean> ci) {
		if (!RuntimeChecks.check("*.gold_tools_useful_in_nether")) return;
		if (world.isClient) return;
		// originally I did this with a Fabric API event, but it wasn't flexible enough and only fired for players
		if ((state.isIn(FeatureGoldToolsUsefulInNether.NETHER_BLOCKS) ||
					(world.getDimension().isUltrawarm() && world.getDimension().isPiglinSafe() && state.getBlock().isIn(FeatureGoldToolsUsefulInNether.NETHER_BLOCKS_ONLY_IN_NETHER)))
				&& (!(miner instanceof PlayerEntity) || !((PlayerEntity)miner).abilities.creativeMode)) {
			if (!stack.isDamageable()) return;
			if (stack.getItem().isIn(FeatureGoldToolsUsefulInNether.GOLD_TOOLS)) {
				if (!stack.hasTag()) stack.setTag(new CompoundTag());
				int partialDamage = stack.getTag().getInt("PartialDamage");
				if (stack.getDamage() == 0) {
					// must have been repaired. reset for less jankiness
					partialDamage = 0;
				}
				if (partialDamage == 0) {
					partialDamage++;
					stack.damage(1, miner, (e) -> {
						e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
					});
				} else if (partialDamage < 50) {
					partialDamage++;
				} else {
					partialDamage = 0;
				}
				stack.getTag().putInt("PartialDamage", partialDamage);
				ci.setReturnValue(true);
			} else if (!stack.getItem().isIn(FeatureGoldToolsUsefulInNether.NETHER_TOOLS)) {
				// BRING OUT THE WHEEL OF PUNISHMENT
				stack.damage(49, miner, (e) -> {
					e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
				});
				world.playSound(null, miner.getPos().x, miner.getPos().y, miner.getPos().z, SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, miner.getSoundCategory(), 0.4f, 2.0f);
				world.sendEntityStatus(miner, (byte)47);
			}
		}
	}
	

}
