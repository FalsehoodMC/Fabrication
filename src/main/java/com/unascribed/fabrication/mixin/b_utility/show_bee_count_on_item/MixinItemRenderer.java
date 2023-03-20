package com.unascribed.fabrication.mixin.b_utility.show_bee_count_on_item;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

@Mixin(ItemRenderer.class)
@EligibleIf(configAvailable="*.show_bee_count_on_item", envMatches=Env.CLIENT)
public class MixinItemRenderer {

	@FabInject(at=@At("TAIL"), method="renderGuiItemOverlay(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V")
	public void renderGuiItemOverlay(MatrixStack matrices, TextRenderer renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci) {
		if (!(FabConf.isEnabled("*.show_bee_count_on_item") && stack.hasNbt())) return;
		NbtCompound tag = stack.getNbt().getCompound("BlockEntityTag");
		if (tag == null || !tag.contains("Bees", NbtElement.LIST_TYPE)) return;

		VertexConsumerProvider.Immediate vc = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
		String count = String.valueOf(((NbtList)tag.get("Bees")).size());
		matrices.push();
		matrices.translate(0, 0, 400);
		renderer.draw(count, (float)(x + 19 - 2 - renderer.getWidth(count)), (float)(y), 16777045, true, matrices.peek().getPositionMatrix(), vc, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
		matrices.pop();
		vc.draw();
	}
}
