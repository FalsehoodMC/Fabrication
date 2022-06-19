package com.unascribed.fabrication.mixin.i_woina.dropped_items_dont_stack;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.ItemEntity;

@Mixin(ItemEntity.class)
@EligibleIf(configAvailable="*.dropped_items_dont_stack")
public abstract class MixinItemEntity {

	@FabInject(at=@At("HEAD"), method="canMerge()Z", cancellable=true)
	public void canMerge(CallbackInfoReturnable<Boolean> cir) {
		if (FabConf.isEnabled("*.dropped_items_dont_stack")) cir.setReturnValue(false);
	}

}
