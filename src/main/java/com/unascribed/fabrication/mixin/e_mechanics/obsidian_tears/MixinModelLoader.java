package com.unascribed.fabrication.mixin.e_mechanics.obsidian_tears;

import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.SpecialEligibility;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

@Mixin(ModelLoader.class)
@EligibleIf(configAvailable="*.obsidian_tears", envMatches=Env.CLIENT)
@FailOn(invertedSpecialConditions=SpecialEligibility.NOT_FORGE)
public class MixinModelLoader {

	@Shadow
	private void addModel(ModelIdentifier modelId) {}

	@FabInject(at=@At(value="CONSTANT", args="stringValue=special"), method="<init>", require=0)
	public void construct(CallbackInfo ci) {
		addModel(new ModelIdentifier(new Identifier("fabrication", "obsidian_tears"), "inventory"));
	}

}
