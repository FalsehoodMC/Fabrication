package com.unascribed.fabrication.mixin.c_tweaks.no_heavy_minecarts;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StorageMinecartEntity.class)
@EligibleIf(configAvailable="*.no_heavy_minecarts")
public abstract class MixinStorageMinecartEntity extends AbstractMinecartEntity {
	protected MixinStorageMinecartEntity(EntityType<?> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method="applySlowdown", at=@At("HEAD"))
	private void cancelSlowdown(CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.no_heavy_minecarts")) {
			//has to reimplement since mixin cant affect flow control well and it would need a redirect otherwise
			float slowdown = 0.98F;

			if (this.isTouchingWater()) {
				slowdown *= 0.95F;
			}

			this.setVelocity(this.getVelocity().multiply(slowdown, 0.0D, slowdown));
			ci.cancel();
		}
	}
}
