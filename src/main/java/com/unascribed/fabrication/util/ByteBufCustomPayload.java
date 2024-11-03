package com.unascribed.fabrication.util;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ByteBufCustomPayload(Identifier id, PacketByteBuf buf) implements CustomPayload {

	public static final Id<ByteBufCustomPayload> ID = new Id<>(Identifier.of("fabrication", "bytebuf"));
	public static final PacketCodec<PacketByteBuf, ByteBufCustomPayload> CODEC = PacketCodec.ofStatic(ByteBufCustomPayload::write, ByteBufCustomPayload::new);

	public ByteBufCustomPayload(PacketByteBuf buf) {
		this(buf.readIdentifier(), buf2buf(buf));
	}

	private static void write(PacketByteBuf buf, ByteBufCustomPayload payload) {
		buf.writeIdentifier(payload.id);
		buf.writeByteArray(payload.buf.capacity(payload.buf.readableBytes()).array());
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	private static PacketByteBuf buf2buf(PacketByteBuf buf) {
		byte[] arr = buf.readByteArray();
		return new PacketByteBuf(Unpooled.buffer(arr.length)).writeBytes(arr);
	}
}
