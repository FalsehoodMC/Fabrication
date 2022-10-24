package com.unascribed.fabrication.mixin.g_weird_tweaks.curable_piglins;

import com.unascribed.fabrication.interfaces.SetPreZombified;
import com.unascribed.fabrication.interfaces.ZombImmunizableEntity;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(AbstractPiglinEntity.class)
@EligibleIf(configAvailable="*.curable_piglins")
public abstract class MixinAbstractPiglinEntity extends LivingEntity implements ZombImmunizableEntity {

	@Shadow
	protected abstract boolean isImmuneToZombification();

	@Shadow
	public abstract void setImmuneToZombification(boolean immuneToZombification);

	protected MixinAbstractPiglinEntity(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@FabInject(method="zombify(Lnet/minecraft/server/world/ServerWorld;)V", at=@At("TAIL"), locals=LocalCapture.CAPTURE_FAILHARD)
	public void tagPiglin(ServerWorld world, CallbackInfo ci, ZombifiedPiglinEntity zombifiedPiglinEntity) {
		if (zombifiedPiglinEntity instanceof SetPreZombified) {
			((SetPreZombified)zombifiedPiglinEntity).fabrication$setPreZombifiedType((EntityType<? extends MobEntity>) this.getType());
		}
	}

	@Override
	public void fabrication$setZombImmune(boolean zombImmune) {
		setImmuneToZombification(zombImmune);
	}

	@Override
	public boolean fabrication$isZombImmune() {
		return this.isImmuneToZombification();
	}
}
