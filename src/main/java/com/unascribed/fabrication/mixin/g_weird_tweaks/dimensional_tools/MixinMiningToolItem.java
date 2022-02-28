package com.unascribed.fabrication.mixin.g_weird_tweaks.dimensional_tools;

import java.util.Collections;
import java.util.Set;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.loaders.LoaderDimensionalTools;
import com.unascribed.fabrication.loaders.LoaderDimensionalTools.MohsIdentifier;
import com.unascribed.fabrication.support.EligibleIf;

import com.google.common.collect.Sets;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(MiningToolItem.class)
@EligibleIf(configAvailable="*.dimensional_tools")
public class MixinMiningToolItem {

	@Inject(at=@At("HEAD"), method="postMine(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/LivingEntity;)Z",
			cancellable=true)
	public void postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner, CallbackInfoReturnable<Boolean> ci) {
		if (!FabConf.isEnabled("*.dimensional_tools")) return;
		if (world.isClient) return;
		if ((!(miner instanceof PlayerEntity) || !((PlayerEntity)miner).getAbilities().creativeMode)) {
			if (!stack.isDamageable()) return;
			if (stack.getMiningSpeedMultiplier(state) <= 1) {
				// tool is not effective against this block, don't penalize or reward
				return;
			}
			Set<MohsIdentifier> blockDimensions = LoaderDimensionalTools.getAssociatedDimensions(state.getBlock());
			Set<Identifier> effectiveBlockDimensions = Sets.newHashSet();
			for (MohsIdentifier mi : blockDimensions) {
				if (mi.isHard() || world.getRegistryKey().getValue().equals(mi.getId())) {
					effectiveBlockDimensions.add(mi.getId());
				}
			}
			Set<MohsIdentifier> toolDimensions = LoaderDimensionalTools.getAssociatedDimensionsForTool(stack);
			int factor = 1;
			if (toolDimensions.isEmpty() && blockDimensions.isEmpty()) {
				return;
			} else if (toolDimensions.isEmpty()) {
				toolDimensions = Collections.singleton(null);
			} else if (effectiveBlockDimensions.isEmpty()) {
				effectiveBlockDimensions = Collections.singleton(null);
			}
			out: for (MohsIdentifier tool : toolDimensions) {
				for (Identifier block : effectiveBlockDimensions) {
					int thisFactor = LoaderDimensionalTools.getDamageFactor(tool, block);
					if (thisFactor == 0) {
						factor = 0;
						break out;
					}
					if ((factor > 0 && thisFactor < 0)
							|| (factor < 0 && thisFactor < factor)
							|| (factor > 0 && thisFactor > factor)) {
						factor = thisFactor;
					}
				}
			}
			if (factor < 0) {
				if (!stack.hasNbt()) stack.setNbt(new NbtCompound());
				int legacyPartialDamage = stack.getNbt().getInt("PartialDamage");
				if (legacyPartialDamage != 0) {
					stack.getNbt().putDouble("fabrication:PartialDamage", legacyPartialDamage/50D);
					stack.getNbt().remove("PartialDamage");
				}
				double partialDamage = stack.getNbt().getDouble("fabrication:PartialDamage");
				if (stack.getDamage() == 0) {
					// must have been repaired. reset for less jankiness
					partialDamage = 0;
				}
				if (partialDamage <= 0) {
					partialDamage += 1/(double)(-factor);
					stack.damage(1, miner, (e) -> {
						e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
					});
				} else if (partialDamage < 1) {
					partialDamage += 1/(double)(-factor);
				} else {
					partialDamage = 0;
				}
				stack.getNbt().putDouble("fabrication:PartialDamage", partialDamage);
				ci.setReturnValue(true);
			} else if (factor > 1) {
				// BRING OUT THE WHEEL OF PUNISHMENT
				int ffactor = factor;
				stack.damage(factor-1, miner, (e) -> {
					if (ffactor > 30) {
						e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
					}
				});
				world.playSound(null, miner.getPos().x, miner.getPos().y, miner.getPos().z, SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, miner.getSoundCategory(), factor/125f, 2.0f);
				world.sendEntityStatus(miner, (byte)47);
				miner.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 40, 3, false, false, false));
				if (miner instanceof PlayerEntity) {
					((PlayerEntity)miner).getItemCooldownManager().set((Item)(Object)this, 40);
				}
			} else if (factor == 0) {
				ci.setReturnValue(true);
			}
		}
	}


}
