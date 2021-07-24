package com.unascribed.fabrication.mixin.c_tweaks.nether_cauldron;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CauldronBehavior.class)
@EligibleIf(configEnabled="*.nether_cauldron")
public class MixinCauldronBlock {

	@Inject(at=@At(value="INVOKE", shift=At.Shift.BEFORE, target="Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"), method="fillCauldron(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;Lnet/minecraft/block/BlockState;Lnet/minecraft/sound/SoundEvent;)Lnet/minecraft/util/ActionResult;", cancellable = true)
	private static void setLevel(World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack, BlockState state, SoundEvent soundEvent, CallbackInfoReturnable<ActionResult> cir) {
		if (MixinConfigPlugin.isEnabled("*.nether_cauldron") && world instanceof ServerWorld && world.getDimension().isUltrawarm() && state.isOf(Blocks.WATER_CAULDRON)) {
			world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5f, 2.6f + (world.random.nextFloat() - world.random.nextFloat()) * 0.8f);
			((ServerWorld)world).spawnParticles(ParticleTypes.LARGE_SMOKE, pos.getX()+0.5, pos.getY()+0.6, pos.getZ()+0.5, 8, 0.2, 0.2, 0.2, 0);
			cir.setReturnValue(ActionResult.CONSUME);
		}
	}
}
