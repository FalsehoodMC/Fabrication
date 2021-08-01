package com.unascribed.fabrication.mixin.z_combined.exp_drop;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
@EligibleIf(anyConfigAvailable={"*.drop_more_exp_on_death", "*.drop_exp_with_keep_inventory"})
public abstract class MixinPlayerEntity extends LivingEntity {

	protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(at=@At("HEAD"), method= "getXpToDrop(Lnet/minecraft/entity/player/PlayerEntity;)I", cancellable=true)
	public void getCurrentExperience(PlayerEntity attacker, CallbackInfoReturnable<Integer> ci) {
		boolean keepInv = world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY);
		boolean keepInvDrop = MixinConfigPlugin.isEnabled("*.drop_exp_with_keep_inventory") && keepInv;
		if (keepInv && !keepInvDrop) return;
		if (MixinConfigPlugin.isEnabled("*.drop_more_exp_on_death") || keepInvDrop) {
			PlayerEntity self = ((PlayerEntity)(Object)this);
			int level = self.experienceLevel;
			int points = 0;
			for (int i = 0; i < level; i++) {
				self.experienceLevel = i;
				points += self.getNextLevelExperience();
			}
			self.experienceLevel = level;
			points += self.experienceProgress*self.getNextLevelExperience();
			float amount = keepInvDrop ? 1 : 0.8f;
			ci.setReturnValue((int)(points*amount));
		}
	}
	
}
