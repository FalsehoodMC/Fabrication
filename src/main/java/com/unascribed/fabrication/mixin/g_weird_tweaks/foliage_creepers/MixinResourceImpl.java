package com.unascribed.fabrication.mixin.g_weird_tweaks.foliage_creepers;

import com.unascribed.fabrication.interfaces.FilterableResource;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.resource.Resource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;

@Mixin(Resource.class)
@EligibleIf(configAvailable="*.foliage_creepers", envMatches=Env.CLIENT)
public abstract class MixinResourceImpl implements FilterableResource {

	private ResourceFilter fabrication$resourceFilter = null;

	public void fabrication$applyFilter(ResourceFilter filter) {
		fabrication$resourceFilter = filter;

	}
	@Inject(method="getInputStream()Ljava/io/InputStream;", at=@At("RETURN"), cancellable=true)
	public void filterInput(CallbackInfoReturnable<InputStream> cir) {
		if (fabrication$resourceFilter != null) {
			cir.setReturnValue(fabrication$resourceFilter.apply(cir.getReturnValue()));
		}
	}

}
