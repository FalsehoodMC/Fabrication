package com.unascribed.fabrication.mixin.b_utility.ping_privacy;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.logic.PingPrivacyPersistentState;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Mixin(ServerPlayNetworkHandler.class)
@EligibleIf(configAvailable="*.ping_privacy")
public class MixinServerPlayNetworkHandler {

	@Shadow @Final
	private MinecraftServer server;
	@Shadow @Final
	public ClientConnection connection;

	@FabInject(at=@At("HEAD"), method="sendPacket(Lnet/minecraft/network/packet/Packet;)V")
	public void sendPacket(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {
		if (FabConf.isEnabled("*.ping_privacy") && packet instanceof GameJoinS2CPacket) {
			SocketAddress addr = connection.getAddress();
			if (addr instanceof InetSocketAddress) {
				PingPrivacyPersistentState.get(server.getOverworld()).addKnownIp(((InetSocketAddress)addr).getAddress());
			}
		}
	}

}
