package com.unascribed.fabrication.mixin.b_utility.legacy_command_syntax;

import com.mojang.brigadier.StringReader;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.command.argument.EntitySummonArgumentType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

@Mixin(EntitySummonArgumentType.class)
@EligibleIf(configAvailable="*.legacy_command_syntax")
public class MixinEntitySummonArgumentType {

	@FabInject(at=@At("HEAD"), method="parse(Lcom/mojang/brigadier/StringReader;)Lnet/minecraft/util/Identifier;", cancellable=true)
	public void legacyCommandInput(StringReader sr, CallbackInfoReturnable<Identifier> cir) {
		if (!FabConf.isEnabled("*.legacy_command_syntax")) return;
		char peek = sr.peek();
		if (peek >= 'A' && peek <= 'Z') {
			int start = sr.getCursor();
			while (sr.canRead() && isCharValid(sr.peek())) {
				sr.skip();
			}
			if (!sr.canRead()) {
				cir.setReturnValue(
						new Identifier("minecraft", sr.getString().substring(start, sr.getCursor())
						.replaceAll("([a-z])([A-Z])", "$1_$2")
						.toLowerCase(Locale.ROOT)));
			}
		}
	}

	@Unique
	private static boolean isCharValid(char c) {
		return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
	}

}
