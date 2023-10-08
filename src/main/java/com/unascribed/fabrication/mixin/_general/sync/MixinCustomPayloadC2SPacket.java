package com.unascribed.fabrication.mixin._general.sync;

import com.unascribed.fabrication.util.ByteBufCustomPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CustomPayloadC2SPacket.class)
public class MixinCustomPayloadC2SPacket {

	@Inject(method="readPayload(Lnet/minecraft/util/Identifier;Lnet/minecraft/network/PacketByteBuf;)Lnet/minecraft/network/packet/CustomPayload;", at=@At("HEAD"), cancellable=true)
	private static void oldPayload(Identifier id, PacketByteBuf buf, CallbackInfoReturnable<CustomPayload> cir){
		if ("fabrication".equals(id.getNamespace())) {
			cir.setReturnValue(new ByteBufCustomPayload(buf));
		}
	}
}
