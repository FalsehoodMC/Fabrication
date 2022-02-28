package com.unascribed.fabrication.mixin.b_utility.despawning_items_blink;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.util.Identifier;

@Mixin(ClientPlayNetworkHandler.class)
@EligibleIf(configAvailable="*.despawning_items_blink", envMatches=Env.CLIENT)
public class MixinClientPlayNetworkHandler {

	@Shadow @Final
	private ClientConnection connection;

	@Inject(at=@At("TAIL"), method="onGameJoin(Lnet/minecraft/network/packet/s2c/play/GameJoinS2CPacket;)V")
	public void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
		connection.send(new CustomPayloadC2SPacket(new Identifier("fabrication", "item_despawn"), new PacketByteBuf(Unpooled.buffer())));
	}

	@Inject(at=@At("HEAD"), method="onCustomPayload(Lnet/minecraft/network/packet/s2c/play/CustomPayloadS2CPacket;)V", cancellable=true)
	public void onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
		if (packet.getChannel().getNamespace().equals("fabrication") && packet.getChannel().getPath().equals("item_despawn")) {
			if (FabConf.isEnabled("*.despawning_items_blink")) {
				if (MinecraftClient.getInstance().world != null) {
					PacketByteBuf buf = packet.getData();
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
