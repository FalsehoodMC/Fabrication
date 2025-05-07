package com.unascribed.fabrication.mixin.b_utility.chat_markdown;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabModifyArg;
import com.unascribed.fabrication.util.Markdown;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayNetworkHandler.class)
@EligibleIf(configAvailable="*.chat_markdown")
public class MixinServerPlayNetworkHandler {

	@FabModifyArg(at=@At(value= "INVOKE", target="Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V"),
			method="method_31286(Ljava/lang/String;)V")
	public Text consume(Text t) {
		if (FabConf.isEnabled("*.chat_markdown") && t instanceof TranslatableText && "chat.type.text".equals(((TranslatableText)t).getKey())) {
			Object[] o = ((TranslatableText)t).getArgs();
			if (o != null && o.length > 1 && o[1] instanceof String) o[1] = Markdown.convert((String) o[1]);
		}
		return t;
	}

}
