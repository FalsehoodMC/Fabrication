package com.unascribed.fabrication.mixin.b_utility.canhit;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.unascribed.fabrication.logic.CanHitUtil;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
@EligibleIf(configAvailable="*.canhit")
public abstract class MixinPlayerEntity extends LivingEntity {

	protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Redirect(at=@At(value="INVOKE", target="net/minecraft/entity/Entity.isAttackable()Z"),
			method="attack(Lnet/minecraft/entity/Entity;)V")
	public boolean isAttackable(Entity entity) {
		if (!FabConf.isEnabled("*.canhit") || CanHitUtil.isExempt(this)) return entity.isAttackable();
		if (!entity.isAttackable()) return false;
		ItemStack stack = getStackInHand(Hand.MAIN_HAND);
		return CanHitUtil.canHit(stack, entity);
	}


}
