package com.unascribed.fabrication.mixin.c_tweaks.campfires_place_unlit;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.item.ItemPlacementContext;

@Mixin(CampfireBlock.class)
@EligibleIf(configAvailable="*.campfires_place_unlit")
public class MixinCampfireBlock {

	@Inject(at=@At("RETURN"), method="getPlacementState(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/block/BlockState;", cancellable=true)
	public void getPlacementState(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> ci) {
		if (FabConf.isEnabled("*.campfires_place_unlit")) {
			ci.setReturnValue(ci.getReturnValue().with(CampfireBlock.LIT, false));
		}
	}

}
