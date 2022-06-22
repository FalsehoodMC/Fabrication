package com.unascribed.fabrication.mixin.a_fixes.fix_nether_portal_nausea;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.PortalRenderFix;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
@EligibleIf(configAvailable="*.fix_nether_portal_nausea", envMatches=Env.CLIENT)
public abstract class MixinInGameHud {

	@Shadow
	protected abstract void renderPortalOverlay(float nauseaStrength);

	@Shadow @Final
	private MinecraftClient client;

	@FabInject(method="render(Lnet/minecraft/client/util/math/MatrixStack;F)V", at=@At(value="INVOKE",target="Lnet/minecraft/client/network/ClientPlayerEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z"))
	private void fixPortal(MatrixStack matrices, float tickDelta, CallbackInfo ci){
		if (!FabConf.isEnabled("*.fix_nether_portal_nausea")) return;
		if (((PortalRenderFix)this.client.player).fabrication$shouldRenderPortal()) {
			this.renderPortalOverlay(((PortalRenderFix)this.client.player).fabrication$getPortalRenderProgress(tickDelta));
		}
	}
}
