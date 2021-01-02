package com.unascribed.fabrication.mixin.i_woina.oof;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(PlayerEntity.class)
@EligibleIf(configEnabled="*.oof", envMatches=Env.CLIENT)
public abstract class MixinPlayerEntity {

	@Shadow
	public abstract GameProfile getGameProfile();
	
	@Inject(at=@At("HEAD"), method="getHurtSound(Lnet/minecraft/entity/damage/DamageSource;)Lnet/minecraft/sound/SoundEvent;",
			cancellable=true)
	public void getHurtSound(DamageSource src, CallbackInfoReturnable<SoundEvent> ci) {
		if (!RuntimeChecks.check("*.oof") || !((PlayerEntity)(Object)this).world.isClient) return;
		if (src == DamageSource.DROWN) {
			ci.setReturnValue(SoundEvents.ENTITY_PLAYER_HURT_DROWN);
		} else {
			Map<Type, MinecraftProfileTexture> skinMap = MinecraftClient.getInstance().getSkinProvider().getTextures(getGameProfile());
			if (skinMap != null && skinMap.get(Type.SKIN) != null && "slim".equals(skinMap.get(Type.SKIN).getMetadata("model"))) {
				ci.setReturnValue(FabricationMod.OOF_ALEX);
			} else {
				ci.setReturnValue(FabricationMod.OOF);
			}
		}
	}
	
}
