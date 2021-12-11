package com.unascribed.fabrication.mixin.e_mechanics.toggleable_furnace_carts;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FurnaceMinecartEntity.class)
@EligibleIf(configAvailable="*.toggleable_furnace_carts")
public abstract class MixinFurnaceMinecartEntity extends AbstractMinecartEntity {

	@Shadow
	private int fuel;
	@Shadow
	public double pushX;
	@Shadow
	public double pushZ;

	public int fabrication$pauseFuel = 0;

	public MixinFurnaceMinecartEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject(at=@At("HEAD"), method="moveOnRail(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V")
	protected void toggleOnUnpoweredPoweredRail(BlockPos pos, BlockState state, CallbackInfo ci) {
		if (!MixinConfigPlugin.isEnabled("*.toggleable_furnace_carts")) return;
		if (state.isOf(Blocks.POWERED_RAIL) && !state.get(PoweredRailBlock.POWERED)) {
			if (fuel > 0) {
				fabrication$pauseFuel += fuel;
				fuel = 0;
				pushX = 0;
				pushZ = 0;
			}
		} else if (fabrication$pauseFuel > 0) {
			fuel += fabrication$pauseFuel;
			fabrication$pauseFuel = 0;
			Direction dir = this.getMovementDirection();
			pushX = dir.getOffsetX();
			pushZ = dir.getOffsetY();
		}
	}

	@Inject(at=@At("TAIL"), method="writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	protected void writeCustomDataToTag(NbtCompound nbt, CallbackInfo ci) {
		super.writeCustomDataToNbt(nbt);
		nbt.putInt("fabrication:PauseFuel", fabrication$pauseFuel);
	}

	@Inject(at=@At("TAIL"), method="readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	protected void readCustomDataFromTag(NbtCompound nbt, CallbackInfo ci) {
		fabrication$pauseFuel = nbt.getInt("fabrication:PauseFuel");
	}

}
