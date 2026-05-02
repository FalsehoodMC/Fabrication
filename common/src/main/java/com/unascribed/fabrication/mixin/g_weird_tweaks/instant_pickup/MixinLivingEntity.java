package com.unascribed.fabrication.mixin.g_weird_tweaks.instant_pickup;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.logic.InstantPickup;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.instant_pickup")
public abstract class MixinLivingEntity extends Entity {

	public MixinLivingEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@Shadow
	protected PlayerEntity attackingPlayer;

	@FabInject(at=@At("TAIL"), method="onDeath(Lnet/minecraft/entity/damage/DamageSource;)V")
	public void onDeath(DamageSource src, CallbackInfo ci) {
		if (FabConf.isEnabled("*.instant_pickup") && src.getSource() instanceof PlayerEntity) {
			InstantPickup.slurp(world, getBoundingBox().expand(0.25), (PlayerEntity)src.getSource());
		}
	}

}
