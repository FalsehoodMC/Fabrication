package com.unascribed.fabrication.mixin._general.sync;

import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.FeaturesFile;
import com.unascribed.fabrication.loaders.LoaderFScript;
import com.unascribed.fabrication.support.OptionalFScript;
import io.netty.buffer.Unpooled;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.FabRefl;
import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.features.FeatureHideArmor;
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
		Identifier channel = FabRefl.getChannel(packet);
		if (channel.getNamespace().equals("fabrication")) {
			if (channel.getPath().equals("config")) {
				PacketByteBuf recvdData = FabRefl.getData(packet);
				int id = recvdData.readVarInt();
				if (id == 0) {
					// hello
					if (player instanceof SetFabricationConfigAware) {
						((SetFabricationConfigAware) player).fabrication$setConfigAware(true);
						FabricationMod.sendConfigUpdate(player.server, null, player);
						if (MixinConfigPlugin.isEnabled("*.hide_armor")) {
							FeatureHideArmor.sendSuppressedSlotsForSelf(player);
						}
					}
				} else if (id == 1) {
					// set
					if (player.hasPermissionLevel(2)) {
						String key = recvdData.readString(32767);
						if (MixinConfigPlugin.isValid(key)) {
							String value = recvdData.readString(32767);
							MixinConfigPlugin.set(key, value);
							if (FabricationMod.isAvailableFeature(key)) {
								FabricationMod.updateFeature(key);
							}
							FabricationMod.sendConfigUpdate(player.server, key);
							fabrication$sendCommandFeedback(
									new TranslatableText("chat.type.admin", player.getDisplayName(), new LiteralText(key + " is now set to " + value))
											.formatted(Formatting.GRAY, Formatting.ITALIC));
						}
					}
				}
			}else if (channel.getPath().equals("fscript")) {
				PacketByteBuf recvdData = FabRefl.getData(packet);
				int id = recvdData.readVarInt();
				if(id == 0){
					// get
					String key = recvdData.readString(32767);
					if (MixinConfigPlugin.isValid(key)) {
						PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
						data.writeVarInt(0);
						data.writeString(LoaderFScript.get(key));
						player.networkHandler.sendPacket(new CustomPayloadS2CPacket(new Identifier("fabrication", "fscript"), data));
					}
				}else if (id == 1) {
					// set
					if (player.hasPermissionLevel(2)) {
						String key = MixinConfigPlugin.remap(recvdData.readString(32767));
						if (MixinConfigPlugin.isValid(key) && FeaturesFile.get(key).fscript != null) {
							String value = recvdData.readString(32767);
							if (Agnos.isModLoaded("fscript") && OptionalFScript.set(key, value, player)) {
								fabrication$sendCommandFeedback(
										new TranslatableText("chat.type.admin", player.getDisplayName(), new LiteralText(key + " script is now set to " + value))
												.formatted(Formatting.GRAY, Formatting.ITALIC));
							}
						}
					}
				}else if (id == 2) {
					// unset
					if (player.hasPermissionLevel(2)) {
						String key = MixinConfigPlugin.remap(recvdData.readString(32767));
						if (MixinConfigPlugin.isValid(key) && FeaturesFile.get(key).fscript != null && Agnos.isModLoaded("fscript")) {
							OptionalFScript.restoreDefault(key);
							fabrication$sendCommandFeedback(
									new TranslatableText("chat.type.admin", player.getDisplayName(), new LiteralText(key + " script has been unset"))
											.formatted(Formatting.GRAY, Formatting.ITALIC));
						}
					}
				}else if (id == 3) {
					// TODO currently unused
					// reload
					if (player.hasPermissionLevel(2)) {
						LoaderFScript.reload();
						if (Agnos.isModLoaded("fscript")) OptionalFScript.reload();
						fabrication$sendCommandFeedback(
								new TranslatableText("chat.type.admin", player.getDisplayName(), new LiteralText(" scripts have been reloaded"))
										.formatted(Formatting.GRAY, Formatting.ITALIC));
					}
				}
			}
			ci.cancel();
		}
	}
	public void fabrication$sendCommandFeedback(Text text){
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
