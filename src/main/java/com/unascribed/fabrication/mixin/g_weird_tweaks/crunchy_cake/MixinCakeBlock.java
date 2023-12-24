package com.unascribed.fabrication.mixin.g_weird_tweaks.crunchy_cake;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.block.BlockState;
import net.minecraft.block.CakeBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CakeBlock.class)
@EligibleIf(configAvailable="*.crunchy_cake")
public class MixinCakeBlock {

	@FabInject(method="tryEat(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/util/ActionResult;", at=@At(value="INVOKE", target="Lnet/minecraft/entity/player/HungerManager;add(IF)V"))
	private void fabrication$munch(WorldAccess world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfoReturnable<?> cir) {
		if (!FabConf.isEnabled("*.crunchy_cake")) return;
		if (!world.isClient()) {
			world.playSound(null, player.getBlockPos(),player.getEatSound(state.getBlock().asItem().getDefaultStack()), SoundCategory.PLAYERS, 0.5F + 0.5F * (float) world.getRandom().nextInt(2), (world.getRandom().nextFloat() - world.getRandom().nextFloat()) * 0.2F + 1.0F);
		}
	}
}
