package com.unascribed.fabrication.mixin.a_fixes.nether_cauldron;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CauldronBlock.class)
@EligibleIf(configEnabled="*.nether_cauldron")
public class MixinCauldronBlock {

	@Shadow
	public static final IntProperty LEVEL = Properties.LEVEL_3;

	@Inject(at=@At("HEAD"), method="setLevel")
    public void setLevel(World world, BlockPos pos, BlockState state, int level, CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.nether_cauldron") && world.getDimension().isUltrawarm() && level>=state.get(LEVEL)) {
			world.playSound((PlayerEntity) null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
			ci.cancel();
		}
	}
}
