package com.unascribed.fabrication.mixin.a_fixes.fix_nether_portal_nausea;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.PortalRenderFix;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
@EligibleIf(configAvailable="*.fix_nether_portal_nausea", envMatches=Env.CLIENT)
public abstract class MixinInGameHud {

	@Shadow @Final
	private MinecraftClient client;

	@Shadow
	protected abstract void renderPortalOverlay(DrawContext context, float nauseaStrength);

	@FabInject(method="renderMiscOverlays(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at=@At(value="INVOKE",target="Lnet/minecraft/client/network/ClientPlayerEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z"))
	private void fixPortal(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci){
		if (!FabConf.isEnabled("*.fix_nether_portal_nausea")) return;
		if (((PortalRenderFix)this.client.player).fabrication$shouldRenderPortal()) {
			this.renderPortalOverlay(context, ((PortalRenderFix)this.client.player).fabrication$getPortalRenderProgress(tickCounter.getTickDelta(true)));
		}
	}
}
