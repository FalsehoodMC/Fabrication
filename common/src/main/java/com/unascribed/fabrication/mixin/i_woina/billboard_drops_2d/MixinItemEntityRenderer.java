package com.unascribed.fabrication.mixin.i_woina.billboard_drops_2d;

import com.mojang.blaze3d.systems.RenderSystem;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
@EligibleIf(configAvailable="*.billboard_drops_2d", envMatches=Env.CLIENT)
public class MixinItemEntityRenderer {

	@Shadow @Final
	private ItemRenderer itemRenderer;

	@FabInject(at=@At("HEAD"), cancellable=true,
			method="render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
	public void render(ItemEntity entity, float a, float b, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.billboard_drops_2d")) return;
		ItemStack stack = entity.getStack();
		if (stack.isEmpty()) return;
		BakedModel bm = this.itemRenderer.getModel(stack, entity.world, null, 1);
		if (bm.hasDepth() && !stack.isOf(Items.TRIDENT) && !stack.isOf(Items.SPYGLASS)) return;
		Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
		if (camera == null) return;
		matrices.push();
		{
			float l = MathHelper.sin(((float) entity.getItemAge() + b) / 10.0F + entity.uniqueOffset) * 0.1F + 0.1F;
			float m = bm.getTransformation().getTransformation(ModelTransformation.Mode.GROUND).scale.getY();
			matrices.translate(0.0, l + 0.5F * m, 0.0);
		}
		float scaleX = bm.getTransformation().ground.scale.getX()/2f;
		float scaleY = bm.getTransformation().ground.scale.getY()/2f;

		Quaternion quaternion = Vec3f.NEGATIVE_Y.getDegreesQuaternion(camera.getYaw());
		Vec3f[] vec3fs = new Vec3f[]{new Vec3f(-scaleX, -scaleY, 0.0F), new Vec3f(-scaleX, scaleY, 0.0F), new Vec3f(scaleX, scaleY, 0.0F), new Vec3f(scaleX, -scaleY, 0.0F)};
		for(int k = 0; k < 4; ++k) {
			vec3fs[k].rotate(quaternion);
		}

		BakedModel bakedModel = this.itemRenderer.getModel(entity.getStack(), entity.world, null, -1);
		Sprite sprite = bakedModel.getParticleSprite();
		if (sprite == null) return;
		float l = sprite.getMinU();
		float m = sprite.getMaxU();
		float n = sprite.getMinV();
		float o = sprite.getMaxV();
		RenderSystem.setShaderTexture(0, sprite.getAtlas().getId());
		VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(TexturedRenderLayers.getEntityTranslucentCull());
		Matrix4f mat = matrices.peek().getPositionMatrix();;
		vertexConsumer.vertex(mat, vec3fs[0].getX(), vec3fs[0].getY(), vec3fs[0].getZ()).color(255, 255, 255, 255).texture(m, o).overlay(OverlayTexture.DEFAULT_UV).light(i).normal(1, 1, 1).next();
		vertexConsumer.vertex(mat, vec3fs[1].getX(), vec3fs[1].getY(), vec3fs[1].getZ()).color(255, 255, 255, 255).texture(m, n).overlay(OverlayTexture.DEFAULT_UV).light(i).normal(1, 1, 1).next();
		vertexConsumer.vertex(mat, vec3fs[2].getX(), vec3fs[2].getY(), vec3fs[2].getZ()).color(255, 255, 255, 255).texture(l, n).overlay(OverlayTexture.DEFAULT_UV).light(i).normal(1, 1, 1).next();
		vertexConsumer.vertex(mat, vec3fs[3].getX(), vec3fs[3].getY(), vec3fs[3].getZ()).color(255, 255, 255, 255).texture(l, o).overlay(OverlayTexture.DEFAULT_UV).light(i).normal(1, 1, 1).next();
		matrices.pop();
		ci.cancel();
	}

}
