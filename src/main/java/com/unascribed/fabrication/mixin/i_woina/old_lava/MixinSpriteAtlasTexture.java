package com.unascribed.fabrication.mixin.i_woina.old_lava;

import com.unascribed.fabrication.features.FeatureOldLava;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.client.texture.SpriteLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.texture.SpriteAtlasTexture;

@Mixin(SpriteAtlasTexture.class)
@EligibleIf(configAvailable="*.old_lava", envMatches=Env.CLIENT)
public class MixinSpriteAtlasTexture {

	@FabInject(at=@At("TAIL"), method="upload(Lnet/minecraft/client/texture/SpriteLoader$StitchResult;)V")
	public void upload(SpriteLoader.StitchResult data, CallbackInfo ci) {
		FeatureOldLava.onLoaded((SpriteAtlasTexture)(Object)this, data);
	}

}
