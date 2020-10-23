package com.unascribed.fabrication.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;

@Mixin(ClientPlayNetworkHandler.class)
@EligibleIf(configEnabled="*.sync_attacker_yaw", envMatches=Env.CLIENT)
public class MixinSyncAttackerYawClientNetwork {
	
	@Inject(at=@At("HEAD"), method="onCustomPayload(Lnet/minecraft/network/packet/s2c/play/CustomPayloadS2CPacket;)V", cancellable=true)
	public void onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
		if (packet.getChannel().getNamespace().equals("fabrication") && packet.getChannel().getPath().equals("attacker_yaw")) {
			if (RuntimeChecks.check("*.sync_attacker_yaw")) {
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
