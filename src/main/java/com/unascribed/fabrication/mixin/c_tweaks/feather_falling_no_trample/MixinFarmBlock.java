package com.unascribed.fabrication.mixin.c_tweaks.feather_falling_no_trample;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.block.Block;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmlandBlock.class)
@EligibleIf(configEnabled="*.feather_falling_no_trample")
public abstract class MixinFarmBlock extends Block {

	public MixinFarmBlock(Settings settings) {
		super(settings);
	}

	@Inject(method="onLandedUpon(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;F)V",
			at=@At("HEAD"), cancellable=true)
	public void onLandedUpon(World world, BlockPos pos, Entity entity, float distance, CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.feather_falling_no_trample") && entity instanceof LivingEntity
				&& EnchantmentHelper.getEquipmentLevel(Enchantments.FEATHER_FALLING, (LivingEntity)entity) >= 1) {
			super.onLandedUpon(world, pos, entity, distance);
			ci.cancel();
		}
	}
}
