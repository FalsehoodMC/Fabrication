package com.unascribed.fabrication.mixin.b_utility.hide_armor;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.interfaces.GetSuppressedSlots;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.EquipmentSlot.Type;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;

@Mixin(ClientPlayNetworkHandler.class)
@EligibleIf(configEnabled="*.hide_armor", envMatches=Env.CLIENT)
public class MixinClientPlayNetworkHandler {
	
	@Shadow @Final
	private ClientConnection connection;
	
	@Inject(at=@At("HEAD"), method="onCustomPayload(Lnet/minecraft/network/packet/s2c/play/CustomPayloadS2CPacket;)V", cancellable=true)
	public void onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
		if (packet.getChannel().getNamespace().equals("fabrication") && packet.getChannel().getPath().equals("hide_armor")) {
			PacketByteBuf buf = packet.getData();
			int bits = buf.readVarInt();
			PlayerEntity p = MinecraftClient.getInstance().player;
			if (p instanceof GetSuppressedSlots) {
				((GetSuppressedSlots)p).fabrication$getSuppressedSlots().clear();
				for (EquipmentSlot es : EquipmentSlot.values()) {
					if (es.getType() == Type.ARMOR) {
						if ((bits & (1 << es.getEntitySlotId())) != 0) {
							((GetSuppressedSlots)p).fabrication$getSuppressedSlots().add(es);
						}
					}
				}
			}
			ci.cancel();
		}
	}
	
}
