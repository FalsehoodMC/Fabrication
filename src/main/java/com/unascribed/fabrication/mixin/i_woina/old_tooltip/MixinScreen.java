package com.unascribed.fabrication.mixin.i_woina.old_tooltip;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Screen.class)
@EligibleIf(configAvailable="*.old_tooltip", envMatches=Env.CLIENT)
public abstract class MixinScreen extends AbstractParentElement implements Drawable {

    @Shadow
    public int width;

    @Shadow
    public int height;

    @Shadow @Nullable protected MinecraftClient client;

    @Inject(method = "renderTooltipFromComponents(Lnet/minecraft/client/util/math/MatrixStack;Ljava/util/List;II)V", at = @At(value = "HEAD"), cancellable = true)
    private void drawOldTooltip(MatrixStack matrices, List<TooltipComponent> components, int x, int y, CallbackInfo ci) {
        if (MixinConfigPlugin.isEnabled("*.old_tooltip")) {
            // Ported from MCP beta 1.7.3
            // Slightly altered in order to get item attributes to properly list in the tooltip
            if (!components.isEmpty()) {
                int i = 0;
                for (TooltipComponent line : components) {
                    int j = line.getWidth(client.textRenderer);
                    if (j > i) {
                        i = j;
                    }
                }

                int k = x + 12;
                int l = y - 12;
                int n = 8;
                if (components.size() > 1) {
                    n += 2 + (components.size() - 1) * 10;
                }

                if (k + i > this.width) {
                    k -= 28 + i;
                }

                if (l + n + 6 > this.height) {
                    l = this.height - n - 6;
                }
                matrices.push();
                matrices.translate(0.0D, 0.0D, 400.0D);
                Matrix4f matrix4f = matrices.peek().getModel();
                VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
                this.fillGradient(matrices, k - 3, l - 3, k + i + 3, l + (11 * components.size()), -1073741824, -1073741824);
                for (int s = 0; s < components.size(); ++s) {
                    components.get(s).drawText(client.textRenderer, k, l, matrix4f, immediate);

                    if (s == 0) {
                        l += 2;
                    }

                    l += 10;
                }
                immediate.draw();
                matrices.pop();
                ci.cancel();
            }
        }
    }
}