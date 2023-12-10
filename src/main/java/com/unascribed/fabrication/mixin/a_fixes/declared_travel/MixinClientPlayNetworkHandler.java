package com.unascribed.fabrication.mixin.a_fixes.declared_travel;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.DimInformedScreen;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.util.Strings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
@EligibleIf(configAvailable="*.declared_travel", envMatches=Env.CLIENT)
public class MixinClientPlayNetworkHandler {

	@Inject(method="onPlayerRespawn(Lnet/minecraft/network/packet/s2c/play/PlayerRespawnS2CPacket;)V", at=@At(value="INVOKE", shift=At.Shift.AFTER, target="Lnet/minecraft/client/network/ClientPlayNetworkHandler;startWorldLoading(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/client/world/ClientWorld;)V"))
	private void fabrication$addDimensionDataToTerrainScreen(PlayerRespawnS2CPacket packet, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.declared_travel")) return;
		Screen screen = MinecraftClient.getInstance().currentScreen;
		if (!(screen instanceof DimInformedScreen)) return;
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (player == null) return;
		Identifier target = packet.commonPlayerSpawnInfo().dimension().getValue();
		if (target == null) return;
		Identifier current = player.getWorld().getRegistryKey().getValue();
		if (current == null) {
			((DimInformedScreen) screen).fabrication$setDimText("Entering "+ Strings.capitalizeIdenfier(target.getPath()));
			return;
		}
		if (!target.equals(current)) {
			if ("overworld".equals(target.getPath())) {
				((DimInformedScreen) screen).fabrication$setDimText("Leaving "+ Strings.capitalizeIdenfier(current.getPath()));
			} else {
				((DimInformedScreen) screen).fabrication$setDimText("Entering "+ Strings.capitalizeIdenfier(target.getPath()));
			}
		}
	}
}
