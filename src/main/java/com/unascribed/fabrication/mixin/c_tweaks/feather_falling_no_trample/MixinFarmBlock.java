package com.unascribed.fabrication.mixin.c_tweaks.feather_falling_no_trample;

import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.block.Block;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;


import static net.minecraft.block.FarmlandBlock.setToDirt;

@Mixin(FarmlandBlock.class)
@EligibleIf(configEnabled = "*.feather_falling_no_trample")
public class MixinFarmBlock extends Block{

    public MixinFarmBlock(Settings settings) {
        super(settings);
    }

    @Overwrite
    public void onLandedUpon(World world, BlockPos pos, Entity entity, float distance) {
        if (!world.isClient && world.random.nextFloat() < distance - 0.5F && entity instanceof LivingEntity
                && (entity instanceof PlayerEntity || world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING))
                && !(EnchantmentHelper.getEquipmentLevel(Enchantments.FEATHER_FALLING,(LivingEntity) entity)>=1)
                && entity.getWidth() * entity.getWidth() * entity.getHeight() > 0.512F) {
            setToDirt(world.getBlockState(pos), world, pos);
        }
        super.onLandedUpon(world, pos, entity, distance);
    }
}
