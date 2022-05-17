package com.unascribed.fabrication.mixin.g_weird_tweaks.villagers_follow_emerald_blocks;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.world.World;

@Mixin(VillagerEntity.class)
@EligibleIf(configAvailable="*.villagers_follow_emerald_blocks")
public abstract class MixinVillagerEntity extends MerchantEntity  {
	private PlayerEntity fabrication$player = null;

	@Inject(method="mobTick()V", at=@At("TAIL"))
	public void mobTick(CallbackInfo ci){
		if(FabConf.isEnabled("*.villagers_follow_emerald_blocks") && !isAiDisabled()){
			if (world.getTime()%40 == 0)
				fabrication$player = world.getClosestPlayer(getX(), getY(), getZ(), 10, (player) -> player instanceof PlayerEntity && !player.isSpectator() && ((PlayerEntity)player).isHolding(Items.EMERALD_BLOCK));
			if (fabrication$player != null) {
				getLookControl().lookAt(fabrication$player, getMaxHeadRotation(), getMaxLookPitchChange());
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
