package com.unascribed.fabrication.mixin.a_fixes.fix_end_portal_render;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndPortalBlockEntity.class)
@EligibleIf(configAvailable="*.fix_end_portal_render", envMatches=Env.CLIENT)
public abstract class MixinPortalBlockEntity extends BlockEntity {

	public MixinPortalBlockEntity(BlockEntityType<?> type) {
		super(type);
	}

	@FabInject(at=@At("HEAD"), method="shouldDrawSide(Lnet/minecraft/util/math/Direction;)Z", cancellable=true)
	public void renderAllSides(Direction direction, CallbackInfoReturnable<Boolean> cir) {
		if (FabConf.isEnabled("*.fix_end_portal_render")) {
			if (direction == Direction.UP) return;
			boolean shouldDrawSide = !world.getBlockState(pos.offset(direction)).isOf(getCachedState().getBlock());
			if (shouldDrawSide) {
				shouldDrawSide = Block.shouldDrawSide(this.getCachedState(), this.world, this.getPos(), direction);
			}
			cir.setReturnValue(shouldDrawSide);
		}
	}

}
