package com.unascribed.fabrication.mixin.b_utility.legacy_command_syntax;

import java.util.Locale;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.mojang.brigadier.StringReader;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.command.argument.EntitySummonArgumentType;
import net.minecraft.util.Identifier;

@Mixin(EntitySummonArgumentType.class)
@EligibleIf(configAvailable="*.legacy_command_syntax")
public class MixinEntitySummonArgumentType {

	@ModifyReturn(target="Lnet/minecraft/util/Identifier;fromCommandInput(Lcom/mojang/brigadier/StringReader;)Lnet/minecraft/util/Identifier;",
			method="parse(Lcom/mojang/brigadier/StringReader;)Lnet/minecraft/util/Identifier;")
	private static Identifier fabrication$legacyCommandInput(Identifier original, StringReader sr) throws CommandSyntaxException {
		if (!FabConf.isEnabled("*.legacy_command_syntax")) return original;
		char peek = sr.peek();
		if (peek >= 'A' && peek <= 'Z') {
			int start = sr.getCursor();
			while (sr.canRead() && isCharValid(sr.peek())) {
				sr.skip();
			}
			return new Identifier("minecraft", sr.getString().substring(start, sr.getCursor())
					.replaceAll("([a-z])([A-Z])", "$1_$2")
					.toLowerCase(Locale.ROOT));
		}
		return Identifier.fromCommandInput(sr);
	}

	@Unique
	private static boolean isCharValid(char c) {
		return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
	}

}
