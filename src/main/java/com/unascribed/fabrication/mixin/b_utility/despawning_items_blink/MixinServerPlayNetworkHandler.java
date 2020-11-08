package com.unascribed.fabrication.mixin.b_utility.despawning_items_blink;

import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.interfaces.SetAttackerYawAware;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

@Mixin(ServerPlayNetworkHandler.class)
@EligibleIf(configEnabled="*.despawning_items_blink")
public class MixinServerPlayNetworkHandler {
	
	@Shadow
	public ServerPlayerEntity player;
	
	@Inject(at=@At("HEAD"), method="onCustomPayload(Lnet/minecraft/network/packet/c2s/play/CustomPayloadC2SPacket;)V", cancellable=true)
	public void onCustomPayload(CustomPayloadC2SPacket packet, CallbackInfo ci) {
		Identifier channel = packet.channel;
		if (channel.getNamespace().equals("fabrication") && channel.getPath().equals("item_despawn")) {
			if (RuntimeChecks.check("*.sync_attacker_yaw") && player instanceof SetAttackerYawAware) {
				LogManager.getLogger("Fabrication").debug("Enabling item despawn syncing for "+player.getEntityName());
				((SetAttackerYawAware)player).fabrication$setAttackerYawAware(true);
			}
			ci.cancel();
		}
	}
	
}
