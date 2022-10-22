package com.unascribed.fabrication.mixin.i_woina.blinking_drops;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.logic.BlinkingDropsOverlay;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.support.injection.HijackReturn;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemRenderer.class)
@EligibleIf(configAvailable="*.blinking_drops", envMatches=Env.CLIENT, modNotLoaded="forge:obfuscate")
public class MixinItemRenderer {

	@Hijack(target="Lnet/minecraft/client/render/RenderLayers;getItemLayer(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/client/render/RenderLayer;",
			method="renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V")
	private static HijackReturn blink(ItemStack stack){
		if (!FabConf.isEnabled("*.blinking_drops") || stack.getItem() instanceof BlockItem || !BlinkingDropsOverlay.isDropped) return null;
		return BlinkingDropsOverlay.renderLayer;
	}

}
