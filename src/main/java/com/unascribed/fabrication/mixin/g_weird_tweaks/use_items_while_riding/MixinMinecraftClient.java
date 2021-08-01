package com.unascribed.fabrication.mixin.g_weird_tweaks.use_items_while_riding;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftClient.class)
@EligibleIf(configAvailable="*.use_items_while_riding", envMatches=Env.CLIENT)
public class MixinMinecraftClient {
	@Redirect(method="doAttack()V", at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;isRiding()Z"))
	public boolean canAttack(ClientPlayerEntity clientPlayerEntity){
		return !MixinConfigPlugin.isEnabled("*.use_items_while_riding") && clientPlayerEntity.isRiding();
	}
	@Redirect(method= "doItemUse()V", at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;isRiding()Z"))
	public boolean canUse(ClientPlayerEntity clientPlayerEntity){
		return !MixinConfigPlugin.isEnabled("*.use_items_while_riding") && clientPlayerEntity.isRiding();
	}
}
