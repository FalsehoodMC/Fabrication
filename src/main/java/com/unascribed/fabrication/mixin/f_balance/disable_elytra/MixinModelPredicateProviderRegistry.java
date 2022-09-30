package com.unascribed.fabrication.mixin.f_balance.disable_elytra;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

@Mixin(ModelPredicateProviderRegistry.class)
@EligibleIf(configAvailable="*.disable_elytra", envMatches=Env.CLIENT)
public class MixinModelPredicateProviderRegistry {

	// "broken" predicate lambda
	// this calls the isUsable method used by the other mixin, making elytra always look broken
	// so we make this method use the original isUsable implementation to avoid that
	@FabInject(at=@At("HEAD"), method={"method_27884", "lambda$static$12"}, cancellable=true, remap=false, require=0)
	private static void call(ItemStack item, ClientWorld world, LivingEntity entity, int seed, CallbackInfoReturnable<Float> ci) {
		if (FabConf.isEnabled("*.disable_elytra")) {
			ci.setReturnValue(item.getDamage() < item.getMaxDamage()-1 ? 0.0f : 1.0f);
		}
	}

}
