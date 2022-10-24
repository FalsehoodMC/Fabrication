package com.unascribed.fabrication.mixin.f_balance.mobs_dont_drop_ingots;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.SpecialEligibility;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.registry.Registry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Consumer;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.mobs_dont_drop_ingots", specialConditions=SpecialEligibility.FORGE)
public abstract class MixinLivingEntityForge {

	@ModifyArg(method="dropLoot(Lnet/minecraft/entity/damage/DamageSource;Z)V",
			at=@At(value="INVOKE", target="java/util/List.forEach(Ljava/util/function/Consumer;)V", remap=false))
	public Consumer<ItemStack> splitLoot(Consumer<ItemStack> lootConsumer) {
		if(!FabConf.isEnabled("*.mobs_dont_drop_ingots")) return lootConsumer;
		return (stack)-> {
			Item replacement = null;
			Item current = stack.getItem();
			if (Items.IRON_INGOT.equals(current)) replacement = Items.IRON_NUGGET;
			else if (Items.GOLD_INGOT.equals(current)) replacement = Items.GOLD_NUGGET;
			else if (Items.NETHERITE_INGOT.equals(current) || Items.GOLD_NUGGET.equals(current) || Items.IRON_NUGGET.equals(current))
				replacement = Items.AIR;
			if (replacement != null) {
				NbtCompound tag = new NbtCompound();
				stack.writeNbt(tag);
				tag.putString("id", Registry.ITEM.getId(replacement).toString());
				stack = ItemStack.fromNbt(tag);
			}

			lootConsumer.accept(stack);
		};
	}

}
