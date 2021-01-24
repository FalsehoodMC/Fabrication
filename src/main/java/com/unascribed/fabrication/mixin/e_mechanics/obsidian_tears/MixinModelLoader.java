package com.unascribed.fabrication.mixin.e_mechanics.obsidian_tears;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.SpecialEligibility;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

@Mixin(ModelLoader.class)
@EligibleIf(configEnabled="*.obsidian_tears", envMatches=Env.CLIENT, specialConditions={SpecialEligibility.NOT_FORGE, SpecialEligibility.NO_OPTIFINE})
public class MixinModelLoader {

	@Shadow
	private void addModel(ModelIdentifier modelId) {}
	
	@Inject(at=@At(value="CONSTANT", args="stringValue=special"), method="<init>")
	public void construct(CallbackInfo ci) {
		addModel(new ModelIdentifier(new Identifier("fabrication", "obsidian_tears"), "inventory"));
	}
	
}
