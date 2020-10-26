package com.unascribed.fabrication.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.interfaces.IsSpider;
import com.unascribed.fabrication.interfaces.SetSlippery;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.BlockState;
import net.minecraft.block.GlazedTerracottaBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

@Mixin(GlazedTerracottaBlock.class)
@EligibleIf(configEnabled="*.spiders_cant_climb_glazed_terracotta")
public abstract class MixinSpidersCantClimbGlazedBlock extends HorizontalFacingBlock {

	protected MixinSpidersCantClimbGlazedBlock(Settings settings) {
		super(settings);
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		if (context instanceof IsSpider && ((IsSpider)context).fabrication$isSpider()) {
			return VoxelShapes.cuboid(0.01, 0, 0.01, 0.99, 1, 0.99);
		}
		return super.getCollisionShape(state, world, pos, context);
	}

	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		super.onEntityCollision(state, world, pos, entity);
		if (entity instanceof SetSlippery) {
			((SetSlippery)entity).fabrication$setSlippery(true);
		}
	}
	
}
