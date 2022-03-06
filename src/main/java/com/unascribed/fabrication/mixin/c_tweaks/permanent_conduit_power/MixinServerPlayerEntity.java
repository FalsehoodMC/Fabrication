package com.unascribed.fabrication.mixin.c_tweaks.permanent_conduit_power;

import com.mojang.authlib.GameProfile;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.features.FeatureTaggablePlayers;
import com.unascribed.fabrication.loaders.LoaderTaggablePlayers;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.util.EffectNedsReplacing;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
@EligibleIf(configAvailable="*.permanent_conduit_power")
public abstract class MixinServerPlayerEntity extends PlayerEntity {

	public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile profile) {
		super(world, pos, yaw, profile);
	}

	private boolean fabrication$permConduitPower = false;

	@Inject(at=@At("TAIL"), method="tick()V")
	public void tick(CallbackInfo ci) {
		if (FabConf.isEnabled("*.permanent_conduit_power") && ConfigPredicates.shouldRun("*.permanent_conduit_power", (PlayerEntity)this)) {
			if (!fabrication$permConduitPower) fabrication$permConduitPower = true;
			if (EffectNedsReplacing.needsReplacing(this, StatusEffects.CONDUIT_POWER)) {
				addStatusEffect(new StatusEffectInstance(StatusEffects.CONDUIT_POWER, Integer.MAX_VALUE, 0, true, false));
			}
		} else if (fabrication$permConduitPower) {
			fabrication$permConduitPower = false;
			removeStatusEffect(StatusEffects.CONDUIT_POWER);
		}
	}

	@Inject(at=@At("TAIL"), method="writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void writeCustomDataToTag(NbtCompound tag, CallbackInfo ci) {
		tag.putBoolean("fabrication:permanent_conduit_power", fabrication$permConduitPower);
	}

	@Inject(at=@At("TAIL"), method="readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void readCustomDataFromTag(NbtCompound tag, CallbackInfo ci) {
		fabrication$permConduitPower = tag.getBoolean("fabrication:permanent_conduit_power");
	}

}
