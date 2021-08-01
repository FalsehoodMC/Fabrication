package com.unascribed.fabrication.mixin.c_tweaks.fullres_banner_shields;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.datafixers.util.Pair;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

@Mixin(BannerBlockEntityRenderer.class)
@EligibleIf(configAvailable="*.fullres_banner_shields", envMatches=Env.CLIENT)
public class MixinBannerBlockEntityRenderer {

	@Unique
	private static final String RENDER_CANVAS = "renderCanvas(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/util/SpriteIdentifier;ZLjava/util/List;Z)V";

	@Inject(at=@At(value="INVOKE", target="net/minecraft/client/model/ModelPart.render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V",
			shift=Shift.AFTER, ordinal=0), method=RENDER_CANVAS, cancellable=true)
	private static void renderCanvasHead(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, ModelPart canvas, SpriteIdentifier baseSprite, boolean isBanner, List<Pair<BannerPattern, DyeColor>> patterns, boolean bl2, CallbackInfo ci) {
		if (!MixinConfigPlugin.isEnabled("*.fullres_banner_shields")) return;
		if (!(vertexConsumers instanceof Immediate)) return;
		if (!isBanner) {
			((Immediate)vertexConsumers).draw();
			RenderSystem.enablePolygonOffset();
			RenderSystem.polygonOffset(-3, -3);
			Matrix4f mdl = matrices.peek().getModel();
			Matrix3f nrm = matrices.peek().getNormal();
			for (Pair<BannerPattern, DyeColor> pattern : patterns) {
				float[] col = pattern.getSecond().getColorComponents();
				SpriteIdentifier si = new SpriteIdentifier(TexturedRenderLayers.BANNER_PATTERNS_ATLAS_TEXTURE, pattern.getFirst().getSpriteId(true));
				VertexConsumer vc = si.getVertexConsumer(vertexConsumers, RenderLayer::getEntityNoOutline);
				Sprite sprite = si.getSprite();
				float minU = sprite.getMinU();
				float maxU = sprite.getMaxU();
				float sizeU = (maxU-minU);
				maxU = minU+(sizeU*(21/64f));
				minU = minU+(sizeU*(1/64f));
				float minV = sprite.getMinV();
				float maxV = sprite.getMaxV();
				float sizeV = (maxV-minV);
				maxV = minV+(sizeV*(41/64f));
				minV = minV+(sizeV*(1/64f));
				float x = -0.3125f;
				float y = -0.625f;
				float w = 0.625f;
				float h = 1.25f;
				float z = -0.125f;
				vc.vertex(mdl, x, y, z).color(col[0], col[1], col[2], 1).texture(minU, minV).overlay(overlay).light(light).normal(nrm, 0, 0, -1).next();
				vc.vertex(mdl, x+w, y, z).color(col[0], col[1], col[2], 1).texture(maxU, minV).overlay(overlay).light(light).normal(nrm, 0, 0, -1).next();
				vc.vertex(mdl, x+w, y+h, z).color(col[0], col[1], col[2], 1).texture(maxU, maxV).overlay(overlay).light(light).normal(nrm, 0, 0, -1).next();
				vc.vertex(mdl, x, y+h, z).color(col[0], col[1], col[2], 1).texture(minU, maxV).overlay(overlay).light(light).normal(nrm, 0, 0, -1).next();
			}
			((Immediate)vertexConsumers).draw();
			RenderSystem.disablePolygonOffset();
			ci.cancel();
		}
	}

}
