package com.unascribed.fabrication.mixin.f_balance.mobs_dont_drop_ingots;

import java.util.function.Consumer;

import com.unascribed.fabrication.FabConf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.SpecialEligibility;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabModifyArg;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.mobs_dont_drop_ingots")
@FailOn(invertedSpecialConditions=SpecialEligibility.NOT_FORGE)
public abstract class MixinLivingEntity extends Entity {

	public MixinLivingEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@FabModifyArg(method="dropLoot(Lnet/minecraft/entity/damage/DamageSource;Z)V", at=@At(value="INVOKE", target="Lnet/minecraft/loot/LootTable;generateLoot(Lnet/minecraft/loot/context/LootContextParameterSet;JLjava/util/function/Consumer;)V"))
	public Consumer<ItemStack> generateLoot(Consumer<ItemStack> lootConsumer) {
		if(!FabConf.isEnabled("*.mobs_dont_drop_ingots")) return lootConsumer;
		return (stack)-> {
			Item replacement = null;
			Item current = stack.getItem();
			if (Items.IRON_INGOT.equals(current)) replacement = Items.IRON_NUGGET;
			else if (Items.GOLD_INGOT.equals(current)) replacement = Items.GOLD_NUGGET;
			else if (Items.COPPER_INGOT.equals(current) || Items.NETHERITE_INGOT.equals(current) || Items.GOLD_NUGGET.equals(current) || Items.IRON_NUGGET.equals(current))
				replacement = Items.AIR;
			if (replacement != null) {
				NbtElement tag = stack.encodeAllowEmpty(this.getRegistryManager());
				if (!(tag instanceof NbtCompound)) return;
				((NbtCompound) tag).putString("id", Registries.ITEM.getId(replacement).toString());
				stack = ItemStack.fromNbtOrEmpty(this.getRegistryManager(), (NbtCompound) tag);
			}

			lootConsumer.accept(stack);
		};
	}
}
