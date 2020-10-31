package com.unascribed.fabrication.mixin.z_combined.furnace_minecart;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

@Mixin(FurnaceMinecartEntity.class)
@EligibleIf(anyConfigEnabled={"*.furnace_minecart_pushing", "*.hyperspeed_furnace_minecart", "*.furnace_minecart_any_fuel"})
public abstract class MixinFurnaceMinecartEntity extends AbstractMinecartEntity {

	protected MixinFurnaceMinecartEntity(EntityType<?> entityType, World world) {
		super(entityType, world);
	}

	@Shadow @Final
	private static Ingredient ACCEPTABLE_FUEL;
	@Shadow
	private int fuel;
	
	@Shadow
	public double pushX;
	public double pushZ;
	
	private boolean fabrication$wasShoved;
	
	@Override
	@Overwrite
	public void applySlowdown() {
		double d = this.pushX * this.pushX + this.pushZ * this.pushZ;
		if (d > 1.0E-7) {
			d = MathHelper.sqrt(d);
			this.pushX /= d;
			this.pushZ /= d;
			double speed = MixinConfigPlugin.isEnabled("*.hyperspeed_furnace_minecart") ? 2 : 1;
			if (fabrication$wasShoved) {
				speed = 0.2;
			}
			this.setVelocity(this.getVelocity().multiply(0.8, 0.0, 0.8).add(this.pushX*speed, 0.0, this.pushZ*speed));
		} else {
			this.setVelocity(this.getVelocity().multiply(0.98, 0.0, 0.98));
		}
		super.applySlowdown();
	}
	
	@Override
	@Overwrite
	public ActionResult interact(PlayerEntity player, Hand hand) {
		ItemStack itemStack = player.getStackInHand(hand);
		boolean anyFuel = MixinConfigPlugin.isEnabled("*.furnace_minecart_any_fuel");
		if (anyFuel ? FurnaceBlockEntity.canUseAsFuel(itemStack) : ACCEPTABLE_FUEL.test(itemStack)) {
			int value = anyFuel ? FurnaceBlockEntity.createFuelTimeMap().get(itemStack.getItem())*2 : 3600;
			if (this.fuel + value <= 32000) {
				if (!player.abilities.creativeMode) {
					itemStack.decrement(1);
				}
				this.fuel += value;
			}
			fabrication$wasShoved = false;
		} else if (fuel == 0 && MixinConfigPlugin.isEnabled("*.furnace_minecart_pushing")) {
			fuel = 1;
			fabrication$wasShoved = true;
		}
		if (this.fuel > 0) {
			this.pushX = this.getX() - player.getX();
			this.pushZ = this.getZ() - player.getZ();
		}
		return ActionResult.success(this.world.isClient);
	}
	
	@Override
	@Overwrite
	public double getMaxOffRailSpeed() {
		return MixinConfigPlugin.isEnabled("*.hyperspeed_furnace_minecart") ? 0.6 : 0.2;
	}
	
	
}
