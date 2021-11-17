package com.unascribed.fabrication.mixin.g_weird_tweaks.foliage_creepers;

import java.io.InputStream;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.util.Grayscale;

import net.minecraft.resource.ResourceImpl;
import net.minecraft.util.Identifier;

@Mixin(ResourceImpl.class)
@EligibleIf(configAvailable="*.foliage_creepers")
public abstract class MixinTexture {


	@Mutable @Shadow @Final private InputStream inputStream;

	@Inject(at=@At("TAIL"), method= "<init>(Ljava/lang/String;Lnet/minecraft/util/Identifier;Ljava/io/InputStream;Ljava/io/InputStream;)V")
	public void convertToGrayscale(String packName, Identifier id, InputStream inputStream, InputStream metaInputStream, CallbackInfo ci){
		if (!(MixinConfigPlugin.isEnabled("*.foliage_creepers") && id.equals(new Identifier("textures/entity/creeper/creeper.png")))) return;
		this.inputStream = new Grayscale(this.inputStream);
	}

}