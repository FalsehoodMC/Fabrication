package com.unascribed.fabrication.mixin._general.sync;

import com.unascribed.fabrication.support.injection.FabModifyArg;
import com.unascribed.fabrication.util.ByteBufCustomPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CustomPayloadC2SPacket.class)
public class MixinCustomPayloadC2SPacket {

	@FabModifyArg(method="<clinit>()V", at=@At(value="INVOKE", target="Lnet/minecraft/network/packet/CustomPayload;createCodec(Lnet/minecraft/network/packet/CustomPayload$CodecFactory;Ljava/util/List;)Lnet/minecraft/network/codec/PacketCodec;"))
	private static CustomPayload.CodecFactory<PacketByteBuf> oldPayload(CustomPayload.CodecFactory<PacketByteBuf> codecFactory){
		return id -> {
			if ("fabrication".equals(id.getNamespace())) {
				return ByteBufCustomPayload.CODEC;
			}
			return codecFactory.create(id);
		};
	}
}
