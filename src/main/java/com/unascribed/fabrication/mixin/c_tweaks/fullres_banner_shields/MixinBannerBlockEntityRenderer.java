package com.unascribed.fabrication.mixin.c_tweaks.fullres_banner_shields;

import java.util.Optional;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.SpecialEligibility;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

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

@Mixin(BannerBlockEntityRenderer.class)
@EligibleIf(configAvailable="*.fullres_banner_shields", envMatches=Env.CLIENT)
public class MixinBannerBlockEntityRenderer {

	@Unique
	private static final String RENDER_CANVAS = "renderCanvas(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/util/SpriteIdentifier;ZLnet/minecraft/util/DyeColor;Lnet/minecraft/component/type/BannerPatternsComponent;Z)V";

	@FabInject(at=@At(value="INVOKE", target="net/minecraft/client/model/ModelPart.render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V",
			shift=Shift.AFTER, ordinal=0), method=RENDER_CANVAS, cancellable=true)
	private static void renderCanvasHead(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, ModelPart canvas, SpriteIdentifier baseSprite, boolean isBanner, DyeColor color, BannerPatternsComponent patterns, boolean glint, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.fullres_banner_shields")) return;
		if (!(vertexConsumers instanceof Immediate)) return;
		if (!isBanner) {
			((Immediate)vertexConsumers).draw();
			RenderSystem.enablePolygonOffset();
			RenderSystem.polygonOffset(-3, -3);
			MatrixStack.Entry entry = matrices.peek();
			Matrix4f mdl = entry.getPositionMatrix();
			Matrix3f nrm = entry.getNormalMatrix();
			for (BannerPatternsComponent.Layer layer : patterns.layers()) {
				int col = layer.color().getEntityColor();
				Optional<RegistryKey<BannerPattern>> patternKey = layer.pattern().getKey();
				if (patternKey.isEmpty()) continue;
				Optional<RegistryEntry.Reference<BannerPattern>> patternEntry = MinecraftClient.getInstance().world.getRegistryManager().get(RegistryKeys.BANNER_PATTERN).getEntry(patternKey.get());
				if (patternEntry.isEmpty()) continue;
				SpriteIdentifier si = TexturedRenderLayers.getBannerPatternTextureId(patternEntry.get());
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
				// TODO? colors
				vc.vertex(mdl, x, y, z).color(col).texture(minU, minV).overlay(overlay).light(light).normal(entry, 0, 0, -1);
				vc.vertex(mdl, x+w, y, z).color(col).texture(maxU, minV).overlay(overlay).light(light).normal(entry, 0, 0, -1);
				vc.vertex(mdl, x+w, y+h, z).color(col).texture(maxU, maxV).overlay(overlay).light(light).normal(entry, 0, 0, -1);
				vc.vertex(mdl, x, y+h, z).color(col).texture(minU, maxV).overlay(overlay).light(light).normal(entry, 0, 0, -1);
			}
			((Immediate)vertexConsumers).draw();
			RenderSystem.disablePolygonOffset();
			ci.cancel();
		}
	}

}
