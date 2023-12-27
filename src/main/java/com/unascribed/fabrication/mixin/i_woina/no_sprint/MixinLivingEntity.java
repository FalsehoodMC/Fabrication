package com.unascribed.fabrication.mixin.i_woina.no_sprint;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.no_sprint")
abstract public class MixinLivingEntity extends Entity {

	public MixinLivingEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	private static final Predicate<Entity> fabrication$noSprintPredicate = ConfigPredicates.getFinalPredicate("*.no_sprint");

	@FabInject(at=@At(value="INVOKE", target="Lnet/minecraft/entity/attribute/EntityAttributeInstance;addTemporaryModifier(Lnet/minecraft/entity/attribute/EntityAttributeModifier;)V"), method="setSprinting(Z)V", cancellable = true)
	public void setSprinting(boolean sprinting, CallbackInfo ci) {
		if (FabConf.isEnabled("*.no_sprint") && fabrication$noSprintPredicate.test(this)) {
			super.setSprinting(false);
			ci.cancel();
		}
	}
}
