package com.unascribed.fabrication.mixin.i_woina.old_lava;

import com.unascribed.fabrication.features.FeatureOldLava;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.SpriteAtlasTexture.Data;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteAtlasTexture.class)
@EligibleIf(configEnabled="*.old_lava", envMatches=Env.CLIENT)
public class MixinSpriteAtlasTexture {

	@Inject(at=@At("TAIL"), method="upload(Lnet/minecraft/client/texture/SpriteAtlasTexture$Data;)V", expect=1)
	public void upload(Data data, CallbackInfo ci) {
		FeatureOldLava.onLoaded((SpriteAtlasTexture)(Object)this, data);
	}
	
}
