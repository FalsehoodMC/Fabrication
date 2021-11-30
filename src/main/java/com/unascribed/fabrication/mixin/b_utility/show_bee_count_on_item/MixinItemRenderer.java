package com.unascribed.fabrication.mixin.b_utility.show_bee_count_on_item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;

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

	@Shadow
	private float zOffset;

	@Inject(at=@At("TAIL"), method="renderGuiItemOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V")
	public void renderGuiItemOverlay(TextRenderer renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci) {
		if (!(MixinConfigPlugin.isEnabled("*.show_bee_count_on_item") && stack.hasNbt())) return;
		NbtCompound tag = stack.getNbt().getCompound("BlockEntityTag");
		if (tag == null || !tag.contains("Bees", NbtElement.LIST_TYPE)) return;

		MatrixStack matrixStack = new MatrixStack();
		matrixStack.translate(0, 0, zOffset + 200);
		VertexConsumerProvider.Immediate vc = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
		String count = String.valueOf(((NbtList)tag.get("Bees")).size());
		renderer.draw(count, (float)(x + 19 - 2 - renderer.getWidth(count)), (float)(y), 16777045, true, matrixStack.peek().getPositionMatrix(), vc, false, 0, 15728880);
		vc.draw();
	}
}
