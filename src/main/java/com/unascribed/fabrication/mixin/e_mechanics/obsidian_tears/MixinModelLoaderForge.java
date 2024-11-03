package com.unascribed.fabrication.mixin.e_mechanics.obsidian_tears;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.SpecialEligibility;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.util.Identifier;

@Mixin(ModelLoader.class)
@EligibleIf(configAvailable="*.obsidian_tears", envMatches=Env.CLIENT, specialConditions=SpecialEligibility.FORGE)
public class MixinModelLoaderForge {

	@Shadow
	private void loadInventoryVariantItemModel(Identifier id) {}

	@FabInject(at=@At(value="CONSTANT", args="stringValue=special"), method="<init>", remap=false)
	public void construct(CallbackInfo ci) {
		loadInventoryVariantItemModel(Identifier.of("fabrication", "obsidian_tears"));
	}

}
