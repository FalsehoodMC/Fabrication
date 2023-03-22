package com.unascribed.fabrication.mixin._general.fapi;

import com.mojang.brigadier.CommandDispatcher;
import com.unascribed.fabrication.FabricationClientCommands;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
@EligibleIf(envMatches=Env.CLIENT)
public class MixinClientPlayNetworkHandler {
	@Shadow
	private CommandDispatcher<CommandSource> commandDispatcher;

	@FabInject(method="onCommandTree(Lnet/minecraft/network/packet/s2c/play/CommandTreeS2CPacket;)V", at=@At("TAIL"))
	private void addClientSuggestions(CommandTreeS2CPacket packet, CallbackInfo info) {
		FabricationClientCommands.addSuggestions(commandDispatcher);
	}

	@FabInject(method="sendChatCommand(Ljava/lang/String;)V", at=@At("HEAD"), cancellable=true)
	private void onSendCommand(String command, CallbackInfo info) {
		if (FabricationClientCommands.runCommand(command)) {
			info.cancel();
		}
	}
}
