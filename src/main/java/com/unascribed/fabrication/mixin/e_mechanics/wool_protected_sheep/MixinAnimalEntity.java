package com.unascribed.fabrication.mixin.e_mechanics.wool_protected_sheep;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabModifyVariable;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value=AnimalEntity.class, priority=999)
@EligibleIf(configAvailable="*.wool_protected_sheep")
public abstract class MixinAnimalEntity {

	@FabModifyVariable(at=@At("HEAD"), method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", argsOnly=true)
	public float damage(float amount, DamageSource source) {
		Object self = this;
		if (self instanceof SheepEntity && FabConf.isEnabled("*.wool_protected_sheep") && !((SheepEntity)self).isSheared()
				&& !source.isIn(DamageTypeTags.BYPASSES_SHIELD) && amount > 0 && !source.isIn(DamageTypeTags.BYPASSES_ARMOR) && !source.isOf(DamageTypes.OUT_OF_WORLD)) {
			return Math.max(0, amount-1);
		}
		return amount;
	}

}
