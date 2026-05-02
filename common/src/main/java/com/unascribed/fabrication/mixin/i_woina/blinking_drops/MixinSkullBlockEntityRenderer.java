package com.unascribed.fabrication.mixin.i_woina.blinking_drops;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.logic.BlinkingDropsOverlay;
import com.unascribed.fabrication.logic.WoinaDrops;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.injection.FabModifyArg;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SkullBlockEntityRenderer.class)
@EligibleIf(configAvailable="*.blinking_drops", envMatches=Env.CLIENT)
@FailOn(modLoaded="forge:obfuscate")
public class MixinSkullBlockEntityRenderer {

	@FabModifyArg(at=@At(value="INVOKE", target="Lnet/minecraft/client/render/block/entity/SkullBlockEntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"),
			method="renderSkull(Lnet/minecraft/util/math/Direction;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/block/entity/SkullBlockEntityModel;Lnet/minecraft/client/render/RenderLayer;)V",
			index=3)
	private static int fabrication$blinkSkull(int old){
		if (!FabConf.isEnabled("*.blinking_drops") || !BlinkingDropsOverlay.isDropped || BlinkingDropsOverlay.newOverlay == null) return old;
		Integer tmp = BlinkingDropsOverlay.newOverlay;
		BlinkingDropsOverlay.newOverlay = null;
		return WoinaDrops.modifyOverlay(tmp, old);
	}

}
