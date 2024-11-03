package com.unascribed.fabrication.mixin.e_mechanics.obsidian_tears;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;

@Mixin(ServerPlayerEntity.class)
@EligibleIf(configAvailable="*.obsidian_tears")
public abstract class MixinServerPlayerEntity extends PlayerEntity {

	public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@FabInject(at=@At("TAIL"), method="copyFrom(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V")
	public void copyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.obsidian_tears")) return;
		if (!alive) {
			ServerPlayerEntity self = (ServerPlayerEntity)(Object)this;
			if (oldPlayer.getSpawnPointPosition() != null && getWorld().getRegistryKey().equals(oldPlayer.getSpawnPointDimension())
					&& getWorld().getBlockState(oldPlayer.getSpawnPointPosition()).getBlock() == Blocks.CRYING_OBSIDIAN) {
				NbtCompound hunger = new NbtCompound();
				self.getHungerManager().writeNbt(hunger);
				hunger.putFloat("foodSaturationLevel", 0);
				hunger.putInt("foodLevel", 15);
				self.getHungerManager().readNbt(hunger);
				self.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 15*20, 0));
				self.setHealth(getHealth()*0.5f);
			}
		}
	}

	@FabInject(at=@At("HEAD"), method="findRespawnPosition(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;FZZ)Ljava/util/Optional;",
		cancellable=true)
	private static void findRespawnPosition(ServerWorld world, BlockPos pos, float f, boolean b, boolean b2, CallbackInfoReturnable<Optional<Vec3d>> ci) {
		if (!FabConf.isEnabled("*.obsidian_tears")) return;
		BlockState state = world.getBlockState(pos);
		Block bl = state.getBlock();
		if (bl == Blocks.CRYING_OBSIDIAN) {
			BlockState bs;
			if ((bs = world.getBlockState(pos.up())).getBlock().canMobSpawnInside(bs) && (bs = world.getBlockState(pos.up().up())).getBlock().canMobSpawnInside(bs)) {
				ci.setReturnValue(Optional.of(new Vec3d(pos.getX()+0.5, pos.getY()+1, pos.getZ()+0.5)));
			} else {
				Optional<Vec3d> attempt = RespawnAnchorBlock.findRespawnPosition(EntityType.PLAYER, world, pos);
				if (attempt.isPresent()) {
					ci.setReturnValue(attempt);
				}
			}
		}
	}

}
