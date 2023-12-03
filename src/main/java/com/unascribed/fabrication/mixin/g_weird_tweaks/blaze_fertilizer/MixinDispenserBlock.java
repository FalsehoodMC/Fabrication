package com.unascribed.fabrication.mixin.g_weird_tweaks.blaze_fertilizer;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DispenserBlock.class)
@EligibleIf(configAvailable="*.blaze_fertilizer")
public class MixinDispenserBlock {

	private static final ItemDispenserBehavior fabrication$blazeFertilizer = new ItemDispenserBehavior() {
		@Override
		protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
			BlockPos pos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
			ServerWorld world = pointer.world();
			BlockState state = world.getBlockState(pos);
			if (state.getBlock().equals(Blocks.NETHER_WART) && state.get(NetherWartBlock.AGE) < 3) {
				world.setBlockState(pos, state.with(NetherWartBlock.AGE, Math.min(world.random.nextInt(3) + state.get(NetherWartBlock.AGE), 3)), 2);
				world.spawnParticles(ParticleTypes.FLAME, pos.getX()+0.5, pos.getY()+0.4, pos.getZ()+0.5, 4, 0.3, 0.3, 0.3, 0.05);
				stack.decrement(1);
			}
			return stack;
		}

	};

	@FabInject(at=@At("HEAD"), method="getBehaviorForItem(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/block/dispenser/DispenserBehavior;", cancellable=true)
	public void getBehaviorForItem(ItemStack stack, CallbackInfoReturnable<DispenserBehavior> ci) {
		if (!FabConf.isEnabled("*.blaze_fertilizer")) return;
		if (stack.getItem() == Items.BLAZE_POWDER) {
			ci.setReturnValue(fabrication$blazeFertilizer);
		}
	}

}
