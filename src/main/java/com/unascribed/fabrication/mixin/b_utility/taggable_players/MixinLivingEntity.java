package com.unascribed.fabrication.mixin.b_utility.taggable_players;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.PlayerTag;
import com.unascribed.fabrication.interfaces.TaggablePlayer;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
@EligibleIf(configEnabled="*.taggable_players")
public abstract class MixinLivingEntity extends Entity {

	protected MixinLivingEntity(EntityType<? extends Entity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(at=@At("HEAD"), method="canTarget(Lnet/minecraft/entity/LivingEntity;)Z", cancellable=true)
	public void canTarget(LivingEntity other, CallbackInfoReturnable<Boolean> ci) {
		if (MixinConfigPlugin.isEnabled("*.taggable_players") && other instanceof TaggablePlayer) {
			TaggablePlayer tp = ((TaggablePlayer)other);
			if (tp.fabrication$hasTag(PlayerTag.INVISIBLE_TO_MOBS)) {
				ci.setReturnValue(false);
			}
			Object self = this;
			if (self instanceof CreeperEntity) {
				if (tp.fabrication$hasTag(PlayerTag.SCARES_CREEPERS)) {
					ci.setReturnValue(false);
				}
			} else if (self instanceof PhantomEntity) {
				if (tp.fabrication$hasTag(PlayerTag.NO_PHANTOMS)) {
					ci.setReturnValue(false);
				}
			}
		}
	}
	
}
