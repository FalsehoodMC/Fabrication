package com.unascribed.fabrication.mixin.i_woina.blinking_drops;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RenderLayer.class)
@EligibleIf(configAvailable="*.blinking_drops", envMatches=Env.CLIENT, modNotLoaded="forge:obfuscate")
public abstract class MixinRenderLayer extends RenderPhase {

	public MixinRenderLayer(String name, Runnable beginAction, Runnable endAction) {
		super(name, beginAction, endAction);
	}

	//ENTITY_TRANSLUCENT_CULL net/minecraft/client/render/RenderLayer.method_34823
	@ModifyReturn(method="method_34823(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
			target="Lnet/minecraft/client/render/RenderLayer$MultiPhaseParameters$Builder;shader(Lnet/minecraft/client/render/RenderPhase$Shader;)Lnet/minecraft/client/render/RenderLayer$MultiPhaseParameters$Builder;")
	private static RenderLayer.MultiPhaseParameters.Builder fabrication$allowOverlay(RenderLayer.MultiPhaseParameters.Builder old){
		return old.shader(ENTITY_CUTOUT_SHADER);
	}

}
