package com.unascribed.fabrication.mixin.f_balance.disable_elytra_boost;


import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.function.Predicate;

@Mixin(FireworkItem.class)
@EligibleIf(configAvailable="*.disable_elytra_boost")
public abstract class MixinFireworkRocketItem {

	private static final Predicate<PlayerEntity> fabrication$disableElytraBoostPredicate = ConfigPredicates.getFinalPredicate("*.disable_elytra_boost");
	@FabInject(at=@At(value="INVOKE", target="Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"),
			method="use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;", cancellable=true)
	private void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
		if (FabConf.isEnabled("*.disable_elytra_boost") && fabrication$disableElytraBoostPredicate.test(user)) cir.setReturnValue(TypedActionResult.pass(user.getStackInHand(hand)));
	}

}
