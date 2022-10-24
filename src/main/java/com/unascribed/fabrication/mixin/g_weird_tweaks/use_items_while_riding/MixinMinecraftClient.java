package com.unascribed.fabrication.mixin.g_weird_tweaks.use_items_while_riding;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftClient.class)
@EligibleIf(configAvailable="*.use_items_while_riding", envMatches=Env.CLIENT)
public class MixinMinecraftClient {
	@ModifyReturn(method={"doItemUse()V", "doAttack()V"}, target="Lnet/minecraft/client/network/ClientPlayerEntity;isRiding()Z")
	private static boolean fabrication$allowUsageWhileRiding(boolean old){
		return !FabConf.isEnabled("*.use_items_while_riding") && old;
	}
}
