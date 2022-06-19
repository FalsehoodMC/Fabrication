package com.unascribed.fabrication.mixin.g_weird_tweaks.foliage_creepers;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.Grayscale;
import com.unascribed.fabrication.util.GrayscaleIdentifier;
import net.minecraft.resource.ResourceImpl;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStream;

@Mixin(ResourceImpl.class)
@EligibleIf(configAvailable="*.foliage_creepers", envMatches= Env.CLIENT)
public abstract class MixinResourceImpl {

	@Mutable @Shadow @Final private InputStream inputStream;

	@Mutable @Shadow @Final private Identifier id;

	@FabInject(at=@At("TAIL"), method= "<init>(Ljava/lang/String;Lnet/minecraft/util/Identifier;Ljava/io/InputStream;Ljava/io/InputStream;)V")
	public void convertToGrayscale(String packName, Identifier id, InputStream inputStream, InputStream metaInputStream, CallbackInfo ci){
		if (this.id instanceof GrayscaleIdentifier)
			this.inputStream = new Grayscale(this.inputStream);
	}

}
