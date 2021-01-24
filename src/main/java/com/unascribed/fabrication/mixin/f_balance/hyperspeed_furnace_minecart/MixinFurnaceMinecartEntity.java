package com.unascribed.fabrication.mixin.f_balance.hyperspeed_furnace_minecart;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.unascribed.fabrication.interfaces.WasShoved;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.world.World;

@Mixin(FurnaceMinecartEntity.class)
@EligibleIf(configEnabled="*.hyperspeed_furnace_minecart")
public abstract class MixinFurnaceMinecartEntity extends AbstractMinecartEntity {

	protected MixinFurnaceMinecartEntity(EntityType<?> entityType, World world) {
		super(entityType, world);
	}

	@Shadow
	public int fuel;
	
	@Inject(at=@At("HEAD"), method="tick()V")
	public void tick(CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.hyperspeed_furnace_minecart") && !(this instanceof WasShoved && ((WasShoved)this).fabrication$wasShoved())) {
			// hyperspeed carts burn fuel 4x faster
			fuel = Math.max(0, fuel - 3);
		}
	}
	
	@Inject(at=@At("RETURN"), method="getMaxOffRailSpeed()D", cancellable=true)
	public void getMaxOffRailSpeed(CallbackInfoReturnable<Double> ci) {
		if (MixinConfigPlugin.isEnabled("*.hyperspeed_furnace_minecart")) {
			ci.setReturnValue(Math.max(ci.getReturnValueD(), 0.6));
		}
	}
	
}
