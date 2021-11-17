package com.unascribed.fabrication.mixin.a_fixes.sync_attacker_yaw;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.util.Identifier;

@Mixin(ClientPlayNetworkHandler.class)
@EligibleIf(configAvailable="*.sync_attacker_yaw", envMatches=Env.CLIENT)
public class MixinClientPlayNetworkHandler {

	@Shadow @Final
	private ClientConnection connection;

	@Inject(at=@At("TAIL"), method="onGameJoin(Lnet/minecraft/network/packet/s2c/play/GameJoinS2CPacket;)V")
	public void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
		// this *should* use minecraft:register but it is unclear what format that packet is
		// intended to have in an Identifier-based world; Dinnerbone's original post says
		// "NUL-delimited strings" but that's kind of silly with identifiers
		////
		// instead we just send an empty packet on our channel
		// i don't think anyone has used the REGISTER channel for years, honestly
		connection.send(new CustomPayloadC2SPacket(new Identifier("fabrication", "attacker_yaw"), new PacketByteBuf(Unpooled.buffer())));
	}

	@Inject(at=@At("HEAD"), method="onCustomPayload(Lnet/minecraft/network/packet/s2c/play/CustomPayloadS2CPacket;)V", cancellable=true)
	public void onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
		if (packet.getChannel().getNamespace().equals("fabrication") && packet.getChannel().getPath().equals("attacker_yaw")) {
			if (MixinConfigPlugin.isEnabled("*.sync_attacker_yaw")) {
				if (MinecraftClient.getInstance().world != null) {
					PacketByteBuf buf = packet.getData();
					Entity e = MinecraftClient.getInstance().world.getEntityById(buf.readInt());
					if (e instanceof LivingEntity) {
						((LivingEntity)e).knockbackVelocity = buf.readFloat();
					}
				}
			}
			ci.cancel();
		}
	}

}
