package com.unascribed.fabrication.mixin.f_balance.loading_furnace_minecart;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FurnaceMinecartEntity.class)
@EligibleIf(configAvailable="*.loading_furnace_minecart")
public abstract class MixinFurnaceMinecartEntity extends AbstractMinecartEntity {
	@Shadow
	private int fuel;

	@Shadow public abstract void tick();

	@Shadow public abstract Type getMinecartType();

	@Shadow public abstract BlockState getDefaultContainedBlock();

	protected MixinFurnaceMinecartEntity(EntityType<?> entityType, World world) {
		super(entityType, world);
	}
	@Inject(method = "tick", at = @At("HEAD"))
	void tick(CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.loading_furnace_minecart") && fuel>0 && world instanceof ServerWorld) {
			((ServerWorld)world).getChunkManager().addTicket(ChunkTicketType.PORTAL, new ChunkPos(this.getBlockPos()),3, this.getBlockPos());
		}
	}
}
