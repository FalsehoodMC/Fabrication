package com.unascribed.fabrication.mixin.j_experiments.packed_atlases;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.client.texture.TextureStitcher;
import net.minecraft.util.math.MathHelper;

@Mixin(TextureStitcher.class)
@EligibleIf(envMatches=Env.CLIENT, configAvailable="*.packed_atlases")
public class MixinTextureStitcher {

	@Redirect(at=@At(value="INVOKE", target="net/minecraft/util/math/MathHelper.smallestEncompassingPowerOfTwo(I)I"),
			method="stitch()V", expect=2)
	public int shortCircuitPowerOfTwo(int in) {
		if (MixinConfigPlugin.isEnabled("*.packed_atlases")) return in;
		return MathHelper.smallestEncompassingPowerOfTwo(in);
	}
	
}
