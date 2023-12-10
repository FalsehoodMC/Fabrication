package com.unascribed.fabrication.mixin.f_balance.interrupting_damage;

import com.unascribed.fabrication.interfaces.InterruptableRangedMob;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.SpecialEligibility;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractSkeletonEntity.class)
@EligibleIf(configAvailable="*.interrupting_damage")
@FailOn(invertedSpecialConditions=SpecialEligibility.NOT_FORGE)
public abstract class MixinAbstractSkeletonEntity implements InterruptableRangedMob {

	@Shadow
	@Final
	private BowAttackGoal<AbstractSkeletonEntity> bowAttackGoal;

	@Override
	public void fabrication$interruptRangedMob() {
		if (bowAttackGoal instanceof InterruptableRangedMob) ((InterruptableRangedMob)bowAttackGoal).fabrication$interruptRangedMob();
	}
}
