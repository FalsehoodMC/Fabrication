package com.unascribed.fabrication.mixin.i_woina.instant_bow;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

@Mixin(BowItem.class)
@EligibleIf(configAvailable="*.instant_bow")
public abstract class MixinBowItem {

	@Shadow
	public abstract void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks);

	@Inject(method="use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;",
			at=@At(value="INVOKE", target="Lnet/minecraft/entity/player/PlayerEntity;setCurrentHand(Lnet/minecraft/util/Hand;)V", shift=At.Shift.BEFORE), cancellable=true)
	private void getUseAction(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir){
		if(!(MixinConfigPlugin.isEnabled("*.instant_bow") && ConfigPredicates.shouldRun("*.instant_bow", user))) return;
		ItemStack itemStack = user.getStackInHand(hand);
		onStoppedUsing(itemStack, world, user, 0);
		cir.setReturnValue(TypedActionResult.fail(itemStack));
	}
}
