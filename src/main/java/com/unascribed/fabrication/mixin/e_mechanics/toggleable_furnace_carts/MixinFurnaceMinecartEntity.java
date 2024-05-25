package com.unascribed.fabrication.mixin.e_mechanics.toggleable_furnace_carts;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.ToggleableFurnaceCart;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=FurnaceMinecartEntity.class, priority=990)
@EligibleIf(configAvailable="*.toggleable_furnace_carts")
public abstract class MixinFurnaceMinecartEntity extends AbstractMinecartEntity implements ToggleableFurnaceCart {

	@Shadow
	private int fuel;
	@Shadow
	public double pushX;
	@Shadow
	public double pushZ;

	public int fabrication$pauseFuel = 0;
	public Direction fabrication$lastMovDirection = null;

	public MixinFurnaceMinecartEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@FabInject(method="interact(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;", at=@At("HEAD"))
	public void interact(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		if (fabrication$pauseFuel > 32000) cir.setReturnValue(ActionResult.success(this.world.isClient));
	}

	@FabInject(at=@At("HEAD"), method="moveOnRail(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V")
	protected void toggleOnUnpoweredPoweredRail(BlockPos pos, BlockState state, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.toggleable_furnace_carts")) return;
		if (state.isOf(Blocks.POWERED_RAIL) && !state.get(PoweredRailBlock.POWERED)) {
			if (fuel > 0) {
				fabrication$lastMovDirection = this.getMovementDirection();
				fabrication$pauseFuel += fuel;
				fuel = 0;
				pushX = 0;
				pushZ = 0;
			}
		} else if (fabrication$pauseFuel > 0) {
			fuel += fabrication$pauseFuel;
			fabrication$pauseFuel = 0;
			Direction dir = fabrication$lastMovDirection == null ? this.getMovementDirection() : fabrication$lastMovDirection;
			pushX = dir.getOffsetX();
			pushZ = dir.getOffsetZ();
		}
	}

	@FabInject(at=@At("TAIL"), method="writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	protected void writeCustomDataToTag(NbtCompound nbt, CallbackInfo ci) {
		super.writeCustomDataToNbt(nbt);
		nbt.putInt("fabrication:PauseFuel", fabrication$pauseFuel);
		if (fabrication$lastMovDirection != null) nbt.putByte("fabrication:LastMoveDir", (byte) fabrication$lastMovDirection.getId());
	}

	@FabInject(at=@At("TAIL"), method="readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	protected void readCustomDataFromTag(NbtCompound nbt, CallbackInfo ci) {
		fabrication$pauseFuel = nbt.getInt("fabrication:PauseFuel");
		if (nbt.contains("fabrication:LastMoveDir", NbtElement.BYTE_TYPE)) fabrication$lastMovDirection = Direction.byId(nbt.getByte("fabrication:LastMoveDir"));
	}

	public int fabrication$tgfc$getPauseFuel() {
		return this.fabrication$pauseFuel;
	}
	public void fabrication$tgfc$setFuel(int fuel) {
		if (this.fabrication$pauseFuel == 0) this.fuel = fuel;
		else this.fabrication$pauseFuel = fuel;
	}

}
