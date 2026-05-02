package com.unascribed.fabrication.mixin._general.fapi;

import com.unascribed.fabrication.FabricationClientCommands;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
@EligibleIf(envMatches=Env.CLIENT)
public class MixinClientPlayerEntity {
	@FabInject(method="sendChatMessage(Ljava/lang/String;)V", at=@At("HEAD"), cancellable=true)
	private void runFabricationCommand(String message, CallbackInfo info) {
		if (FabricationClientCommands.runCommand(message)) {
			info.cancel();
		}
	}
}
