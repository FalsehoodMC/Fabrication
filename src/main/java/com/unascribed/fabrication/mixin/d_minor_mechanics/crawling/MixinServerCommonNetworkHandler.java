package com.unascribed.fabrication.mixin.d_minor_mechanics.crawling;

import com.unascribed.fabrication.util.ByteBufCustomPayload;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.interfaces.SetCrawling;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

@Mixin(ServerCommonNetworkHandler.class)
@EligibleIf(configAvailable="*.crawling")
public class MixinServerCommonNetworkHandler {

	@FabInject(at=@At("HEAD"), method="onCustomPayload(Lnet/minecraft/network/packet/c2s/common/CustomPayloadC2SPacket;)V", cancellable=true)
	public void onCustomPayload(CustomPayloadC2SPacket packet, CallbackInfo ci) {
		Object self = this;
		if (!(self instanceof ServerPlayNetworkHandler)) return;
		ServerPlayerEntity player = ((ServerPlayNetworkHandler) self).getPlayer();
		CustomPayload payload = packet.payload();
		if (!(payload instanceof ByteBufCustomPayload)) return;
		Identifier channel = payload.id();
		if (channel.getNamespace().equals("fabrication") && channel.getPath().equals("crawling")) {
			PacketByteBuf recvdData = ((ByteBufCustomPayload) payload).buf;
			boolean crawling = recvdData.readBoolean();
			if (player instanceof SetCrawling) {
				((SetCrawling)player).fabrication$setCrawling(crawling);
			}
			ci.cancel();
		}
	}

}
