package com.unascribed.fabrication.mixin.h_situational.disable_elytra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

@Mixin(ModelPredicateProviderRegistry.class)
@EligibleIf(configEnabled="*.disable_elytra", envMatches=Env.CLIENT)
public class MixinModelPredicateProviderRegistry {
	
	// "broken" predicate lambda
	// this calls the isUsable method used by the other mixin, making elytra always look broken
	// so we make this method use the original isUsable implementation to avoid that
	@Inject(at=@At("HEAD"), method={"method_27884", "lambda$static$12"}, cancellable=true, remap=false)
	private static void call(ItemStack item, ClientWorld world, LivingEntity entity, CallbackInfoReturnable<Float> ci) {
		if (MixinConfigPlugin.isEnabled("*.disable_elytra")) {
			ci.setReturnValue(item.getDamage() < item.getMaxDamage()-1 ? 0.0f : 1.0f);
		}
	}
	
}
