package com.unascribed.fabrication.mixin._general.sync;

import java.util.Map;
import java.util.Set;

import com.unascribed.fabrication.client.OptionalFScriptScreen;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.interfaces.GetServerConfig;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.ResolvedConfigValue;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.netty.buffer.Unpooled;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.util.Identifier;

@Mixin(ClientPlayNetworkHandler.class)
@EligibleIf(envMatches=Env.CLIENT)
public class MixinClientPlayNetworkHandler implements GetServerConfig {
	
	@Shadow @Final
	private ClientConnection connection;

	@Shadow @Final private MinecraftClient client;
	private boolean fabrication$hasHandshook = false;
	private final Map<String, ResolvedConfigValue> fabrication$serverTrileanConfig = Maps.newHashMap();
	private final Map<String, String> fabrication$serverStringConfig = Maps.newHashMap();
	private long fabrication$launchId;
	private final Set<String> fabrication$serverFailedConfig = Sets.newHashSet();
	private final Set<String> fabrication$serverBanned = Sets.newHashSet();
	private String fabrication$serverVersion;
	
	@Inject(at=@At("TAIL"), method="onGameJoin(Lnet/minecraft/network/packet/s2c/play/GameJoinS2CPacket;)V")
	public void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
		PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
		data.writeVarInt(0);
		connection.send(new CustomPayloadC2SPacket(new Identifier("fabrication", "config"), data));
	}
	
	@Inject(at=@At("HEAD"), method="onCustomPayload(Lnet/minecraft/network/packet/s2c/play/CustomPayloadS2CPacket;)V", cancellable=true)
	public void onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
		if (packet.getChannel().getNamespace().equals("fabrication")) {
			if (packet.getChannel().getPath().equals("config")) {
				try {
					fabrication$hasHandshook = true;
					PacketByteBuf buf = packet.getData();
					int trileanKeys = buf.readVarInt();
					for (int i = 0; i < trileanKeys; i++) {
						String k = buf.readString(32767);
						int v = buf.readUnsignedByte();
						fabrication$serverTrileanConfig.put(k, ResolvedConfigValue.values()[v]);
					}
					int stringKeys = buf.readVarInt();
					for (int i = 0; i < stringKeys; i++) {
						String k = buf.readString(32767);
						String v = buf.readString(32767);
						fabrication$serverStringConfig.put(k, v);
					}
					if (buf.isReadable(8)) {
						fabrication$launchId = buf.readLong();
						if (buf.isReadable()) {
							fabrication$serverVersion = buf.readString(32767);
							fabrication$serverFailedConfig.clear();
							int failedKeys = buf.readVarInt();
							for (int i = 0; i < failedKeys; i++) {
								fabrication$serverFailedConfig.add(buf.readString(32767));
							}
						} else {
							fabrication$serverVersion = "1.2.11 or earlier";
						}
					} else if (fabrication$launchId == 0) {
						fabrication$launchId = hashCode() * 31L;
						fabrication$serverVersion = "1.2 or earlier";
					}
					if (buf.isReadable()) {
						fabrication$serverBanned.clear();
						int bannedKeys = buf.readVarInt();
						for (int i = 0; i < bannedKeys; i++) {
							String k = buf.readString(32767);
							fabrication$serverBanned.add(k);
						}
					}
					ci.cancel();
				} catch (RuntimeException e) {
					e.printStackTrace();
					throw e;
				}
			}else if (packet.getChannel().getPath().equals("fscript")){
				try{
					PacketByteBuf buf = packet.getData();
					int code = buf.readVarInt();
					if (code == 0){
						if (client.currentScreen instanceof OptionalFScriptScreen){
							((OptionalFScriptScreen) client.currentScreen).fabrication$setScript(buf.readString());
						}
					}
					ci.cancel();
				}catch (RuntimeException e) {
					e.printStackTrace();
					throw e;
				}
			}
		}
	}

	@Override
	public boolean fabrication$hasHandshook() {
		return fabrication$hasHandshook;
	}
	
	@Override
	public Map<String, ResolvedConfigValue> fabrication$getServerTrileanConfig() {
		return fabrication$serverTrileanConfig;
	}
	
	@Override
	public Map<String, String> fabrication$getServerStringConfig() {
		return fabrication$serverStringConfig;
	}
	
	@Override
	public long fabrication$getLaunchId() {
		return fabrication$launchId;
	}
	
	@Override
	public Set<String> fabrication$getServerFailedConfig() {
		return fabrication$serverFailedConfig;
	}
	
	@Override
	public String fabrication$getServerVersion() {
		return fabrication$serverVersion;
	}
	
	@Override
	public Set<String> fabrication$getServerBanned() {
		return fabrication$serverBanned;
	}
	
}
