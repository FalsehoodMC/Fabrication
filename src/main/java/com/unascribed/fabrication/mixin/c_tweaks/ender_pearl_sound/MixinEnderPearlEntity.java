package com.unascribed.fabrication.mixin.c_tweaks.ender_pearl_sound;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderPearlEntity.class)
@EligibleIf(configAvailable="*.ender_pearl_sound")
public abstract class MixinEnderPearlEntity extends ThrownItemEntity {

	public MixinEnderPearlEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(at=@At(value="INVOKE", target="Lnet/minecraft/entity/projectile/thrown/EnderPearlEntity;discard()V"), method="onCollision(Lnet/minecraft/util/hit/HitResult;)V")
	public void teleportSound(HitResult hitResult, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.ender_pearl_sound")) return;
		world.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1, 0.5f);
	}
}
