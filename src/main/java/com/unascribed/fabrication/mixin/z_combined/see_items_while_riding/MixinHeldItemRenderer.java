package com.unascribed.fabrication.mixin.z_combined.see_items_while_riding;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.item.HeldItemRenderer;

@Mixin(HeldItemRenderer.class)
@EligibleIf(anyConfigAvailable={"*.see_items_while_riding", "*.use_items_while_riding"}, envMatches=Env.CLIENT)
public class MixinHeldItemRenderer {
	@Redirect(method="updateHeldItems()V", at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;isRiding()Z"))
	public boolean isRiding(ClientPlayerEntity clientPlayerEntity){
		return !(FabConf.isEnabled("*.see_items_while_riding") || FabConf.isEnabled("*.use_items_while_riding")) && clientPlayerEntity.isRiding();
	}
}
