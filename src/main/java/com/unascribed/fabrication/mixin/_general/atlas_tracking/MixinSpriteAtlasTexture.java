package com.unascribed.fabrication.mixin._general.atlas_tracking;

import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.client.AtlasTracking;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

@Mixin(SpriteAtlasTexture.class)
@EligibleIf(envMatches=Env.CLIENT)
public class MixinSpriteAtlasTexture {

	@FabInject(at=@At("TAIL"), method="<init>")
	public void construct(Identifier id, CallbackInfo ci) {
		AtlasTracking.allAtlases.add((SpriteAtlasTexture)(Object)this);
	}

}
