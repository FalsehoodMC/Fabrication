package com.unascribed.fabrication.mixin.b_utility.despawning_items_blink;

import com.unascribed.fabrication.interfaces.SetItemDespawnAware;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.FabRefl;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

@Mixin(ServerPlayNetworkHandler.class)
@EligibleIf(configAvailable="*.despawning_items_blink")
public class MixinServerPlayNetworkHandler {

	@Shadow
	public ServerPlayerEntity player;

	@FabInject(at=@At("HEAD"), method="onCustomPayload(Lnet/minecraft/network/packet/c2s/play/CustomPayloadC2SPacket;)V", cancellable=true)
	public void onCustomPayload(CustomPayloadC2SPacket packet, CallbackInfo ci) {
		Identifier channel = FabRefl.getChannel(packet);
		if (channel.getNamespace().equals("fabrication") && channel.getPath().equals("item_despawn")) {
			if (player instanceof SetItemDespawnAware) {
				FabLog.debug("Enabling item despawn syncing for "+player.getEntityName());
				((SetItemDespawnAware)player).fabrication$setItemDespawnAware(true);
			}
			ci.cancel();
		}
	}

}
