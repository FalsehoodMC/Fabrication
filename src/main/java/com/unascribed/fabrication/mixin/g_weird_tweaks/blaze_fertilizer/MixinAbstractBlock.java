package com.unascribed.fabrication.mixin.g_weird_tweaks.blaze_fertilizer;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
@EligibleIf(configAvailable="*.blaze_fertilizer")
public abstract class MixinAbstractBlock {

	@Inject(method = "onUse(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;",
			at = @At("HEAD"), cancellable = true)
	public void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
		ItemStack items = player.getStackInHand(hand);
		if (MixinConfigPlugin.isEnabled("*.blaze_fertilizer") && world instanceof ServerWorld && items.getItem().equals(Items.BLAZE_POWDER)
				&& state.getBlock().equals(Blocks.NETHER_WART) && state.get(NetherWartBlock.AGE) < 3) {
			world.setBlockState(hit.getBlockPos(), state.with(NetherWartBlock.AGE, Math.min(world.random.nextInt(3) + state.get(NetherWartBlock.AGE), 3)), 2);
			((ServerWorld)world).spawnParticles(ParticleTypes.FLAME, pos.getX()+0.5, pos.getY()+0.4, pos.getZ()+0.5, 4, 0.3, 0.3, 0.3, 0.05);
			items.decrement(1);
		}
	}
}
