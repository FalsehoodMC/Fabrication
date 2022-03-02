package com.unascribed.fabrication.mixin.a_fixes.open_inventories_in_nether_portal;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientPlayerEntity.class)
@EligibleIf(configAvailable="*.open_inventories_in_nether_portal", envMatches=Env.CLIENT)
public class MixinClientPlayerEntity {

	@ModifyReturn(method="updateNausea()V", target="Lnet/minecraft/client/gui/screen/Screen;isPauseScreen()Z")
	private static boolean fabrication$preventClosingScreen(boolean old) {
		return FabConf.isEnabled("*.open_inventories_in_nether_portal") || old;
	}
}
