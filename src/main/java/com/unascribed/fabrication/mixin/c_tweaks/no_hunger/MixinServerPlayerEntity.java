package com.unascribed.fabrication.mixin.c_tweaks.no_hunger;

import com.mojang.authlib.GameProfile;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.SetSaturation;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
@EligibleIf(configAvailable="*.no_hunger")
public abstract class MixinServerPlayerEntity extends PlayerEntity {

	public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile, @Nullable PlayerPublicKey publicKey) {
		super(world, pos, yaw, gameProfile, publicKey);
	}

	@FabInject(at=@At("TAIL"), method="tick()V")
	public void tick(CallbackInfo ci) {
		if (FabConf.isEnabled("*.no_hunger") && ConfigPredicates.shouldRun("*.no_hunger", (PlayerEntity)this)) {
			getHungerManager().setFoodLevel(hasStatusEffect(StatusEffects.HUNGER) ? 0 : getHealth() >= getMaxHealth() ? 20 : 17);
			// prevent the hunger bar from jiggling
			((SetSaturation)getHungerManager()).fabrication$setSaturation(10);
		}
	}

}
