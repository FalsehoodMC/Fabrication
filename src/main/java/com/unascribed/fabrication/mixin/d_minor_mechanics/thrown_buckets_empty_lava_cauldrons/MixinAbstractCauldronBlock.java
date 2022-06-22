package com.unascribed.fabrication.mixin.d_minor_mechanics.thrown_buckets_empty_lava_cauldrons;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LavaCauldronBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LavaCauldronBlock.class)
@EligibleIf(configAvailable="*.thrown_buckets_empty_lava_cauldrons")
public class MixinAbstractCauldronBlock {

	@FabInject(at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;setOnFireFromLava()V", shift=At.Shift.BEFORE), cancellable=true, method="onEntityCollision(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V")
	private void fuelFurnace(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.thrown_buckets_empty_lava_cauldrons")) return;
		if (entity instanceof ItemEntity && ((ItemEntity)entity).getStack().isOf(Items.BUCKET)) {
			((ItemEntity)entity).getStack().decrement(1);
			ItemEntity itemEntity = new ItemEntity(world, entity.getX(), entity.getY(), entity.getZ(), Items.LAVA_BUCKET.getDefaultStack());
			itemEntity.setVelocity(entity.getVelocity());
			world.spawnEntity(itemEntity);
			world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
			world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL_LAVA, SoundCategory.BLOCKS, 1.0F, 1.0F);
			ci.cancel();
		}
	}

}
