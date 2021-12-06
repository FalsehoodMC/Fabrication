package com.unascribed.fabrication.mixin.e_mechanics.colorful_redstone;

import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

@Mixin(RedstoneWireBlock.class)
@EligibleIf(configAvailable="*.colorful_redstone")
public class MixinRedstoneWireBlock {

	@Inject(at=@At("RETURN"), method="getRenderConnectionType(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Z)Lnet/minecraft/block/enums/WireConnection;", cancellable=true)
	private void getRenderConnectionType(BlockView blockView, BlockPos blockPos, Direction direction, boolean bl, CallbackInfoReturnable<WireConnection> ci) {
		if (!MixinConfigPlugin.isEnabled("*.colorful_redstone")) return;
		if (ci.getReturnValue() != WireConnection.NONE && !fabrication$canConnect(blockView, blockPos, direction)) {
			ci.setReturnValue(WireConnection.NONE);
		}
	}
	
	private static boolean fabrication$canConnect(BlockView view, BlockPos ourPos, Direction dir) {
		BlockPos ourFloorPos = ourPos.down();
		BlockState ourFloor = view.getBlockState(ourFloorPos);
		if (ourFloor.isIn(BlockTags.WOOL)) {
			BlockPos theirPos = ourPos.offset(dir);
			BlockState them = view.getBlockState(theirPos);
			if (!them.isOf(Blocks.REDSTONE_WIRE)) {
				BlockPos up = theirPos.up();
				if (them.isSolidBlock(view, theirPos) && !view.getBlockState(up).isSolidBlock(view, up)) {
					theirPos = up;
				} else if (!them.isSolidBlock(view, theirPos)) {
					theirPos = theirPos.down();
				}
			}
			BlockPos theirFloorPos = theirPos.down();
			BlockState theirFloor = view.getBlockState(theirFloorPos);
			if (theirFloor.isIn(BlockTags.WOOL)) {
				if (ourFloor.getMapColor(view, ourFloorPos) != theirFloor.getMapColor(view, theirFloorPos)) {
					return false;
				}
			}
		}
		return true;
	}
	
	private final ThreadLocal<Direction> fabrication$capturedDirection = new ThreadLocal<>();
	
	@Inject(at=@At(value="INVOKE_ASSIGN", target="net/minecraft/world/World.getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"),
			method="getReceivedRedstonePower(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I", locals=LocalCapture.CAPTURE_FAILHARD)
	private void captureLocals(World world, BlockPos ourPos, CallbackInfoReturnable<Integer> ci, int i, int j, Iterator var5, Direction dir, BlockPos theirPos) {
		if (!MixinConfigPlugin.isEnabled("*.colorful_redstone")) return;
		fabrication$capturedDirection.set(dir);
	}
	
	@ModifyVariable(at=@At(value="INVOKE_ASSIGN", target="net/minecraft/world/World.getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"),
			method="getReceivedRedstonePower(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I", ordinal=0)
	private BlockState hideNonConnectedBlockState(BlockState orig, World world, BlockPos ourPos) {
		if (!MixinConfigPlugin.isEnabled("*.colorful_redstone")) return orig;
		if (!fabrication$canConnect(world, ourPos, fabrication$capturedDirection.get())) {
			return Blocks.AIR.getDefaultState();
		}
		return orig;
	}
	
	@ModifyVariable(at=@At(value="INVOKE", target="net/minecraft/block/BlockState.isSolidBlock(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Z"),
			method="getReceivedRedstonePower(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I", ordinal=1)
	private BlockPos mockNonsolidBelowForDisconnect(BlockPos orig, World world, BlockPos ourPos) {
		if (!MixinConfigPlugin.isEnabled("*.colorful_redstone")) return orig;
		if (!fabrication$canConnect(world, ourPos, fabrication$capturedDirection.get())) {
			// redstone wire (which we are guaranteed to be) is not solid
			// this short-circuits the check for wires below us
			return ourPos;
		}
		return orig;
	}
	
}
