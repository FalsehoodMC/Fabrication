package com.unascribed.fabrication.mixin.b_utility.linkify_urls;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.util.Regex;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.regex.Matcher;

@Mixin(InGameHud.class)
@EligibleIf(configAvailable="*.linkify_urls", envMatches=Env.CLIENT)
public class MixinInGameHud {

	@ModifyVariable(at=@At(value="HEAD"), method="addChatMessage(Lnet/minecraft/network/MessageType;Lnet/minecraft/text/Text;Ljava/util/UUID;)V", argsOnly=true)
	public Text consume(Text message) {
		if (!FabConf.isEnabled("*.linkify_urls")) return message;
		if (!(message instanceof TranslatableText && "chat.type.text".equals(((TranslatableText)message).getKey()))) return message;
		Object[] args = ((TranslatableText) message).getArgs();
		boolean anyMatch = false;
		for (int i=0; i<args.length; i++) {
			if (args[i] instanceof String && !((String)args[i]).isEmpty()) {
				LiteralText newLine = null;
				String astr = (String) args[i];
				Matcher matcher = Regex.webUrl.matcher(astr);
				int last = 0;
				while (matcher.find()) {
					if (newLine == null) newLine = new LiteralText(astr.substring(last, matcher.start()));
					else newLine.append(astr.substring(last, matcher.start()));
					String str = matcher.group();
					LiteralText lt = new LiteralText(str);
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
		if (anyMatch) return new TranslatableText(((TranslatableText) message).getKey(), args);
		return message;
	}

}
