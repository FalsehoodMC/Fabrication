package com.unascribed.fabrication.mixin.g_weird_tweaks.foliage_creepers;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.util.GrayscaleIdentifier;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(TextureManager.class)
@EligibleIf(configAvailable="*.foliage_creepers", envMatches= Env.CLIENT)
public abstract class MixinTextureManager {

	@ModifyArgs(at=@At(value="INVOKE", target="net/minecraft/client/texture/ResourceTexture.<init>(Lnet/minecraft/util/Identifier;)V"),
			method="bindTextureInner(Lnet/minecraft/util/Identifier;)V")
	public void convertToGrayscale(Args args) {
		Identifier id = args.get(0);
		if (!(MixinConfigPlugin.isEnabled("*.foliage_creepers") && "fabrication_grayscale".equals(id.getNamespace()))) return;
		args.set(0, new GrayscaleIdentifier(id.getPath()));
	}

}
