package com.unascribed.fabrication.mixin.a_fixes.boundless_levels;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.util.RomanNumeral;

import com.google.common.primitives.Ints;

import net.minecraft.client.resource.language.TranslationStorage;

@Mixin(TranslationStorage.class)
@EligibleIf(configEnabled="*.boundless_levels", envMatches=Env.CLIENT)
public class MixinTranslationStorage {

	@Inject(at=@At("HEAD"), method="get(Ljava/lang/String;)Ljava/lang/String;", cancellable=true)
	public void get(String key, CallbackInfoReturnable<String> ci) {
		if (!MixinConfigPlugin.isEnabled("*.boundless_levels")) return;
		if (key.startsWith("enchantment.level.")) {
			Integer i = Ints.tryParse(key.substring(18));
			if (i != null) {
				ci.setReturnValue(RomanNumeral.format(i));
			}
		}
		if (key.startsWith("potion.potency.")) {
			Integer i = Ints.tryParse(key.substring(15));
			if (i != null) {
				if (i == 0) {
					ci.setReturnValue("");
				} else {
					ci.setReturnValue(RomanNumeral.format(i+1));
				}
			}
		}
	}

}
