package com.unascribed.fabrication.mixin.d_minor_mechanics.furnace_minecart_resupplying;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.ResupplyingFurnaceCart;
import com.unascribed.fabrication.interfaces.ToggleableFurnaceCart;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.FurnaceResupplierFakeInventory;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//TODO should just implement inventory if fabrication ever got a good way to create methods in classes
@Mixin(FurnaceMinecartEntity.class)
@EligibleIf(configAvailable="*.furnace_minecart_resupplying")
public abstract class MixinFurnaceMinecartEntity extends AbstractMinecartEntity implements ResupplyingFurnaceCart {
	@Shadow
	public double pushX;
	@Shadow
	public double pushZ;
	@Shadow
	private int fuel;
	public FurnaceResupplierFakeInventory fabrication$hopperFuel = null;

	protected MixinFurnaceMinecartEntity(EntityType<?> entityType, World world) {
		super(entityType, world);
	}

	@FabInject(at=@At("HEAD"), method="tick()V")
	protected void tick(CallbackInfo ci) {
		if (!FabConf.isEnabled("*.furnace_minecart_resupplying")) return;
		if (fabrication$hopperFuel == null || fabrication$hopperFuel.isEmpty()) return;
		ItemStack fuelStack = fabrication$hopperFuel.stack;
		int fuel;
		int value = (FabConf.isEnabled("*.furnace_minecart_any_fuel") ? FurnaceBlockEntity.createFuelTimeMap().get(fuelStack.getItem())*2 : 3600) + ((fuel = ToggleableFurnaceCart.get(this)) == 0 ? this.fuel : fuel);
		if (value <= 32000) {
			fuelStack.decrement(1);
			if (this instanceof ToggleableFurnaceCart) ((ToggleableFurnaceCart) this).fabrication$tgfc$setFuel(value);
			else this.fuel = value;
		}
		if (ToggleableFurnaceCart.get(this) == 0 && this.fuel > 0) {
			Direction dir = this.getMovementDirection();
			pushX = dir.getOffsetX();
			pushZ = dir.getOffsetZ();
		}
	}

	@Override
	public FurnaceResupplierFakeInventory fabrication$getResupplyingFurnaceCart() {
		if (fabrication$hopperFuel == null) return fabrication$hopperFuel = new FurnaceResupplierFakeInventory(this.world);
		return fabrication$hopperFuel;
	}

	/* I guess it's possible that someone inserts fuel and it doesn't turn into fuel before the cart gets unloaded, i think that's fine
	@FabInject(at=@At("TAIL"), method="writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	protected void writeCustomDataToTag(NbtCompound nbt, CallbackInfo ci) {
	}

	@FabInject(at=@At("TAIL"), method="readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	protected void readCustomDataFromTag(NbtCompound nbt, CallbackInfo ci) {
	}
	*/

}
