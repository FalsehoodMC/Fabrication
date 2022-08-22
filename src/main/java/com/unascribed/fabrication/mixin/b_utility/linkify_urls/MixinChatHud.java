package com.unascribed.fabrication.mixin.b_utility.linkify_urls;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.util.Regex;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabModifyVariable;

import java.util.regex.Matcher;

@Mixin(ChatHud.class)
@EligibleIf(configAvailable="*.linkify_urls", envMatches=Env.CLIENT)
public class MixinChatHud {

	@FabModifyVariable(at=@At(value="HEAD"), method="addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", argsOnly=true)
	public Text consume(Text message) {
		if (!FabConf.isEnabled("*.linkify_urls")) return message;
		if (!(message instanceof MutableText && message.getContent() instanceof TranslatableTextContent && "chat.type.text".equals(((TranslatableTextContent)message.getContent()).getKey()))) return message;
		Object[] args = ((TranslatableTextContent)message.getContent()).getArgs();
		boolean anyMatch = false;
		for (int i=0; i<args.length; i++) {
			if (args[i] instanceof MutableText) {
				MutableText newLine = null;
				String astr = ((MutableText) args[i]).getString();
				Matcher matcher = Regex.webUrl.matcher(astr);
				int last = 0;
				while (matcher.find()) {
					if (newLine == null) newLine = Text.literal(astr.substring(last, matcher.start()));
					else newLine.append(astr.substring(last, matcher.start()));
					String str = matcher.group();
					MutableText lt = Text.literal(str);
					lt.setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, str)).withUnderline(true).withItalic(true).withColor(Formatting.AQUA));
					newLine.append(lt);
					last = matcher.end();
				}
				if (newLine != null) {
					if (!anyMatch) anyMatch = true;
					if (last < astr.length()) newLine.append(astr.substring(last));
					args[i] = newLine;
				}
			}
		}
		if (anyMatch) return Text.translatable(((TranslatableTextContent) message.getContent()).getKey(), args);
		return message;
	}

}
