package com.unascribed.fabrication.mixin.b_utility.ping_privacy;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;
import com.unascribed.fabrication.logic.PingPrivacy;
import com.unascribed.fabrication.logic.PingPrivacyPersistentState;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.ServerMetadata.Players;
import net.minecraft.server.ServerMetadata.Version;
import net.minecraft.server.network.ServerQueryNetworkHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

@Mixin(ServerQueryNetworkHandler.class)
@EligibleIf(configAvailable="*.ping_privacy")
public class MixinServerQueryNetworkHandler {

	@Shadow @Final
	private MinecraftServer server;
	@Shadow @Final
	private ClientConnection connection;
	@Shadow
	private boolean responseSent;

	@Inject(at=@At("HEAD"), method="onRequest(Lnet/minecraft/network/packet/c2s/query/QueryRequestC2SPacket;)V", cancellable=true)
	public void onRequest(QueryRequestC2SPacket p, CallbackInfo ci) {
		if (FabConf.isEnabled("*.ping_privacy") && !responseSent) {
			SocketAddress sa = connection.getAddress();
			if (sa instanceof InetSocketAddress) {
				InetSocketAddress isa = (InetSocketAddress)sa;
				if (PingPrivacy.isEvil(isa.getAddress()) || !PingPrivacyPersistentState.get(server.getOverworld()).isKnownAndRecent(isa.getAddress())) {
					ServerMetadata realData = server.getServerMetadata();
					ServerMetadata junkData = new ServerMetadata();
					Version v;
					int playerCount;
					Random tlr = ThreadLocalRandom.current();
					if (PingPrivacy.isEvil(((InetSocketAddress)sa).getAddress())) {
						playerCount = tlr.nextInt(128)+128;
						v = new Version("?", 99999999);
						junkData.setDescription(new LiteralText("A Minecraft Server"));
					} else {
						playerCount = 12;
						junkData.setDescription(new LiteralText("To protect the privacy of this server and its\nusers, you must log in once to see ping data.").formatted(Formatting.ITALIC));
						v = new Version("§7?§8/§7"+realData.getPlayers().getPlayerLimit(), 99999999);
					}
					GameProfile[] sample = new GameProfile[playerCount];
					StringBuilder sb = new StringBuilder(16);
					for (int i = 0; i < sample.length; i++) {
						UUID id = new UUID(tlr.nextLong(), tlr.nextLong());
						sb.setLength(0);
						PingPrivacy.generateBelievableUsername(tlr, sb);
						sample[i] = new GameProfile(id, sb.toString());
					}
					junkData.setVersion(v);
					Players players = new Players(realData.getPlayers().getPlayerLimit(), playerCount);
					players.setSample(sample);
					junkData.setPlayers(players);
					this.connection.send(new QueryResponseS2CPacket(junkData));
					responseSent = true;
					ci.cancel();
				}
			}
		}
	}

}
