package com.unascribed.fabrication.mixin.b_utility.canhit;

import com.unascribed.fabrication.logic.CanHitUtil;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerEntity.class)
@EligibleIf(configAvailable="*.canhit")
public abstract class MixinPlayerEntity {

	@ModifyReturn(target="Lnet/minecraft/entity/Entity;isAttackable()Z", method="attack(Lnet/minecraft/entity/Entity;)V")
	private static boolean fabrication$canHit(boolean old, Entity entity, PlayerEntity player) {
		if (!MixinConfigPlugin.isEnabled("*.canhit") || CanHitUtil.isExempt(player)) return old;
		if (!entity.isAttackable()) return false;
		ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
		return CanHitUtil.canHit(stack, entity);
	}

}
