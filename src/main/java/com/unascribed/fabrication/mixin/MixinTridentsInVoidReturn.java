package com.unascribed.fabrication.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.unascribed.fabrication.support.OnlyIf;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.world.World;

@Mixin(TridentEntity.class)
@OnlyIf(config="tweaks.tridents_in_void_return")
public abstract class MixinTridentsInVoidReturn extends Entity {

	public MixinTridentsInVoidReturn(EntityType<? extends ProjectileEntity> entityType, World world) {
		super(entityType, world);
	}
	
	@Shadow
	private static TrackedData<Byte> LOYALTY;
	@Shadow
	private boolean dealtDamage;
	
	
	@Override
	protected void destroy() {
		TridentEntity self = (TridentEntity)(Object)this;
		if (self.getOwner() != null) {
			int i = this.dataTracker.get(LOYALTY);
			if (i > 0) {
				this.dealtDamage = true;
				return;
			}
		}
		super.destroy();
	}

}
