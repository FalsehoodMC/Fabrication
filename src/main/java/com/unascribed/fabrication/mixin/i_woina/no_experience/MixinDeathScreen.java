package com.unascribed.fabrication.mixin.i_woina.no_experience;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
@EligibleIf(configAvailable="*.no_experience", envMatches=Env.CLIENT)
public class MixinDeathScreen {

	@Shadow
	private Text scoreText;

	@FabInject(at=@At("TAIL"), method="init()V")
	public void renderExperienceBar(CallbackInfo ci) {
		if (!FabConf.isEnabled("*.no_experience")) return;
		this.scoreText = Text.of("");
	}

}
