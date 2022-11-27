package com.unascribed.fabrication.mixin.i_woina.old_tooltip;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.support.injection.Hijack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Screen.class)
@EligibleIf(configAvailable="*.old_tooltip", envMatches=Env.CLIENT)
public abstract class MixinScreen extends AbstractParentElement implements Drawable {

	@Shadow
	public int width;

	@Shadow
	public int height;

	@Shadow protected MinecraftClient client;

	@FabInject(method="renderTooltipFromComponents(Lnet/minecraft/client/util/math/MatrixStack;Ljava/util/List;II)V", at=@At(value="INVOKE", ordinal=0, shift=At.Shift.BEFORE, target="Lnet/minecraft/client/render/Tessellator;getInstance()Lnet/minecraft/client/render/Tessellator;"))
	private void oldTooltip(MatrixStack matrices, List<TooltipComponent> components, int x, int y, CallbackInfo ci) {
		if (!(FabConf.isEnabled("*.old_tooltip") || components.isEmpty())) return;
		int i = 0;
		int backgroundHeight = components.size() == 1 ? 0 : 2;
		for (TooltipComponent line : components) {
			backgroundHeight += line.getHeight();
			int j = line.getWidth(client.textRenderer);
			if (j > i) {
				i = j;
			}
		}

		int k = x + 12;
		int l = y - 12;

		if (k + i > this.width) {
			k -= 28 + i;
		}

		if (l + backgroundHeight + 6 > this.height) {
			l = this.height - backgroundHeight - 6;
		}
		matrices.push();
		matrices.translate(0.0D, 0.0D, 400.0D);
		VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
		this.fillGradient(matrices, k - 3, l - 3, k + i + 3, l + backgroundHeight, -1073741824, -1073741824);
		immediate.draw();
		matrices.pop();
	}
	@Hijack(method="renderTooltipFromComponents(Lnet/minecraft/client/util/math/MatrixStack;Ljava/util/List;II)V",
			target="Lnet/minecraft/client/render/BufferRenderer;draw(Lnet/minecraft/client/render/BufferBuilder;)V")
	private static boolean fabrication$cancelGradient(BufferBuilder builder) {
		if (!FabConf.isEnabled("*.old_tooltip")) return false;
		builder.reset();
		return true;
	}
}
