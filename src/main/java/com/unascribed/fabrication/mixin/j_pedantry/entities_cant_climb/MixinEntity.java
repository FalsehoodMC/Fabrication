package com.unascribed.fabrication.mixin.j_pedantry.entities_cant_climb;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Predicate;

@Mixin(Entity.class)
@EligibleIf(anyConfigAvailable={"*.entities_cant_climb", "*.creepers_cant_climb"})
public class MixinEntity {

	private static final Predicate<LivingEntity> fabrication$entitiesCantClimbPredicate = ConfigPredicates.getFinalPredicate("*.entities_cant_climb");
	@ModifyReturn(method="move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V", target="Lnet/minecraft/block/Block;isIn(Lnet/minecraft/tag/Tag;)Z")
	private static boolean fabrication$disableClimbing(boolean old, Block state, Tag<Block> tag, Entity entity) {
		if (FabConf.isAnyEnabled("*.entities_cant_climb") &&
				tag == BlockTags.CLIMBABLE &&
				entity instanceof LivingEntity &&
				fabrication$entitiesCantClimbPredicate.test((LivingEntity)entity)
		){
			return false;
		}
		return old;
	}
}
