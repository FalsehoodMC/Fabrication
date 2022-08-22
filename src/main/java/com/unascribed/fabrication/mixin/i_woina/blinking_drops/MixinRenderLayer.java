package com.unascribed.fabrication.mixin.i_woina.blinking_drops;

import com.unascribed.fabrication.logic.BlinkingDropsOverlay;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.HijackReturn;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.SpriteAtlasTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderLayer.class)
@EligibleIf(configAvailable="*.blinking_drops", envMatches=Env.CLIENT, modNotLoaded="forge:obfuscate")
public abstract class MixinRenderLayer extends RenderPhase {

	@Shadow
	private static RenderLayer.MultiPhase of(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, RenderLayer.MultiPhaseParameters phases) {
		return null;
	}

	public MixinRenderLayer(String name, Runnable beginAction, Runnable endAction) {
		super(name, beginAction, endAction);
	}

	static {
		RenderLayer.MultiPhaseParameters fab$multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().shader(ENTITY_CUTOUT_SHADER).texture(new Texture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).target(ITEM_TARGET).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR).writeMaskState(RenderPhase.ALL_MASK).build(true);
		RenderLayer rl = of("item_entity_translucent_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, true, true, fab$multiPhaseParameters);
		BlinkingDropsOverlay.renderLayer = rl == null ? null : new HijackReturn(rl);
	}

}
