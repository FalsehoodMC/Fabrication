package com.unascribed.fabrication.mixin.f_balance.interrupting_damage;

import com.unascribed.fabrication.interfaces.InterruptableRangedMob;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.ai.brain.task.CrossbowAttackTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CrossbowAttackTask.class)
@EligibleIf(configAvailable="*.interrupting_damage")
public abstract class MixinCrossbowAttackTask implements InterruptableRangedMob {

	@Shadow
	private CrossbowAttackTask.CrossbowState state;

	@Override
	public void fabrication$interruptRangedMob() {
		if (state == CrossbowAttackTask.CrossbowState.CHARGING) {
			state = CrossbowAttackTask.CrossbowState.UNCHARGED;
		}
	}
}
