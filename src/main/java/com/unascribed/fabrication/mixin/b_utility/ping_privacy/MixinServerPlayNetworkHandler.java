package com.unascribed.fabrication.mixin.b_utility.ping_privacy;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.logic.PingPrivacyPersistentState;
import com.unascribed.fabrication.support.EligibleIf;

import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;

@Mixin(ServerPlayNetworkHandler.class)
@EligibleIf(configAvailable="*.ping_privacy")
public class MixinServerPlayNetworkHandler {

	@Shadow @Final
	private MinecraftServer server;
	@Shadow @Final
	public ClientConnection connection;

	@Inject(at=@At("HEAD"), method="sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V")
	public void sendPacket(Packet<?> p, GenericFutureListener<?> l, CallbackInfo ci) {
		if (FabConf.isEnabled("*.ping_privacy") && p instanceof GameJoinS2CPacket) {
			SocketAddress addr = connection.getAddress();
			if (addr instanceof InetSocketAddress) {
				PingPrivacyPersistentState.get(server.getOverworld()).addKnownIp(((InetSocketAddress)addr).getAddress());
			}
		}
	}

}
