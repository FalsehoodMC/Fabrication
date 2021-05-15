package com.unascribed.fabrication.mixin.z_combined.see_items_while_riding;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.item.HeldItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HeldItemRenderer.class)
@EligibleIf(anyConfigEnabled={"*.see_items_while_riding", "*.use_items_while_riding"}, envMatches=Env.CLIENT)
public class MixinHeldItemRenderer {
	@Redirect(method="updateHeldItems()V", at=@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;isRiding()Z"))
	public boolean isRiding(ClientPlayerEntity clientPlayerEntity){
		return !(MixinConfigPlugin.isEnabled("*.see_items_while_riding") || MixinConfigPlugin.isEnabled("*.use_items_while_riding")) && clientPlayerEntity.isRiding();
	}
}
