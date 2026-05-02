package com.unascribed.fabrication.mixin.g_weird_tweaks.instant_pickup;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.logic.InstantPickup;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorStandEntity.class)
@EligibleIf(configAvailable="*.instant_pickup")
public abstract class MixinArmorStandEntity extends LivingEntity {

	protected MixinArmorStandEntity(EntityType<? extends LivingEntity> arg, World arg2) {
		super(arg, arg2);
	}

	// grr...

	@FabInject(at=@At("TAIL"), method="onBreak(Lnet/minecraft/entity/damage/DamageSource;)V")
	private void onBreak(DamageSource src, CallbackInfo ci) {
		if (FabConf.isEnabled("*.instant_pickup") && src.getSource() instanceof PlayerEntity) {
			InstantPickup.slurp(world, getBoundingBox().expand(0.25), (PlayerEntity)src.getSource());
		}
	}

}
