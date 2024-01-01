package com.unascribed.fabrication.mixin.b_utility.show_map_id;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.util.forgery_nonsense.ForgeryMatrix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

	@Shadow
	private float zOffset;

	@FabInject(at=@At("TAIL"), method="renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V")
	public void renderGuiItemOverlay(TextRenderer renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci) {
		if (FabConf.isEnabled("*.show_map_id") && stack.getItem() == Items.FILLED_MAP){
			MatrixStack matrixStack = ForgeryMatrix.getStack();
			matrixStack.translate(0, 0, zOffset + 200);
			VertexConsumerProvider.Immediate vc = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
			String id = String.valueOf(FilledMapItem.getMapId(stack));
			renderer.draw(id, (float)(x + 19 - 2 - renderer.getWidth(id)), (float)y, 16777215, true, matrixStack.peek().getModel(), vc, false, 0, 15728880);
			vc.draw();
		}
	}
}
