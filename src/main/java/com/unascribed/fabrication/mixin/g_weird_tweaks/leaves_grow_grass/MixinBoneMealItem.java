package com.unascribed.fabrication.mixin.g_weird_tweaks.leaves_grow_grass;

import com.unascribed.fabrication.FabConf;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(BoneMealItem.class)
@EligibleIf(configAvailable="*.leaves_grow_grass")
public abstract class MixinBoneMealItem {

	@FabInject(at=@At("HEAD"), method="useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;", cancellable=true)
	private void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
		if (!FabConf.isEnabled("*.leaves_grow_grass")) return;

		World world = context.getWorld();
		BlockPos blockPos = context.getBlockPos().up();
		if (world.getBlockState(blockPos.down()).isIn(BlockTags.LEAVES) && world.isAir(blockPos)) {
			if (!world.isClient) {
				world.setBlockState(blockPos, Blocks.GRASS.getDefaultState(), 3);
				if (world.random.nextInt(10) == 0) {
					((Fertilizable) Blocks.GRASS).grow((ServerWorld) world, world.random, blockPos, Blocks.GRASS.getDefaultState());
				}
				world.playSound(null, blockPos, SoundEvents.BLOCK_GRASS_STEP, SoundCategory.PLAYERS, 1F, 1F);
				((ServerWorld)world).spawnParticles(ParticleTypes.HAPPY_VILLAGER, blockPos.getX()+0.5, blockPos.getY()+0.4, blockPos.getZ()+0.5, 4, 0.3, 0.3, 0.3, 0.05);
				PlayerEntity player = context.getPlayer();
				if (!(player == null || player.isCreative())) {
					context.getStack().decrement(1);
				}
			}

			cir.setReturnValue(ActionResult.success(world.isClient));
		}
	}

}
