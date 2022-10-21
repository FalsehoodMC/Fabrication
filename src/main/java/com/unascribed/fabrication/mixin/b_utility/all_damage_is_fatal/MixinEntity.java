package com.unascribed.fabrication.mixin.b_utility.all_damage_is_fatal;

import com.google.common.collect.ImmutableList;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabModifyVariable;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Predicate;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.all_damage_is_fatal")
public abstract class MixinEntity extends Entity {

	public MixinEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	private static final Predicate<List<?>> fabrication$damagePredicate = ConfigPredicates.getFinalPredicate("*.all_damage_is_fatal");

	@FabModifyVariable(at=@At("HEAD"), method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", index=2, argsOnly=true)
	public float adjustDamage(float amount, DamageSource source) {
		if (amount == 0) return 0;
		return FabConf.isEnabled("*.all_damage_is_fatal") && fabrication$damagePredicate.test(ImmutableList.of(this, source)) ? ((LivingEntity)(Object)this).getHealth()*20 : amount;
	}

}
