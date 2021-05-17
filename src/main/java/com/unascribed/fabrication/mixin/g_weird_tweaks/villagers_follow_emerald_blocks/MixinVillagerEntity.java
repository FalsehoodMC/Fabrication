package com.unascribed.fabrication.mixin.g_weird_tweaks.villagers_follow_emerald_blocks;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
@EligibleIf(configEnabled="*.villagers_follow_emerald_blocks")
public abstract class MixinVillagerEntity extends MerchantEntity  {
	private PlayerEntity fabrication$player = null;
	private static final TargetPredicate FABRICATION$TARGET_PREDICATE =
			(new TargetPredicate()).setBaseMaxDistance(10.0).includeInvulnerable().includeTeammates().ignoreDistanceScalingFactor().ignoreEntityTargetRules().setPredicate(
					player -> player.getMainHandStack().getItem().equals(Items.EMERALD_BLOCK) || player.getOffHandStack().getItem().equals(Items.EMERALD_BLOCK)
			);

	@Inject(method="mobTick()V", at=@At("TAIL"))
	public void mobTick(CallbackInfo ci){
		if(MixinConfigPlugin.isEnabled("*.villagers_follow_emerald_blocks") && !isAiDisabled()){
			if (world.getTime()%40 == 0)
				fabrication$player = world.getClosestPlayer(FABRICATION$TARGET_PREDICATE, this);
			if (fabrication$player != null) {
				getLookControl().lookAt(fabrication$player, getBodyYawSpeed(), getLookPitchSpeed());
				if (squaredDistanceTo(fabrication$player) < 6.25D)
					getNavigation().stop();
				else
					getNavigation().startMovingTo(fabrication$player, 0.6);
			}
		}
	}

	public MixinVillagerEntity(EntityType<? extends MerchantEntity> entityType, World world) {
		super(entityType, world);
	}
}
