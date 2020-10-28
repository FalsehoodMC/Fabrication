package com.unascribed.fabrication.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.PlayerTag;
import com.unascribed.fabrication.interfaces.TaggablePlayer;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

@Mixin(CreeperEntity.class)
@EligibleIf(configEnabled="*.taggable_players")
public abstract class MixinTaggablePlayerCreeper extends HostileEntity {

	protected MixinTaggablePlayerCreeper(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(at=@At("TAIL"), method="initGoals()V")
	protected void initGoals(CallbackInfo ci) {
		FleeEntityGoal<ServerPlayerEntity> goal = new FleeEntityGoal<>(this, ServerPlayerEntity.class,
				spe -> spe instanceof TaggablePlayer && ((TaggablePlayer)spe).fabrication$hasTag(PlayerTag.SCARES_CREEPERS), 8, 1, 2,
				EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR::test);
		TargetPredicate withinRangePredicate = FabricationMod.snag(FleeEntityGoal.class, goal, "field_18084", "withinRangePredicate");
		withinRangePredicate.ignoreEntityTargetRules();
		goalSelector.add(3, goal);
	}
	
}
