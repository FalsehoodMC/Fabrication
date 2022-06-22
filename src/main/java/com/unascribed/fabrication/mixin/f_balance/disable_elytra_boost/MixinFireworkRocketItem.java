package com.unascribed.fabrication.mixin.f_balance.disable_elytra_boost;


import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

@Mixin(FireworkRocketItem.class)
@EligibleIf(configAvailable="*.disable_elytra_boost")
public abstract class MixinFireworkRocketItem {

	@FabInject(at=@At(value="INVOKE", target="Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"),
			method="use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;", cancellable=true)
	private void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
		if (FabConf.isEnabled("*.disable_elytra_boost") && ConfigPredicates.shouldRun("*.disable_elytra_boost", user)) cir.setReturnValue(TypedActionResult.pass(user.getStackInHand(hand)));
	}

}
