package com.unascribed.fabrication.mixin.f_balance.interrupting_damage;

import com.unascribed.fabrication.interfaces.InterruptableRangedMob;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HostileEntity.class)
@EligibleIf(configAvailable="*.interrupting_damage")
public abstract class MixinHostileEntity extends PathAwareEntity implements InterruptableRangedMob {

	protected MixinHostileEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
		super(entityType, world);
	}

	@Override
	public void fabrication$interruptRangedMob() {
		for (Goal g : goalSelector.getGoals()) {
			if (g instanceof InterruptableRangedMob)
				((InterruptableRangedMob) g).fabrication$interruptRangedMob();
		}
	}
}
