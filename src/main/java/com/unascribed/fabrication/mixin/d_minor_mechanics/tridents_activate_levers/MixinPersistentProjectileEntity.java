package com.unascribed.fabrication.mixin.d_minor_mechanics.tridents_activate_levers;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeverBlock;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

@Mixin(PersistentProjectileEntity.class)
@EligibleIf(configAvailable="*.tridents_activate_levers")
public abstract class MixinPersistentProjectileEntity {

	@Inject(at=@At("HEAD"), method="onBlockHit(Lnet/minecraft/util/hit/BlockHitResult;)V")
	public void onBlockHit(BlockHitResult blockHitResult, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.tridents_activate_levers")) return;
		Object self = this;
		if ((self instanceof TridentEntity)) {
			BlockPos blockPos = blockHitResult.getBlockPos().offset(blockHitResult.getSide());
			BlockState state = ((TridentEntity) self).world.getBlockState(blockPos);
			if (state.isOf(Blocks.LEVER) && state.getOutlineShape(((TridentEntity) self).world, blockPos).getBoundingBox().offset(blockPos).expand(0.01).contains(blockHitResult.getPos())) {
				((TridentEntity) self).world.setBlockState(blockPos, state.cycle(LeverBlock.POWERED));
			}
		}
	}

}
