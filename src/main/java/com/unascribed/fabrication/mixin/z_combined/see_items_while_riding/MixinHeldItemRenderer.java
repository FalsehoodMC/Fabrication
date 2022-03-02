package com.unascribed.fabrication.mixin.z_combined.see_items_while_riding;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.client.render.item.HeldItemRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HeldItemRenderer.class)
@EligibleIf(anyConfigAvailable={"*.see_items_while_riding", "*.use_items_while_riding"}, envMatches=Env.CLIENT)
public class MixinHeldItemRenderer {
	@ModifyReturn(method="updateHeldItems()V", target="Lnet/minecraft/client/network/ClientPlayerEntity;isRiding()Z")
	private static boolean fabrication$heldItemView(boolean original){
		return !(FabConf.isEnabled("*.see_items_while_riding") || FabConf.isEnabled("*.use_items_while_riding")) && original;
	}
}
