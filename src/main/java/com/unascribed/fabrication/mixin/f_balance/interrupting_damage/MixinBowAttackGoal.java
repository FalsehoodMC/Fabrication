package com.unascribed.fabrication.mixin.f_balance.interrupting_damage;

import com.unascribed.fabrication.interfaces.InterruptableRangedMob;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.BowItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BowAttackGoal.class)
@EligibleIf(configAvailable="*.interrupting_damage")
public abstract class MixinBowAttackGoal<T extends HostileEntity & RangedAttackMob> implements InterruptableRangedMob {

	@Shadow
	@Final
	private T actor;

	@Shadow
	private int cooldown;

	@Shadow
	private int attackInterval;

	@Override
	public void fabrication$interruptRangedMob() {
		LivingEntity target = this.actor.getTarget();
		if (target != null && this.actor.isUsingItem()) {
			this.actor.clearActiveItem();
			if (BowItem.getPullProgress(this.actor.getItemUseTime()) > 0.4f)
				this.actor.attack(target, 0.4f);
			this.cooldown = this.attackInterval;
		}
	}
}
