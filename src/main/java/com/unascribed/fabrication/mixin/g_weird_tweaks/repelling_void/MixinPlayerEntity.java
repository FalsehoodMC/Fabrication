package com.unascribed.fabrication.mixin.g_weird_tweaks.repelling_void;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.google.common.collect.Lists;

import net.minecraft.block.SideShapeType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
@EligibleIf(configAvailable="*.repelling_void")
public abstract class MixinPlayerEntity extends LivingEntity {

	protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	private Vec3d fabrication$lastGroundPos;
	private final List<Vec3d> fabrication$voidFallTrail = Lists.newArrayList();
	private boolean fabrication$debted;
	
	@Inject(at=@At("TAIL"), method="tick()V")
	public void tick(CallbackInfo ci) {
		if (!MixinConfigPlugin.isEnabled("*.repelling_void")) return;
		if (onGround) {
			fabrication$lastGroundPos = getPos();
			fabrication$voidFallTrail.clear();
		} else if (fabrication$voidFallTrail.size() < 20) {
			fabrication$voidFallTrail.add(getPos());
		}
		if (fabrication$debted) {
			fabrication$debted = false;
			damage(DamageSource.OUT_OF_WORLD, 12);
		}
	}
	
	
	@Inject(at=@At("HEAD"), method= "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", cancellable=true)
	public void remove(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		Vec3d pos = fabrication$lastGroundPos;
		if (MixinConfigPlugin.isEnabled("*.repelling_void") && !fabrication$debted && source == DamageSource.OUT_OF_WORLD && pos != null && this.getY() < -10) {
			BlockPos bp = new BlockPos(pos).down();
			if (!world.getBlockState(bp).isSideSolid(world, bp, Direction.UP, SideShapeType.CENTER)) {
				boolean foundOne = false;
				out: for (int d = 1; d <= 3; d++) {
					for (int x = -d; x <= d; x++) {
						for (int z = -d; z <= d; z++) {
							bp = new BlockPos(pos).add(x, -1, z);
							if (world.getBlockState(bp).isSideSolid(world, bp, Direction.UP, SideShapeType.CENTER)) {
								foundOne = true;
								break out;
							}
						}
					}
				}
				if (!foundOne) {
					// sorry, we tried...
					bp = new BlockPos(pos).down();
				}
			}
			teleport(bp.getX()+0.5, bp.getY()+1, bp.getZ()+0.5);
			fallDistance = 0;
			world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1, 0.5f);
			world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.BLOCK_SHROOMLIGHT_PLACE, SoundCategory.PLAYERS, 1, 0.5f);
			world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.BLOCK_SHROOMLIGHT_PLACE, SoundCategory.PLAYERS, 1, 0.75f);
			world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.PLAYERS, 0.2f, 0.5f);
			Object self = this;
			if (!world.isClient && self instanceof ServerPlayerEntity) {
				Box box = getBoundingBox();
				((ServerWorld)world).spawnParticles((ServerPlayerEntity)self, ParticleTypes.PORTAL, true, pos.x, pos.y+(box.getYLength()/2), pos.z, 32, box.getXLength()/2, box.getYLength()/2, box.getZLength()/2, 0.2);
				((ServerWorld)world).spawnParticles(ParticleTypes.PORTAL, pos.x, pos.y+(box.getYLength()/2), pos.z, 32, box.getXLength()/2, box.getYLength()/2, box.getZLength()/2, 0.2);
				for (Vec3d vec : fabrication$voidFallTrail) {
					((ServerWorld)world).spawnParticles((ServerPlayerEntity)self, ParticleTypes.CLOUD, true, vec.x, vec.y, vec.z, 0, 0, 1, 0, 0.05);
					((ServerWorld)world).spawnParticles(ParticleTypes.CLOUD, vec.x, vec.y, vec.z, 0, 0, 1, 0, 0.05);
				}
			}
			fabrication$debted = true;
			cir.setReturnValue(false);
		}
	}
	
	@Inject(at = @At("TAIL"), method = "writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void writeCustomDataToTag(NbtCompound tag, CallbackInfo ci) {
		if (fabrication$lastGroundPos != null) {
			Vec3d pos = fabrication$lastGroundPos;
			tag.putDouble("fabrication:LastGroundPosX", pos.x);
			tag.putDouble("fabrication:LastGroundPosY", pos.y);
			tag.putDouble("fabrication:LastGroundPosZ", pos.z);
		}
		if (fabrication$debted) {
			tag.putBoolean("fabrication:Debted", fabrication$debted);
		}
	}
	
	@Inject(at = @At("TAIL"), method = "readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void readCustomDataFromTag(NbtCompound tag, CallbackInfo ci) {
		if (tag.contains("fabrication:LastGroundPosX")) {
			fabrication$lastGroundPos = new Vec3d(
					tag.getDouble("fabrication:LastGroundPosX"),
					tag.getDouble("fabrication:LastGroundPosY"),
					tag.getDouble("fabrication:LastGroundPosZ")
			);
		}
		fabrication$debted = tag.getBoolean("fabrication:Debted");
	}
	
}
