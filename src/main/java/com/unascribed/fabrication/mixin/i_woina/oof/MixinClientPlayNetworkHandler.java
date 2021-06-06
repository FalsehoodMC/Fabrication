package com.unascribed.fabrication.mixin.i_woina.oof;

import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value = ClientPlayNetworkHandler.class, priority = 1049)
@EligibleIf(configEnabled="*.oof", envMatches= Env.CLIENT)
public class MixinClientPlayNetworkHandler {

	@ModifyArgs(method= "onPlaySound(Lnet/minecraft/network/packet/s2c/play/PlaySoundS2CPacket;)V", at=@At(value="INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
	public void playSound(Args args) {
		SoundEvent event = args.get(4);
		if (MixinConfigPlugin.isEnabled("*.oof") &&
				(event.equals(SoundEvents.ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH) || event.equals(SoundEvents.ENTITY_PLAYER_HURT) ||
					event.equals(SoundEvents.ENTITY_PLAYER_HURT_ON_FIRE) || event.equals(SoundEvents.ENTITY_PLAYER_DEATH))) {
			args.set(4, FabricationMod.OOF);
		}
	}
	
}
