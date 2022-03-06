package com.unascribed.fabrication.mixin.c_tweaks.no_hunger;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
@EligibleIf(configAvailable="*.no_hunger")
public abstract class MixinPlayerEntity extends LivingEntity {

	protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(at=@At("HEAD"), method="eatFood(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", cancellable=true)
	public void eatFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> ci) {
		if (FabConf.isEnabled("*.no_hunger")) {
			if (ConfigPredicates.shouldRun("*.no_hunger", (PlayerEntity)(Object)this)) {
				Object self = this;
				Item item = stack.getItem();
				if (item.isFood()) {
					FoodComponent food = item.getFoodComponent();
					heal((int)((food.getHunger()+food.getSaturationModifier())*0.75f));
				}
				((PlayerEntity)self).incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
				world.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5f, world.random.nextFloat() * 0.1f + 0.9f);
				if (self instanceof ServerPlayerEntity) {
					Criteria.CONSUME_ITEM.trigger((ServerPlayerEntity)self, stack);
				}
				ci.setReturnValue(super.eatFood(world, stack));
			}
		}
	}

}
