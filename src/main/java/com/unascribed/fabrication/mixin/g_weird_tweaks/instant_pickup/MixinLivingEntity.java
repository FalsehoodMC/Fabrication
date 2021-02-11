package com.unascribed.fabrication.mixin.g_weird_tweaks.instant_pickup;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.InstantPickup;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
@EligibleIf(configEnabled="*.instant_pickup")
public abstract class MixinLivingEntity extends Entity {

	public MixinLivingEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@Shadow
	protected PlayerEntity attackingPlayer;
	
	@Inject(at=@At("TAIL"), method="onDeath(Lnet/minecraft/entity/damage/DamageSource;)V")
	public void onDeath(DamageSource src, CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.instant_pickup") && src.getSource() instanceof PlayerEntity) {
			InstantPickup.slurp(world, getBoundingBox().expand(0.25), (PlayerEntity)src.getSource());
		}
	}
	
}
