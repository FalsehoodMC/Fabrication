package com.unascribed.fabrication.mixin.a_fixes.declared_travel;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.DimInformedScreen;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import com.unascribed.fabrication.util.Strings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientPlayNetworkHandler.class)
@EligibleIf(configAvailable="*.declared_travel", envMatches=Env.CLIENT)
public class MixinClientPlayNetworkHandler {

	@ModifyReturn(method="onPlayerRespawn(Lnet/minecraft/network/packet/s2c/play/PlayerRespawnS2CPacket;)V", target="Lnet/minecraft/client/gui/screen/DownloadingTerrainScreen;<init>()V")
	private static DownloadingTerrainScreen fabrication$addDimensionDataToTerrainScreen(DownloadingTerrainScreen screen, DownloadingTerrainScreen o, ClientPlayNetworkHandler self, PlayerRespawnS2CPacket packet) {
		if (!(FabConf.isEnabled("*.declared_travel") && screen instanceof DimInformedScreen)) return screen;
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (player == null) return screen;
		Identifier target = packet.getDimension().getValue();
		if (target == null) return screen;
		Identifier current = player.getWorld().getRegistryKey().getValue();
		if (current == null) {
			((DimInformedScreen) screen).fabrication$setDimText("Entering "+ Strings.capitalizeIdenfier(target.getPath()));
			return screen;
		}
		if (!target.equals(current)) {
			if ("overworld".equals(target.getPath())) {
				((DimInformedScreen) screen).fabrication$setDimText("Leaving "+ Strings.capitalizeIdenfier(current.getPath()));
			} else {
				((DimInformedScreen) screen).fabrication$setDimText("Entering "+ Strings.capitalizeIdenfier(target.getPath()));
			}
		}
		return screen;
	}
}
