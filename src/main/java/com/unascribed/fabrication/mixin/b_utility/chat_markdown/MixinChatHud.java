package com.unascribed.fabrication.mixin.b_utility.chat_markdown;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabModifyVariable;
import com.unascribed.fabrication.util.Markdown;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChatHud.class)
@EligibleIf(configAvailable="*.chat_markdown")
public class MixinChatHud {

	@FabModifyVariable(at=@At(value="HEAD"), method="addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", argsOnly=true)
	public Text consume(Text message) {
		if (!FabConf.isEnabled("*.chat_markdown")) return message;
		if (!(message instanceof MutableText && message.getContent() instanceof TranslatableTextContent && "chat.type.text".equals(((TranslatableTextContent)message.getContent()).getKey()))) return message;
		Object[] args = ((TranslatableTextContent)message.getContent()).getArgs();
		boolean anyMatch = false;
		for (int i=1; i<args.length; i++) {
			if (args[i] instanceof MutableText) {
				String astr = ((MutableText) args[i]).getString();
				String bstr = Markdown.convert(astr);
				if (!astr.equals(bstr)){
					args[i] = Text.literal(bstr);
					anyMatch = true;
				}
			}
		}
		if (anyMatch) return Text.translatable(((TranslatableTextContent) message.getContent()).getKey(), args);
		return message;
	}

}
