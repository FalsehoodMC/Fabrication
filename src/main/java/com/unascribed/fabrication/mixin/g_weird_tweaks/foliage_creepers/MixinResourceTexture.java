package com.unascribed.fabrication.mixin.g_weird_tweaks.foliage_creepers;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.FilterableResource;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.util.Grayscale;
import net.minecraft.resource.NamespaceResourceManager;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(NamespaceResourceManager.class)
@EligibleIf(configAvailable="*.foliage_creepers", envMatches= Env.CLIENT)
public abstract class MixinResourceTexture {

	@FabInject(at=@At("RETURN"), method="getResource(Lnet/minecraft/util/Identifier;)Ljava/util/Optional;")
	public void generateGrayscale(Identifier id, CallbackInfoReturnable<Optional<Resource>> cir){
		if (!(FabConf.isEnabled("*.foliage_creepers") && "fabrication_grayscale".equals(id.getNamespace()))) return;
		Optional<Resource> ret = cir.getReturnValue();
		if (ret.isPresent() && ret.get() instanceof FilterableResource) {
			((FilterableResource)ret.get()).fabrication$applyFilter(Grayscale::new);
		}
	}

}
