package com.unascribed.fabrication.mixin.i_woina.dropped_items_dont_stack;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.SpecialEligibility;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
@EligibleIf(configAvailable="*.dropped_items_dont_stack")
@FailOn(invertedSpecialConditions=SpecialEligibility.NOT_FORGE)
public abstract class MixinItemEntity {

	@FabInject(at=@At("HEAD"), method="canMerge()Z", cancellable=true)
	public void canMerge(CallbackInfoReturnable<Boolean> cir) {
		if (FabConf.isEnabled("*.dropped_items_dont_stack")) cir.setReturnValue(false);
	}

}
