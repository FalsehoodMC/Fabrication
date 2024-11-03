package com.unascribed.fabrication.mixin.f_balance.broken_tools_drop_components;


import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.FabRefl;
import com.unascribed.fabrication.loaders.LoaderGearComponents;
import com.unascribed.fabrication.loaders.LoaderGearComponents.ItemMaterialValue;
import com.unascribed.fabrication.loaders.LoaderGearComponents.MaterialData;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.util.Resolvable;

import com.google.common.collect.Lists;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.broken_tools_drop_components")
public abstract class MixinLivingEntity extends Entity {

	public MixinLivingEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@FabInject(at=@At("HEAD"), method= "sendEquipmentBreakStatus(Lnet/minecraft/item/Item;Lnet/minecraft/entity/EquipmentSlot;)V")
	public void sendEquipmentBreakStatus(Item item, EquipmentSlot slot, CallbackInfo ci) {
		shatter(slot, ((LivingEntity)(Object)this).getEquippedStack(slot));
	}

	@Unique
	private void shatter(EquipmentSlot slot, ItemStack stack) {
		if (!FabConf.isEnabled("*.broken_tools_drop_components")) return;
		Item item = stack.getItem();
		Optional<RegistryEntry.Reference<Enchantment>> vanishing = this.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.VANISHING_CURSE);
		if (LoaderGearComponents.ignoreVanishing && (vanishing.isEmpty() || stack.getEnchantments().getEnchantments().contains(vanishing.get()))) return;
		if (stack.contains(DataComponentTypes.CUSTOM_DATA) && stack.get(DataComponentTypes.CUSTOM_DATA).getNbt().getBoolean("fabrication:ShatteredAlready")) return;
		NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
		nbt.putBoolean("fabrication:ShatteredAlready", true);
		stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
		List<ItemStack> enchantables = Lists.newArrayList();
		for (ItemMaterialValue imv : LoaderGearComponents.items.get(Resolvable.mapKey(Registries.ITEM.getId(item), Registries.ITEM))) {
			double dropChance = 1;
			Object self = this;
			if (self instanceof MobEntity) {
				dropChance = FabRefl.MobEntity_getDropChance((MobEntity)self, slot);
				if (dropChance > 1) dropChance = 1;
				if (dropChance <= 0) continue;
			}
			double dropRate = imv.ignoreDropRate ? 1 : LoaderGearComponents.dropRate.getAsDouble();
			if (imv.materialName.equals("xp")) {
				int xpAmt = (int)Math.round(imv.valueInIngots*(dropRate*dropChance));
				Vec3d c = getBoundingBox().getCenter();
				while (xpAmt > 0) {
					int thisOrb = ExperienceOrbEntity.roundToOrbSize(xpAmt);
					xpAmt -= thisOrb;
					getWorld().spawnEntity(new ExperienceOrbEntity(getWorld(), c.x, c.y, c.z, thisOrb));
				}
			} else {
				MaterialData md = LoaderGearComponents.materials.get(imv.materialName);
				if (md == null) continue;
				Item ingot = md.ingotGetter.get();
				Item nugget = md.nuggetGetter.get();
				int nuggetsPerIngot = md.nuggetsPerIngot;
				double value = imv.valueInIngots;
				int valueInNuggets = (int)(value*nuggetsPerIngot);
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
						ingotsToReturn = getWorld().random.nextInt(maxIngotsToReturn+1-guaranteed)+guaranteed;
					}
					nuggetsToReturn -= ingotsToReturn * nuggetsPerIngot;
					if (imv.enchant && stack.hasEnchantments()) {
						for (int i = 0; i < ingotsToReturn; i++) {
							enchantables.add(new ItemStack(ingot));
						}
					} else {
						for (int i = 0; i < ingotsToReturn; i++) {
							dropItem(ingot);
						}
					}
				}
				if (nugget != null) {
					if (imv.enchant && stack.hasEnchantments()) {
						for (int i = 0; i < nuggetsToReturn; i++) {
							enchantables.add(new ItemStack(nugget));
						}
					} else {
						for (int i = 0; i < nuggetsToReturn; i++) {
							dropItem(nugget);
						}
					}
				}
			}
		}
		if (enchantables.size() == 1) {
			EnchantmentHelper.set(stack, enchantables.getFirst().getEnchantments());
		} else if (!enchantables.isEmpty()) {
			for (Object2IntMap.Entry<RegistryEntry<Enchantment>> en : stack.getEnchantments().getEnchantmentEntries()) {
				int lvl = en.getIntValue();
				int[] values;
				if (lvl == 1 || getWorld().random.nextInt(3) == 0) {
					values = new int[] {lvl};
				} else if (lvl == 2) {
					values = new int[] {1, 1};
				} else if (lvl >= 4 && enchantables.size() >= 4 && getWorld().random.nextBoolean()) {
					values = new int[] {lvl-1, lvl-2, lvl-3, lvl-3};
				} else if (enchantables.size() >= 3 && getWorld().random.nextBoolean()) {
					values = new int[] {lvl-1, lvl-2, lvl-2};
				} else  {
					values = new int[] {lvl-1, lvl-1};
				}
				if (values.length == 1) {
					enchantables.get(getWorld().random.nextInt(enchantables.size())).addEnchantment(en.getKey(), values[0]);
				} else {
					Collections.shuffle(enchantables);
					for (int i = 0; i < values.length; i++) {
						enchantables.get(i).addEnchantment(en.getKey(), values[i]);
					}
				}
			}
		}
		for (ItemStack is : enchantables) {
			dropStack(is);
		}
	}

}
