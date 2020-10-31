package com.unascribed.fabrication.mixin.f_balance.broken_tools_drop_components;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.tag.ItemTags;
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
		if (!"minecraft".equals(Registry.ITEM.getId(item).getNamespace())) return;
		if (EnchantmentHelper.hasVanishingCurse(stack)) return;
		Item ingot;
		Item nugget;
		int nuggetsPerIngot = 9;
		int value;
		int sticks = 0;
		boolean netherite = false;
		if (item instanceof ArmorItem) {
			ArmorMaterial mat = ((ArmorItem) item).getMaterial();
			if (!(mat instanceof ArmorMaterials)) return;
			switch ((ArmorMaterials)mat) {
				case LEATHER:
					ingot = Items.LEATHER;
					nugget = Items.RABBIT_HIDE;
					nuggetsPerIngot = 4;
					break;
				case CHAIN:
					ingot = Items.CHAIN;
					nugget = Items.IRON_NUGGET;
					nuggetsPerIngot = 3;
					break;
				case GOLD:
					ingot = Items.GOLD_INGOT;
					nugget = Items.GOLD_NUGGET;
					break;
				case IRON:
					ingot = Items.IRON_INGOT;
					nugget = Items.IRON_NUGGET;
					break;
				case NETHERITE:
					netherite = true;
					// fallthru
				case DIAMOND:
					ingot = Items.DIAMOND;
					nugget = Items.COAL;
					nuggetsPerIngot = 4;
					break;
				case TURTLE:
					ingot = Items.SCUTE;
					nugget = null;
					break;
				default:
					return;
			}
			switch (((ArmorItem)item).getSlotType()) {
				case HEAD:
					value = 5;
					break;
				case CHEST:
					value = 8;
					break;
				case LEGS:
					value = 7;
					break;
				case FEET:
					value = 4;
					break;
				default:
					return;
			}
		} else if (item instanceof ToolItem) {
			ToolMaterial mat = ((ToolItem) item).getMaterial();
			if (!(mat instanceof ToolMaterials)) return;
			switch ((ToolMaterials)mat) {
				case WOOD:
					ingot = ItemTags.PLANKS.getRandom(world.random);
					nugget = Items.STICK;
					nuggetsPerIngot = 2;
					break;
				case STONE:
					ingot = Items.COBBLESTONE;
					nugget = Items.COBBLESTONE_SLAB;
					nuggetsPerIngot = 2;
					break;
				case GOLD:
					ingot = Items.GOLD_INGOT;
					nugget = Items.GOLD_NUGGET;
					break;
				case IRON:
					ingot = Items.IRON_INGOT;
					nugget = Items.IRON_NUGGET;
					break;
				case NETHERITE:
					netherite = true;
					// fallthru
				case DIAMOND:
					ingot = Items.DIAMOND;
					nugget = Items.COAL;
					nuggetsPerIngot = 4;
					break;
				default:
					return;
			}
			if (item instanceof SwordItem) {
				sticks = 1;
				value = 2;
			} else if (item instanceof PickaxeItem) {
				sticks = 2;
				value = 3;
			} else if (item instanceof AxeItem) {
				sticks = 2;
				value = 3;
			} else if (item instanceof HoeItem) {
				sticks = 2;
				value = 2;
			} else if (item instanceof ShovelItem) {
				sticks = 2;
				value = 1;
			} else {
				return;
			}
		} else {
			return;
		}
		int valueInNuggets = value*nuggetsPerIngot;
		float dropChance = 1;
		Object self = this;
		if (self instanceof MobEntity) {
			dropChance = FabricationMod.twiddle(MobEntity.class, this, "method_5929", "getDropChance", new Class<?>[] {EquipmentSlot.class}, slot);
			if (dropChance > 1) dropChance = 1;
			if (dropChance <= 0) return;
		}
		int nuggetsToReturn = (int)(valueInNuggets*(0.75*dropChance))-1;
		if (nuggetsToReturn <= 0) return;
		dropNuggetsAndIngots(nuggetsToReturn, nuggetsPerIngot, ingot, nugget);
		if (netherite) {
			for (int i = 0; i < 4; i++) {
				dropItem(Items.NETHERITE_SCRAP);
			}
			dropNuggetsAndIngots(26, 9, Items.GOLD_INGOT, Items.GOLD_NUGGET);
		}
		if (sticks > 0) {
			for (int i = 0; i < world.random.nextInt(sticks+1); i++) {
				dropItem(Items.STICK);
			}
		}
	}

	@Unique
	private void dropNuggetsAndIngots(int nuggetsToReturn, int nuggetsPerIngot, Item ingot, Item nugget) {
		int maxIngotsToReturn = nuggetsToReturn/nuggetsPerIngot;
		int ingotsToReturn = nugget == null ? maxIngotsToReturn : maxIngotsToReturn <= 0 ? 0 : world.random.nextInt(maxIngotsToReturn)+1;
		nuggetsToReturn -= ingotsToReturn * nuggetsPerIngot;
		for (int i = 0; i < ingotsToReturn; i++) {
			dropItem(ingot);
		}
		if (nugget != null) {
			for (int i = 0; i < nuggetsToReturn; i++) {
				dropItem(nugget);
			}
		}
	}
	
}
