package com.unascribed.fabrication.mixin.e_mechanics.obsidian_tears;

import com.unascribed.fabrication.logic.ObsidianTears;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.SpecialEligibility;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets="net.minecraft.block.dispenser.DispenserBehavior$17")
@EligibleIf(configAvailable="*.obsidian_tears")
@FailOn(invertedSpecialConditions=SpecialEligibility.NOT_FORGE)
public abstract class MixinGlassBottleDispenserBehavior extends FallibleItemDispenserBehavior {

	@Shadow
	protected abstract ItemStack tryPutFilledBottle(BlockPointer pointer, ItemStack emptyBottleStack, ItemStack filledBottleStack);

	@FabInject(at=@At("HEAD"), method="dispenseSilently(Lnet/minecraft/util/math/BlockPointer;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
			cancellable=true)
	public void dispenseSilently(BlockPointer pointer, ItemStack stack, CallbackInfoReturnable<ItemStack> ci) {
		ServerWorld w = pointer.getWorld();
		BlockPos pos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
		BlockState state = w.getBlockState(pos);
		if (state.getBlock() == Blocks.CRYING_OBSIDIAN) {
			setSuccess(true);
			ci.setReturnValue(tryPutFilledBottle(pointer, stack, ObsidianTears.createStack(pointer.getWorld(), pos)));
		}
	}

}
