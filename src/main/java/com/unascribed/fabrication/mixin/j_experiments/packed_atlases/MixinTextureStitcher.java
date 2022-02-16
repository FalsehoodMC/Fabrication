package com.unascribed.fabrication.mixin.j_experiments.packed_atlases;

import com.unascribed.fabrication.support.injection.ModifyReturn;
import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.client.texture.TextureStitcher;

@Mixin(TextureStitcher.class)
@EligibleIf(envMatches=Env.CLIENT, configAvailable="*.packed_atlases")
public class MixinTextureStitcher {

	@ModifyReturn(target={"Lnet/minecraft/util/math/MathHelper;smallestEncompassingPowerOfTwo(I)I", "Lnet/minecraft/class_3532;method_15339(I)I"},
			method={"stitch()V", "method_4557()V"})
	private static int fabrication$shortCircuitPowerOfTwo(int original, int in) {
		if (MixinConfigPlugin.isEnabled("*.packed_atlases")) return in;
		return original;
	}

}
