package com.unascribed.fabrication.mixin.b_utility.chat_markdown;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabModifyArg;
import com.unascribed.fabrication.util.Markdown;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayNetworkHandler.class)
@EligibleIf(configAvailable="*.chat_markdown")
public class MixinServerPlayNetworkHandler {

	@FabModifyArg(at=@At(value="INVOKE", target="Lnet/minecraft/server/network/ServerPlayNetworkHandler;filterText(Ljava/lang/String;Ljava/util/function/Consumer;)V"),
			method="onChatMessage(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;)V")
	public String consume(String in) {
		if (!FabConf.isEnabled("*.chat_markdown")) return in;
		return Markdown.convert(in);
	}

}
