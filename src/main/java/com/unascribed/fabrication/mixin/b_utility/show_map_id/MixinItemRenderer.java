package com.unascribed.fabrication.mixin.b_utility.show_map_id;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@Mixin(ItemRenderer.class)
@EligibleIf(anyConfigAvailable={"*.show_map_id"}, envMatches=Env.CLIENT)
public class MixinItemRenderer {

	@FabInject(at=@At("TAIL"), method="renderGuiItemOverlay(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V")
	public void renderGuiItemOverlay(MatrixStack matrices, TextRenderer renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci) {
		if (FabConf.isEnabled("*.show_map_id") && stack.getItem() == Items.FILLED_MAP){
			VertexConsumerProvider.Immediate vc = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
			String id = String.valueOf(FilledMapItem.getMapId(stack));
			matrices.push();
			matrices.translate(0, 0, 400);
			renderer.draw(id, (float)(x + 19 - 2 - renderer.getWidth(id)), (float)(y + 6 + 3), 16777215, true, matrices.peek().getPositionMatrix(), vc, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
			matrices.pop();
			vc.draw();
		}
	}
}
