package com.unascribed.fabrication.mixin.g_weird_tweaks.foliage_creepers;

import com.unascribed.fabrication.interfaces.Grayscalable;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Identifier.class)
@EligibleIf(configAvailable="*.foliage_creepers", envMatches=Env.CLIENT)
public class MixinIdentifier implements Grayscalable {

	private boolean fabrication$isGrayscale = false;

	@Override
	public void fabrication$markGrayscale() {
		fabrication$isGrayscale = true;
	}

	@Override
	public boolean fabrication$hasGrayscale() {
		return fabrication$isGrayscale;
	}
}
