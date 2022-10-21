package com.unascribed.fabrication.mixin.c_tweaks.can_breathe_water;

import com.mojang.authlib.GameProfile;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(ServerPlayerEntity.class)
@EligibleIf(configAvailable="*.can_breathe_water")
public abstract class MixinServerPlayerEntity extends PlayerEntity {

	public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile, PlayerPublicKey publicKey) {
		super(world, pos, yaw, gameProfile, publicKey);
	}

	private static final Predicate<PlayerEntity> fabrication$canBreatheWaterPredicate = ConfigPredicates.getFinalPredicate("*.can_breathe_water");
	@FabInject(at=@At("TAIL"), method="tick()V")
	public void tick(CallbackInfo ci) {
		if (FabConf.isEnabled("*.can_breathe_water") && fabrication$canBreatheWaterPredicate.test(this)) {
			setAir(getMaxAir());
		}
	}

}
