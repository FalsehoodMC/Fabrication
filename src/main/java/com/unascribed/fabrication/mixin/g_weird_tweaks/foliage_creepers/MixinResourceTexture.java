package com.unascribed.fabrication.mixin.g_weird_tweaks.foliage_creepers;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabModifyArg;
import com.unascribed.fabrication.util.GrayscaleIdentifier;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TextureManager.class)
@EligibleIf(configAvailable="*.foliage_creepers", envMatches= Env.CLIENT)
public abstract class MixinResourceTexture {

	@FabModifyArg(at=@At(value="INVOKE", target="Lnet/minecraft/client/texture/ResourceTexture;<init>(Lnet/minecraft/util/Identifier;)V"), method= "getTexture(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/texture/AbstractTexture;")
	public Identifier convertToGrayscale(Identifier id){
		if (!(FabConf.isEnabled("*.foliage_creepers") && "fabrication_grayscale".equals(id.getNamespace()))) return id;
		return new GrayscaleIdentifier(id.getPath());
	}

}
