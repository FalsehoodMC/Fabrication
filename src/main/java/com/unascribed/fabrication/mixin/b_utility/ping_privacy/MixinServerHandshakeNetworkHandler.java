package com.unascribed.fabrication.mixin.b_utility.ping_privacy;

import com.unascribed.fabrication.interfaces.SetServerAware;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabModifyArg;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerHandshakeNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerHandshakeNetworkHandler.class)
@EligibleIf(configAvailable="*.ping_privacy")
public class MixinServerHandshakeNetworkHandler {


	@Shadow @Final
	private MinecraftServer server;

	@FabModifyArg(at=@At(value="INVOKE", target="Lnet/minecraft/network/ClientConnection;setPacketListener(Lnet/minecraft/network/listener/PacketListener;)V"), method="onHandshake(Lnet/minecraft/network/packet/c2s/handshake/HandshakeC2SPacket;)V")
	public PacketListener onRequest(PacketListener listener) {
		if (listener instanceof SetServerAware) ((SetServerAware) listener).fabrication$pingSetServer(server);
		return listener;
	}
}
