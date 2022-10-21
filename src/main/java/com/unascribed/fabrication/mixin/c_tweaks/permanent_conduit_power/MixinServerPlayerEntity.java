package com.unascribed.fabrication.mixin.c_tweaks.permanent_conduit_power;

import com.mojang.authlib.GameProfile;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.util.EffectNeedsReplacing;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(ServerPlayerEntity.class)
@EligibleIf(configAvailable="*.permanent_conduit_power")
public abstract class MixinServerPlayerEntity extends PlayerEntity {

	public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile profile) {
		super(world, pos, yaw, profile);
	}

	private boolean fabrication$permConduitPower = false;
	private static final Predicate<PlayerEntity> fabrication$permanentConduitPowerPredicate = ConfigPredicates.getFinalPredicate("*.permanent_conduit_power");

	@FabInject(at=@At("TAIL"), method="tick()V")
	public void tick(CallbackInfo ci) {
		if (FabConf.isEnabled("*.permanent_conduit_power") && fabrication$permanentConduitPowerPredicate.test(this)) {
			if (!fabrication$permConduitPower) fabrication$permConduitPower = true;
			if (EffectNeedsReplacing.needsReplacing(this, StatusEffects.CONDUIT_POWER)) {
				addStatusEffect(new StatusEffectInstance(StatusEffects.CONDUIT_POWER, Integer.MAX_VALUE, 0, true, false));
			}
		} else if (fabrication$permConduitPower) {
			fabrication$permConduitPower = false;
			removeStatusEffect(StatusEffects.CONDUIT_POWER);
		}
	}

	@FabInject(at=@At("TAIL"), method="writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void writeCustomDataToTag(NbtCompound tag, CallbackInfo ci) {
		tag.putBoolean("fabrication:permanent_conduit_power", fabrication$permConduitPower);
	}

	@FabInject(at=@At("TAIL"), method="readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void readCustomDataFromTag(NbtCompound tag, CallbackInfo ci) {
		fabrication$permConduitPower = tag.getBoolean("fabrication:permanent_conduit_power");
	}

}
