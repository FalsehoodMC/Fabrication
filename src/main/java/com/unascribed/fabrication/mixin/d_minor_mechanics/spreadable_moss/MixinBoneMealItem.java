package com.unascribed.fabrication.mixin.d_minor_mechanics.spreadable_moss;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.Blocks;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

@Mixin(BoneMealItem.class)
@EligibleIf(configAvailable="*.spreadable_moss")
public class MixinBoneMealItem {

	@Inject(at=@At("HEAD"), method="useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;", cancellable=true)
	private void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
		if (!FabConf.isEnabled("*.spreadable_moss")) return;

		World world = context.getWorld();
		BlockPos bp = context.getBlockPos();
		if (world.getBlockState(bp).isIn(BlockTags.BASE_STONE_OVERWORLD)) {
			boolean found = false;
			for (BlockPos cur : BlockPos.iterateOutwards(bp, 1, 1, 1)) {
				if (world.getBlockState(cur).isOf(Blocks.MOSS_BLOCK)) {
					found = true;
					break;
				}
			}
			if (found) {
				if (!world.isClient) {
					context.getStack().decrement(1);
					world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, bp, 0);
					world.setBlockState(bp, Blocks.MOSS_BLOCK.getDefaultState());
				}
				cir.setReturnValue(ActionResult.success(world.isClient));
			}
		}
	}

}
