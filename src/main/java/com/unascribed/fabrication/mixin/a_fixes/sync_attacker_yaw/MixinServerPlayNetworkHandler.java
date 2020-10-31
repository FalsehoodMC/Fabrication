package com.unascribed.fabrication.mixin.a_fixes.sync_attacker_yaw;

import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.interfaces.SetAttackerYawAware;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

@Mixin(ServerPlayNetworkHandler.class)
@EligibleIf(configEnabled="*.sync_attacker_yaw")
public class MixinServerPlayNetworkHandler {
	
	@Shadow
	public ServerPlayerEntity player;
	
	@Inject(at=@At("HEAD"), method="onCustomPayload(Lnet/minecraft/network/packet/c2s/play/CustomPayloadC2SPacket;)V", cancellable=true)
	public void onCustomPayload(CustomPayloadC2SPacket packet, CallbackInfo ci) {
		Identifier channel = FabricationMod.snag(CustomPayloadC2SPacket.class, packet, "field_12830", "channel");
		if (channel.getNamespace().equals("fabrication") && channel.getPath().equals("attacker_yaw")) {
			if (RuntimeChecks.check("*.sync_attacker_yaw") && player instanceof SetAttackerYawAware) {
				LogManager.getLogger("Fabrication").debug("Enabling attacker yaw syncing for "+player.getEntityName());
				((SetAttackerYawAware)player).fabrication$setAttackerYawAware(true);
			}
			ci.cancel();
		}
	}
	
}
