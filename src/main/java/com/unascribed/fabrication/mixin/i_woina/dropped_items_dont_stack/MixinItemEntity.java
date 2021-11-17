package com.unascribed.fabrication.mixin.i_woina.dropped_items_dont_stack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.entity.ItemEntity;

@Mixin(ItemEntity.class)
@EligibleIf(configEnabled="*.dropped_items_dont_stack")
public abstract class MixinItemEntity {

	@Inject(at=@At("HEAD"), method="canMerge()Z", cancellable=true)
	public void canMerge(CallbackInfoReturnable<Boolean> cir) {
		if (MixinConfigPlugin.isEnabled("*.dropped_items_dont_stack")) cir.setReturnValue(false);
	}

}
