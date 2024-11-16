package com.unascribed.fabrication.mixin.f_balance.tools_in_bundles;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.BundleHelper;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleContentsComponent.class)
@EligibleIf(configAvailable="*.tools_in_bundles")
public class MixinBundleContentsComponent {
	@FabInject(at=@At("TAIL"), method="getOccupancy(Lnet/minecraft/item/ItemStack;)Lorg/apache/commons/lang3/math/Fraction;", cancellable=true)
	private static void getOccupancy(ItemStack stack, CallbackInfoReturnable<Fraction> cir) {
		if (FabConf.isEnabled("*.tools_in_bundles") && stack.getMaxCount() == 1) {
			cir.setReturnValue(BundleHelper.ONE_EIGHTH);
		}
	}
}
