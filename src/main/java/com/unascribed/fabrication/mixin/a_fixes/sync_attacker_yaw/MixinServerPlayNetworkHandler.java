package com.unascribed.fabrication.mixin.a_fixes.sync_attacker_yaw;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.FabRefl;
import com.unascribed.fabrication.interfaces.SetAttackerYawAware;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

@Mixin(ServerPlayNetworkHandler.class)
@EligibleIf(configAvailable="*.sync_attacker_yaw")
public class MixinServerPlayNetworkHandler {

	@Shadow
	public ServerPlayerEntity player;

	@FabInject(at=@At("HEAD"), method="onCustomPayload(Lnet/minecraft/network/packet/c2s/play/CustomPayloadC2SPacket;)V", cancellable=true)
	public void onCustomPayload(CustomPayloadC2SPacket packet, CallbackInfo ci) {
		Identifier channel = FabRefl.getChannel(packet);
		if (channel.getNamespace().equals("fabrication") && channel.getPath().equals("attacker_yaw")) {
			if (FabConf.isEnabled("*.sync_attacker_yaw") && player instanceof SetAttackerYawAware) {
				FabLog.debug("Enabling attacker yaw syncing for "+player.getEntityName());
				((SetAttackerYawAware)player).fabrication$setAttackerYawAware(true);
			}
			ci.cancel();
		}
	}

}
