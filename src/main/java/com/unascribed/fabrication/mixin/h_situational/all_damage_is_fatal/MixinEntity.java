package com.unascribed.fabrication.mixin.h_situational.all_damage_is_fatal;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.all_damage_is_fatal")
public abstract class MixinEntity extends Entity {

	public MixinEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@ModifyVariable(at=@At("HEAD"), method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", index=2, argsOnly=true)
	public float adjustDamage(float amount) {
		if (amount == 0) return 0;
		return MixinConfigPlugin.isEnabled("*.all_damage_is_fatal") ? ((LivingEntity)(Object)this).getHealth()*20 : amount;
	}
	
}
