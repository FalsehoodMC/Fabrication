package com.unascribed.fabrication.mixin.c_tweaks.tridents_in_void_return;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.world.World;

@Mixin(TridentEntity.class)
@EligibleIf(configAvailable="*.tridents_in_void_return")
public abstract class MixinTridentEntity extends Entity {

	public MixinTridentEntity(EntityType<? extends ProjectileEntity> entityType, World world) {
		super(entityType, world);
	}

	@Shadow
	private static TrackedData<Byte> LOYALTY;
	@Shadow
	private boolean dealtDamage;


	@Override
	protected void tickInVoid() {
		TridentEntity self = (TridentEntity)(Object)this;
		if (FabConf.isEnabled("*.tridents_in_void_return") && self.getOwner() != null) {
			int i = this.dataTracker.get(LOYALTY);
			if (i > 0) {
				this.dealtDamage = true;
				return;
			}
		}
		super.tickInVoid();
	}

}
