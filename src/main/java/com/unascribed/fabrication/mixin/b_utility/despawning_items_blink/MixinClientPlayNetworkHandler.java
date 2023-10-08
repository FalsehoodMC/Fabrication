package com.unascribed.fabrication.mixin.b_utility.despawning_items_blink;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.util.ByteBufCustomPayload;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.interfaces.RenderingAgeAccess;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.util.Identifier;

@Mixin(ClientPlayNetworkHandler.class)
@EligibleIf(configAvailable="*.despawning_items_blink", envMatches=Env.CLIENT)
public abstract class MixinClientPlayNetworkHandler extends ClientCommonNetworkHandler {

	protected MixinClientPlayNetworkHandler(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
		super(client, connection, connectionState);
	}

	@FabInject(at=@At("TAIL"), method="onGameJoin(Lnet/minecraft/network/packet/s2c/play/GameJoinS2CPacket;)V")
	public void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
		connection.send(new CustomPayloadC2SPacket(new ByteBufCustomPayload(new Identifier("fabrication", "item_despawn"), new PacketByteBuf(Unpooled.buffer()))));
	}

	@FabInject(at=@At("HEAD"), method="onCustomPayload(Lnet/minecraft/network/packet/CustomPayload;)V", cancellable=true)
	public void onCustomPayload(CustomPayload payload, CallbackInfo ci) {
		if (!(payload instanceof ByteBufCustomPayload)) return;
		Identifier channel = payload.id();
		if (channel == null) return;
		if (channel.getNamespace().equals("fabrication") && channel.getPath().equals("item_despawn")) {
			if (FabConf.isEnabled("*.despawning_items_blink")) {
				if (MinecraftClient.getInstance().world != null) {
					PacketByteBuf buf = ((ByteBufCustomPayload) payload).buf;
					Entity e = MinecraftClient.getInstance().world.getEntityById(buf.readInt());
					if (e instanceof ItemEntity && e instanceof RenderingAgeAccess) {
						((RenderingAgeAccess)e).fabrication$setRenderingAge(buf.readInt());
					}
				}
			}
			ci.cancel();
		}
	}

}
