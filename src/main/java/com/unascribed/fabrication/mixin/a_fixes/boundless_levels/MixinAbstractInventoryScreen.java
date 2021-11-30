package com.unascribed.fabrication.mixin.a_fixes.boundless_levels;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;

@Mixin(AbstractInventoryScreen.class)
@EligibleIf(configAvailable="*.boundless_levels", envMatches=Env.CLIENT)
public class MixinAbstractInventoryScreen {

	@ModifyConstant(constant=@Constant(intValue=9), method="getStatusEffectDescription(Lnet/minecraft/entity/effect/StatusEffectInstance;)Lnet/minecraft/text/Text;")
	public int modifyMaxRenderedLevel(int orig) {
		if (MixinConfigPlugin.isEnabled("*.boundless_levels")) {
			return 32767;
		}
		return orig;
	}

}
