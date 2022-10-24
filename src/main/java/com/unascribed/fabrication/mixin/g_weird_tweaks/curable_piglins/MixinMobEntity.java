package com.unascribed.fabrication.mixin.g_weird_tweaks.curable_piglins;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.GetPreZombified;
import com.unascribed.fabrication.interfaces.ZombImmunizableEntity;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
@EligibleIf(configAvailable="*.curable_piglins")
public abstract class MixinMobEntity extends LivingEntity {

	@Shadow
	@Nullable
	public abstract <T extends MobEntity> T method_29243(EntityType<T> entityType, boolean keepEquipment);

	protected MixinMobEntity(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@FabInject(method="interactMob(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;", at=@At("HEAD"), cancellable=true)
	public void immunizePiglin(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		if (!FabConf.isEnabled("*.curable_piglins")) return;
		Object self = this;
		boolean can = self instanceof ZombImmunizableEntity;
		boolean zombified = self instanceof GetPreZombified;
		if (!(can || zombified)) return;
		ItemStack item = player.getStackInHand(hand);
		if (item.getItem() == Items.GOLDEN_APPLE && (zombified && ((LivingEntity)self).hasStatusEffect(StatusEffects.WEAKNESS) || can && !((ZombImmunizableEntity)self).fabrication$isZombImmune())) {
			item.decrement(1);
			this.playSound(SoundEvents.ENTITY_PLAYER_BURP, 1, this.getRandom().nextFloat() * 0.5F);
			if (zombified) self = this.method_29243(((GetPreZombified)self).fabrication$getPreZombifiedType(), true);
			if (self instanceof ZombImmunizableEntity) ((ZombImmunizableEntity) self).fabrication$setZombImmune(true);
			cir.setReturnValue(ActionResult.SUCCESS);
		}
	}
}
