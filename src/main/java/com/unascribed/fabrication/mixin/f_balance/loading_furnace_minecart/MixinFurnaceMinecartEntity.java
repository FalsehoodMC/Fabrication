package com.unascribed.fabrication.mixin.f_balance.loading_furnace_minecart;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

@Mixin(FurnaceMinecartEntity.class)
@EligibleIf(configAvailable="*.loading_furnace_minecart")
public abstract class MixinFurnaceMinecartEntity extends AbstractMinecartEntity {
	@Shadow
	private int fuel;

	@Override
	@Shadow public abstract void tick();

	@Override
	@Shadow public abstract Type getMinecartType();

	@Override
	@Shadow public abstract BlockState getDefaultContainedBlock();

	protected MixinFurnaceMinecartEntity(EntityType<?> entityType, World world) {
		super(entityType, world);
	}
	@FabInject(method="tick()V", at=@At("HEAD"))
	void tick(CallbackInfo ci) {
		if (FabConf.isEnabled("*.loading_furnace_minecart") && fuel>0 && world instanceof ServerWorld) {
			((ServerWorld)world).getChunkManager().addTicket(ChunkTicketType.PORTAL, new ChunkPos(this.getBlockPos()),3, this.getBlockPos());
		}
	}
}
