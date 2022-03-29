package com.unascribed.fabrication.mixin.f_balance.interrupting_damage;

import com.unascribed.fabrication.interfaces.InterruptableRangedMob;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.CrossbowAttackGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CrossbowAttackGoal.class)
@EligibleIf(configAvailable="*.interrupting_damage")
public abstract class MixinCrossbowAttackGoal<T extends HostileEntity & RangedAttackMob & CrossbowUser> implements InterruptableRangedMob {


	@Shadow
	private CrossbowAttackGoal.Stage stage;

	@Shadow @Final
	private T actor;

	@Override
	public void fabrication$interruptRangedMob() {
		switch (stage){
			case CHARGED:
			case READY_TO_ATTACK:
				LivingEntity target = actor.getTarget();
				if (target != null) {
					this.actor.attack(target, 1.0F);
					ItemStack itemStack2 = this.actor.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this.actor, Items.CROSSBOW));
					CrossbowItem.setCharged(itemStack2, false);
				}
			case CHARGING:
				stage = CrossbowAttackGoal.Stage.UNCHARGED;
		}
	}
}
