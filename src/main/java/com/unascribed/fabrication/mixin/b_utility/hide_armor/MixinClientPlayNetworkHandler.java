package com.unascribed.fabrication.mixin.b_utility.hide_armor;

import com.unascribed.fabrication.util.ByteBufCustomPayload;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.network.packet.CustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
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

@Mixin(ClientPlayNetworkHandler.class)
@EligibleIf(configAvailable="*.hide_armor", envMatches=Env.CLIENT)
public abstract class MixinClientPlayNetworkHandler extends ClientCommonNetworkHandler {

	protected MixinClientPlayNetworkHandler(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
		super(client, connection, connectionState);
	}

	@FabInject(at=@At("HEAD"), method="onCustomPayload(Lnet/minecraft/network/packet/CustomPayload;)V", cancellable=true)
	public void onCustomPayload(CustomPayload payload, CallbackInfo ci) {
		if (!(payload instanceof ByteBufCustomPayload)) return;
		if (payload.id().getNamespace().equals("fabrication") && payload.id().getPath().equals("hide_armor")) {
			PacketByteBuf buf = ((ByteBufCustomPayload) payload).buf;
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
