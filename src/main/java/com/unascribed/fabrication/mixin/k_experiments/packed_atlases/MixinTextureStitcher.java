package com.unascribed.fabrication.mixin.k_experiments.packed_atlases;

import com.unascribed.fabrication.support.injection.ModifyReturn;
import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.texture.TextureStitcher;

@Mixin(TextureStitcher.class)
@EligibleIf(envMatches=Env.CLIENT, configAvailable="*.packed_atlases")
public class MixinTextureStitcher {

	@ModifyReturn(target="Lnet/minecraft/util/math/MathHelper;smallestEncompassingPowerOfTwo(I)I", method="stitch()V")
	private static int fabrication$shortCircuitPowerOfTwo(int original, int in) {
		if (FabConf.isEnabled("*.packed_atlases")) return in;
		return original;
	}

}
