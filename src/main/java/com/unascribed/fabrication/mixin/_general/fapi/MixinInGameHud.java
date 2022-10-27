package com.unascribed.fabrication.mixin._general.fapi;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.FabricationEventsClient;
import com.unascribed.fabrication.support.SpecialEligibility;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
@EligibleIf(specialConditions=SpecialEligibility.NOT_FORGE)
public class MixinInGameHud {

		@FabInject(method="render(Lnet/minecraft/client/util/math/MatrixStack;F)V", at=@At(value="TAIL"))
		public void render(MatrixStack matrixStack, float tickDelta, CallbackInfo callbackInfo) {
			FabricationEventsClient.hud(matrixStack, tickDelta);
		}

}
