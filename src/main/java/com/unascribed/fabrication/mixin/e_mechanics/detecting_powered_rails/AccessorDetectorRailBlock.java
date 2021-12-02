package com.unascribed.fabrication.mixin.e_mechanics.detecting_powered_rails;

import java.util.List;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.DetectorRailBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(DetectorRailBlock.class)
@EligibleIf(configAvailable="*.detecting_powered_rails")
public interface AccessorDetectorRailBlock {

	@Invoker("getCarts(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/lang/Class;Ljava/util/function/Predicate;)Ljava/util/List;")
	<T extends AbstractMinecartEntity> List<T> fabrication$getCarts(World world, BlockPos pos, Class<T> entityClass, Predicate<Entity> entityPredicate);
	
}
