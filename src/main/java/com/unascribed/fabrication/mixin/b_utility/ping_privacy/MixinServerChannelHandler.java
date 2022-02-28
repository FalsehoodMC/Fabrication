package com.unascribed.fabrication.mixin.b_utility.ping_privacy;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;

import io.netty.channel.Channel;

@Mixin(targets="net.minecraft.server.ServerNetworkIo$1")
@EligibleIf(configAvailable="*.ping_privacy")
public class MixinServerChannelHandler {

	@Inject(at=@At(value="INVOKE", target="net/minecraft/server/MinecraftServer.getRateLimit()I"),
			method="net/minecraft/server/ServerNetworkIo$1.initChannel(Lio/netty/channel/Channel;)V")
	private void onInitChannel(Channel channel, CallbackInfo ci) {
		if (FabConf.isEnabled("*.ping_privacy")) {
			channel.pipeline().remove("legacy_query");
		}
	}

}
