package com.unascribed.fabrication.mixin.e_mechanics.weaponized_pearls;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(EnderPearlEntity.class)
@EligibleIf(configAvailable="*.weaponized_pearls")
public abstract class MixinEnderPearlEntity extends ThrownItemEntity {

	public MixinEnderPearlEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
		super(entityType, world);
	}

	private static final Predicate<ThrownItemEntity> fabrication$weaponizedPearsPredicate = ConfigPredicates.getFinalPredicate("*.weaponized_pearls");

	private LivingEntity fabrication$pearljustTeleported = null;

	@FabInject(method="onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V", at=@At("TAIL"))
	public void chorusEffect(EntityHitResult entityHitResult, CallbackInfo ci){
		if (!FabConf.isEnabled("*.weaponized_pearls") || world.isClient) return;
		if (!fabrication$weaponizedPearsPredicate.test(this)) return;
		Entity ent = entityHitResult.getEntity();
		if (ent instanceof LivingEntity) {
			LivingEntity hit = (LivingEntity)ent;
			double d = hit.getX();
			double e = hit.getY();
			double f = hit.getZ();

			if (hit.hasVehicle()) {
				hit.stopRiding();
			}

			for (int i = 0; i < 16; ++i) {
				double g = hit.getX() + (random.nextDouble() - 0.5D) * 24.0D;
				double h = MathHelper.clamp(hit.getY() + (random.nextInt(24) - 12), world.getBottomY(), (world.getBottomY() + world.getHeight() - 1));
				double j = hit.getZ() + (random.nextDouble() - 0.5D) * 24.0D;

				if (hit.teleport(g, h, j, true)) {
					fabrication$pearljustTeleported = hit;
					world.playSound( null, d, e, f, hit instanceof FoxEntity ? SoundEvents.ENTITY_FOX_TELEPORT : SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
					break;
				}
			}

		}
	}

	@FabInject(method="onCollision(Lnet/minecraft/util/hit/HitResult;)V", at=@At(value="INVOKE", shift=At.Shift.AFTER, target="Lnet/minecraft/entity/projectile/thrown/ThrownItemEntity;onCollision(Lnet/minecraft/util/hit/HitResult;)V"), cancellable = true)
	public void pushEffect(HitResult hitResult, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.weaponized_pearls") || world.isClient || this.isRemoved()) return;
		if (!fabrication$weaponizedPearsPredicate.test(this)) return;
		world.playSound(null, getBlockPos(), SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.PLAYERS, 0.1f, 2.0F);
		((ServerWorld)world).spawnParticles(ParticleTypes.PORTAL, getX(), getY(), getZ(), 32, 0.125F, 0.125F, 0.125F, 0.8F);
		world.playSound( null, getX(), getY(), getZ(), SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 0.6F, 3.0F);
		for (LivingEntity hit : world.getEntitiesByClass(LivingEntity.class, new Box(getBlockPos()).expand(10), e -> !e.isSpectator() && (fabrication$pearljustTeleported == null || e != fabrication$pearljustTeleported))) {
			float exposure = Explosion.getExposure(getPos(), hit);
			if (exposure > 0) {
				Vec3d vec = getPos().subtract(hit.getEyePos()).multiply(exposure*(3f/Math.max(Math.min(hit.distanceTo(this), 10), 0.1f)));
				hit.addVelocity(vec.x, vec.y, vec.z);
				if (hit instanceof ServerPlayerEntity && ((ServerPlayerEntity)hit).networkHandler.getConnection().isOpen()) {
					((ServerPlayerEntity)hit).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(hit));
				}
			}
		}
		discard();
		ci.cancel();
	}

}
