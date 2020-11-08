package com.unascribed.fabrication.mixin._general.sync;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.interfaces.SetFabricationConfigAware;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.GameRules;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinServerPlayNetworkHandler {
	
	@Shadow
	public ServerPlayerEntity player;
	
	@Inject(at=@At("HEAD"), method="onCustomPayload(Lnet/minecraft/network/packet/c2s/play/CustomPayloadC2SPacket;)V", cancellable=true)
	public void onCustomPayload(CustomPayloadC2SPacket packet, CallbackInfo ci) {
		Identifier channel = packet.channel;
		if (channel.getNamespace().equals("fabrication") && channel.getPath().equals("config")) {
			PacketByteBuf recvdData = packet.data;
			int id = recvdData.readVarInt();
			if (id == 0) {
				// hello
				if (player instanceof SetFabricationConfigAware) {
					((SetFabricationConfigAware)player).fabrication$setConfigAware(true);
					FabricationMod.sendConfigUpdate(player.server, null, player);
				}
			} else if (id == 1) {
				// set
				if (player.hasPermissionLevel(4)) {
					String key = recvdData.readString(32767);
					if (MixinConfigPlugin.isValid(key)) {
						String value = recvdData.readString(32767);
						MixinConfigPlugin.set(key, value);
						if (FabricationMod.isAvailableFeature(key)) {
							FabricationMod.updateFeature(key);
						}
						FabricationMod.sendConfigUpdate(player.server, key);
						Text text = new TranslatableText("chat.type.admin", player.getDisplayName(), new LiteralText(key+" is now set to "+value))
								.formatted(Formatting.GRAY, Formatting.ITALIC);
						if (player.server.getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
							for (ServerPlayerEntity spe : player.server.getPlayerManager().getPlayerList()) {
								if (player.server.getPlayerManager().isOperator(spe.getGameProfile())) {
									spe.sendSystemMessage(text, Util.NIL_UUID);
								}
							}
						}
						if (player.server.getGameRules().getBoolean(GameRules.LOG_ADMIN_COMMANDS)) {
							player.server.sendSystemMessage(text, Util.NIL_UUID);
						}
					}
				}
			}
			ci.cancel();
		}
	}
	
}
