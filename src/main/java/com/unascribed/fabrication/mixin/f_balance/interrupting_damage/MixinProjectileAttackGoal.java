package com.unascribed.fabrication.mixin.f_balance.interrupting_damage;

import com.unascribed.fabrication.interfaces.InterruptableRangedMob;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.mob.HostileEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ProjectileAttackGoal.class)
@EligibleIf(configAvailable="*.interrupting_damage")
public abstract class MixinProjectileAttackGoal<T extends HostileEntity & RangedAttackMob> implements InterruptableRangedMob {

	@Shadow @Final
	private RangedAttackMob owner;

	@Shadow
	private LivingEntity target;

	@Shadow
	private int updateCountdownTicks;

	@Shadow
	public abstract boolean canStart();

	@Override
	public void fabrication$interruptRangedMob() {
		if (canStart()) {
			this.owner.attack(this.target, 0.1F);
			this.updateCountdownTicks = -1;
		}
	}
}
