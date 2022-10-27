package com.unascribed.fabrication.mixin._general.fapi;

import com.unascribed.fabrication.FabricationClientCommands;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {

	@FabInject(method="sendCommand(Ljava/lang/String;Lnet/minecraft/text/Text;)V", at=@At("HEAD"), cancellable=true)
	private void runFabricationCommand2(String message, Text p, CallbackInfo info) {
		if (FabricationClientCommands.runCommand(message)) {
			info.cancel();
		}
	}
}
