package com.unascribed.fabrication.mixin.b_utility.ping_privacy;

import com.mojang.authlib.GameProfile;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.SetServerAware;
import com.unascribed.fabrication.logic.PingPrivacy;
import com.unascribed.fabrication.logic.PingPrivacyPersistentState;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.ServerMetadata.Players;
import net.minecraft.server.ServerMetadata.Version;
import net.minecraft.server.network.ServerQueryNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Mixin(ServerQueryNetworkHandler.class)
@EligibleIf(configAvailable="*.ping_privacy")
public class MixinServerQueryNetworkHandler implements SetServerAware {

	private MinecraftServer fabrication$pingPrivacyServer;

	@Shadow @Final
	private ClientConnection connection;
	@Shadow
	private boolean responseSent;

	@Shadow @Final
	private ServerMetadata metadata;

	@FabInject(at=@At("HEAD"), method="onRequest(Lnet/minecraft/network/packet/c2s/query/QueryRequestC2SPacket;)V", cancellable=true)
	public void onRequest(QueryRequestC2SPacket p, CallbackInfo ci) {
		if (fabrication$pingPrivacyServer == null) return;
		if (FabConf.isEnabled("*.ping_privacy") && !responseSent) {
			SocketAddress sa = connection.getAddress();
			if (sa instanceof InetSocketAddress) {
				InetSocketAddress isa = (InetSocketAddress)sa;
				if (PingPrivacy.isEvil(isa.getAddress()) || !PingPrivacyPersistentState.get(fabrication$pingPrivacyServer.getOverworld()).isKnownAndRecent(isa.getAddress())) {
					ServerMetadata realData = metadata;
					Text desc;
					Version v;
					int playerCount;
					Random tlr = ThreadLocalRandom.current();
					boolean chatEnforced;
					if (PingPrivacy.isEvil(((InetSocketAddress)sa).getAddress())) {
						playerCount = tlr.nextInt(128)+128;
						v = new Version("?", 99999999);
						desc = Text.literal("A Minecraft Server");
						chatEnforced = false;
					} else {
						playerCount = 12;
						desc = Text.literal("To protect the privacy of this server and its\nusers, you must log in once to see ping data.").formatted(Formatting.ITALIC);
						v = new Version("§7?§8/§7"+realData.players().map(Players::max).orElse(0), 99999999);
						chatEnforced = realData.secureChatEnforced();
					}
					GameProfile[] sample = new GameProfile[playerCount];
					StringBuilder sb = new StringBuilder(16);
					for (int i = 0; i < sample.length; i++) {
						UUID id = new UUID(tlr.nextLong(), tlr.nextLong());
						sb.setLength(0);
						PingPrivacy.generateBelievableUsername(tlr, sb);
						sample[i] = new GameProfile(id, sb.toString());
					}
					Players players = new Players(realData.players().map(Players::max).orElse(0), playerCount, List.of(sample));
					this.connection.send(new QueryResponseS2CPacket(new ServerMetadata(desc, Optional.of(players), Optional.of(v), Optional.empty(), chatEnforced)));
					responseSent = true;
					ci.cancel();
				}
			}
		}
	}

	@Override
	public void fabrication$pingSetServer(MinecraftServer server) {
		fabrication$pingPrivacyServer = server;
	}
}
