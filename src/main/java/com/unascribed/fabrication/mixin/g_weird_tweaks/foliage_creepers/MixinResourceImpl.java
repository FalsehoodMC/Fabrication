package com.unascribed.fabrication.mixin.g_weird_tweaks.foliage_creepers;

import com.unascribed.fabrication.interfaces.FilterableResource;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.resource.Resource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.io.InputStream;

@Mixin(Resource.class)
@EligibleIf(configAvailable="*.foliage_creepers", envMatches= Env.CLIENT)
public abstract class MixinResourceImpl implements FilterableResource {

	@Mutable @Shadow @Final
	private Resource.InputSupplier<InputStream> inputSupplier;

	public void fabrication$applyFilter(ResourceFilter filter) {
		this.inputSupplier = () -> filter.apply(this.inputSupplier.get());
	}

}
