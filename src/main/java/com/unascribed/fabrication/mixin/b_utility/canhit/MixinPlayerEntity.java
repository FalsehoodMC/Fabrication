package com.unascribed.fabrication.mixin.b_utility.canhit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.unascribed.fabrication.CanHitUtil;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
@EligibleIf(configEnabled="*.canhit")
public abstract class MixinPlayerEntity extends LivingEntity {

	protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}
	
	@Redirect(at=@At(value="INVOKE", target="net/minecraft/entity/Entity.isAttackable()Z"),
			method="attack(Lnet/minecraft/entity/Entity;)V")
	public boolean isAttackable(Entity entity) {
		if (!RuntimeChecks.check("*.canhit")) return entity.isAttackable();
		if (!entity.isAttackable()) return false;
		ItemStack stack = getStackInHand(Hand.MAIN_HAND);
		return CanHitUtil.canHit(stack, entity);
	}
	
	
}
