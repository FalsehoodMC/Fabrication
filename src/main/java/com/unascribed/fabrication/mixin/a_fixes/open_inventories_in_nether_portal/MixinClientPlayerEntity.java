package com.unascribed.fabrication.mixin.a_fixes.open_inventories_in_nether_portal;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayerEntity.class)
@EligibleIf(configEnabled="*.open_inventories_in_nether_portal", envMatches=Env.CLIENT)
public class MixinClientPlayerEntity {
	@Final
	@Shadow
	protected MinecraftClient client;

	@Redirect(method="updateNausea()V", at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;closeHandledScreen()V"))
	public void closeHandledScreen(ClientPlayerEntity clientPlayerEntity) {
		if(!MixinConfigPlugin.isEnabled("*.open_inventories_in_nether_portal"))
			clientPlayerEntity.closeHandledScreen();
	}
	@Redirect(method="updateNausea()V", at=@At(value="INVOKE", target="Lnet/minecraft/client/MinecraftClient;openScreen(Lnet/minecraft/client/gui/screen/Screen;)V"))
	public void openScreen(MinecraftClient minecraftClient, Screen screen) {
		if(!MixinConfigPlugin.isEnabled("*.open_inventories_in_nether_portal"))
			client.setScreen(null);
	}
}
