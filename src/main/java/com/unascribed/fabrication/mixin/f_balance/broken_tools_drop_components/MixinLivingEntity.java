package com.unascribed.fabrication.mixin.f_balance.broken_tools_drop_components;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.FabRefl;
import com.unascribed.fabrication.Resolvable;
import com.unascribed.fabrication.loaders.LoaderGearComponents;
import com.unascribed.fabrication.loaders.LoaderGearComponents.ItemMaterialValue;
import com.unascribed.fabrication.loaders.LoaderGearComponents.MaterialData;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Hand;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
@EligibleIf(configEnabled="*.broken_tools_drop_components")
public abstract class MixinLivingEntity extends Entity {

	public MixinLivingEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject(at=@At("HEAD"), method="sendEquipmentBreakStatus(Lnet/minecraft/entity/EquipmentSlot;)V")
	public void sendEquipmentBreakStatus(EquipmentSlot slot, CallbackInfo ci) {
		shatter(slot, ((LivingEntity)(Object)this).getEquippedStack(slot));
	}
	
	@Inject(at=@At("HEAD"), method="sendToolBreakStatus(Lnet/minecraft/util/Hand;)V")
	public void sendToolBreakStatus(Hand hand, CallbackInfo ci) {
		shatter(hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND, ((LivingEntity)(Object)this).getStackInHand(hand));
	}

	@Unique
	private void shatter(EquipmentSlot slot, ItemStack stack) {
		if (!RuntimeChecks.check("*.broken_tools_drop_components")) return;
		Item item = stack.getItem();
		if (LoaderGearComponents.ignoreVanishing && EnchantmentHelper.hasVanishingCurse(stack)) return;
		if (stack.hasTag() && stack.getTag().getBoolean("fabrication:ShatteredAlready")) return;
		if (!stack.hasTag()) stack.setTag(new CompoundTag());
		stack.getTag().putBoolean("fabrication:ShatteredAlready", true);
		for (ItemMaterialValue imv : LoaderGearComponents.items.get(Resolvable.mapKey(Registry.ITEM.getId(item), Registry.ITEM))) {
			MaterialData md = LoaderGearComponents.materials.get(imv.materialName);
			if (md == null) continue;
			Item ingot = md.ingotGetter.get();
			Item nugget = md.nuggetGetter.get();
			int nuggetsPerIngot = md.nuggetsPerIngot;
			double value = imv.valueInIngots;
			int valueInNuggets = (int)(value*nuggetsPerIngot);
			double dropChance = 1;
			Object self = this;
			if (self instanceof MobEntity) {
				dropChance = FabRefl.MobEntity_getDropChance((MobEntity)self, slot);
				if (dropChance > 1) dropChance = 1;
				if (dropChance <= 0) continue;
			}
			double dropRate = imv.ignoreDropRate ? 1 : LoaderGearComponents.dropRate.getAsDouble();
			int nuggetsToReturn = (int)(valueInNuggets*(dropRate*dropChance));
			if (!imv.ignoreDropRate) {
				nuggetsToReturn -= LoaderGearComponents.cheat;
			}
			if (nuggetsToReturn <= 0) continue;
			if (ingot != null) {
				int guaranteed = LoaderGearComponents.guaranteedIngots;
				int maxIngotsToReturn = nuggetsToReturn/nuggetsPerIngot;
				int ingotsToReturn;
				if (maxIngotsToReturn <= 0) {
					ingotsToReturn = 0;
				} else if (guaranteed >= maxIngotsToReturn) {
					ingotsToReturn = maxIngotsToReturn;
				} else {
					ingotsToReturn = world.random.nextInt(maxIngotsToReturn+1-guaranteed)+guaranteed;
				}
				nuggetsToReturn -= ingotsToReturn * nuggetsPerIngot;
				for (int i = 0; i < ingotsToReturn; i++) {
					dropItem(ingot);
				}
			}
			if (nugget != null) {
				for (int i = 0; i < nuggetsToReturn; i++) {
					dropItem(nugget);
				}
			}
		}
	}
	
}
