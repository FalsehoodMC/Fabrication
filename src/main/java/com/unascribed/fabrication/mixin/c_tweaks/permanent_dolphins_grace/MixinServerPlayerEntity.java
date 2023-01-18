package com.unascribed.fabrication.mixin.c_tweaks.permanent_dolphins_grace;

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
@EligibleIf(configAvailable="*.permanent_dolphins_grace")
public abstract class MixinServerPlayerEntity extends PlayerEntity {
	private boolean fabrication$permDolphinsGrace = false;
	private static final Predicate<PlayerEntity> fabrication$permDolphinsGracePredicate = ConfigPredicates.getFinalPredicate("*.permanent_dolphins_grace");

	public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}


	@FabInject(at=@At("TAIL"), method="tick()V")
	public void tick(CallbackInfo ci) {
		if (FabConf.isEnabled("*.permanent_dolphins_grace") && fabrication$permDolphinsGracePredicate.test(this)) {
			if (!fabrication$permDolphinsGrace) fabrication$permDolphinsGrace = true;
			if (EffectNeedsReplacing.needsReplacing(this, StatusEffects.DOLPHINS_GRACE)) {
				addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, Integer.MAX_VALUE, 0, true, false));
			}
		} else if (fabrication$permDolphinsGrace) {
			fabrication$permDolphinsGrace = false;
			removeStatusEffect(StatusEffects.DOLPHINS_GRACE);
		}
	}

	@FabInject(at=@At("TAIL"), method="writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void writeCustomDataToTag(NbtCompound tag, CallbackInfo ci) {
		tag.putBoolean("fabrication:permanent_dolphins_grace", fabrication$permDolphinsGrace);
	}

	@FabInject(at=@At("TAIL"), method="readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void readCustomDataFromTag(NbtCompound tag, CallbackInfo ci) {
		fabrication$permDolphinsGrace = tag.getBoolean("fabrication:permanent_dolphins_grace");
	}

}
