package com.unascribed.fabrication.mixin.i_woina.oof;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

@Mixin(value = ClientPlayNetworkHandler.class, priority = 1049)
@EligibleIf(configAvailable="*.oof", envMatches= Env.CLIENT)
public class MixinClientPlayNetworkHandler {

	@ModifyArg(method= "onPlaySound(Lnet/minecraft/network/packet/s2c/play/PlaySoundS2CPacket;)V", at=@At(value="INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
	private SoundEvent playSound(SoundEvent event) {
		if (MixinConfigPlugin.isEnabled("*.oof") &&
				(event.equals(SoundEvents.ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH) || event.equals(SoundEvents.ENTITY_PLAYER_HURT) ||
						event.equals(SoundEvents.ENTITY_PLAYER_HURT_ON_FIRE) || event.equals(SoundEvents.ENTITY_PLAYER_DEATH))) {
			return FabricationMod.OOF;
		}
		return event;
	}

}
