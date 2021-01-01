package com.unascribed.fabrication.mixin.e_mechanics.obsidian_tears;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.ObsidianTears;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;

@Mixin(targets="net.minecraft.block.dispenser.DispenserBehavior$17")
@EligibleIf(configEnabled="*.obsidian_tears")
public class MixinGlassBottleDispenserBehavior extends FallibleItemDispenserBehavior {

	@Shadow
	private ItemStack method_22141(BlockPointer blockPointer, ItemStack emptyBottleStack, ItemStack filledBottleStack) { return null; }
	
	@Inject(at=@At("HEAD"), method="dispenseSilently(Lnet/minecraft/util/math/BlockPointer;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
			cancellable=true)
	public void dispenseSilently(BlockPointer pointer, ItemStack stack, CallbackInfoReturnable<ItemStack> ci) {
		ServerWorld w = pointer.getWorld();
		BlockPos pos = pointer.getBlockPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
		BlockState state = w.getBlockState(pos);
		if (state.getBlock() == Blocks.CRYING_OBSIDIAN) {
			setSuccess(true);
			ci.setReturnValue(method_22141(pointer, stack, ObsidianTears.createStack(pointer.getWorld(), pos)));
		}
	}

}
