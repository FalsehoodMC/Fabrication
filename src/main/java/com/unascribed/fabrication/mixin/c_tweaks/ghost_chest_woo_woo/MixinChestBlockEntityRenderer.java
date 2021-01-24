package com.unascribed.fabrication.mixin.c_tweaks.ghost_chest_woo_woo;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(ChestBlockEntityRenderer.class)
@EligibleIf(configEnabled="*.ghost_chest_woo_woo", envMatches=Env.CLIENT)
public class MixinChestBlockEntityRenderer {

	@Shadow
	private boolean christmas;
	
	@Inject(at=@At("HEAD"), method="render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V")
	public void renderHead(BlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
		if (!MixinConfigPlugin.isEnabled("*.ghost_chest_woo_woo")) return;
		if (entity.hashCode()%20 == 5 && vertexConsumers instanceof Immediate) {
			((Immediate)vertexConsumers).draw(getRenderLayer(entity));
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		}
	}

	@Inject(at=@At("TAIL"), method="render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V")
	public void renderTail(BlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
		if (!MixinConfigPlugin.isEnabled("*.ghost_chest_woo_woo")) return;
		if (entity.hashCode()%20 == 5 && vertexConsumers instanceof Immediate) {
			((Immediate)vertexConsumers).draw(getRenderLayer(entity));
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		}
	}
	
	@Unique
	private RenderLayer getRenderLayer(BlockEntity entity) {
		World world = entity.getWorld();
		BlockState bs = world != null ? entity.getCachedState() : Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, Direction.SOUTH);
		ChestType type = bs.contains(ChestBlock.CHEST_TYPE) ? bs.get(ChestBlock.CHEST_TYPE) : ChestType.SINGLE;
		SpriteIdentifier sprite = TexturedRenderLayers.getChestTexture(entity, type, this.christmas);
		return sprite.getRenderLayer(RenderLayer::getEntityCutout);
	}
}
