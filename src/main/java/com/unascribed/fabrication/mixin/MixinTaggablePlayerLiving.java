package com.unascribed.fabrication.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.PlayerTag;
import com.unascribed.fabrication.interfaces.TaggablePlayer;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
@EligibleIf(configEnabled="*.taggable_players")
public abstract class MixinTaggablePlayerLiving extends Entity {

	protected MixinTaggablePlayerLiving(EntityType<? extends Entity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(at=@At("HEAD"), method="canTarget(Lnet/minecraft/entity/LivingEntity;)Z", cancellable=true)
	public void canTarget(LivingEntity other, CallbackInfoReturnable<Boolean> ci) {
		if (RuntimeChecks.check("*.taggable_players") && other instanceof TaggablePlayer) {
			if (((TaggablePlayer)other).fabrication$hasTag(PlayerTag.INVISIBLE_TO_MOBS)) {
				ci.setReturnValue(false);
			}
		}
	}
	
}
