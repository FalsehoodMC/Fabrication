package com.unascribed.fabrication.util;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class ByteBufCustomPayload implements CustomPayload {
	public Identifier id = null;
	public PacketByteBuf buf = null;

	public ByteBufCustomPayload(Identifier id, PacketByteBuf buf) {
		this.id = id;
		this.buf = buf;
	}

	public ByteBufCustomPayload(PacketByteBuf buf) {
		this.id = buf.readIdentifier();
		byte[] arr = buf.readByteArray();
		this.buf = new PacketByteBuf(Unpooled.buffer(arr.length));
		this.buf.writeBytes(arr);
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeIdentifier(this.id);
		buf.writeByteArray(this.buf.capacity(this.buf.readableBytes()).array());
	}

	@Override
	public Identifier id() {
		return id;
	}

}
