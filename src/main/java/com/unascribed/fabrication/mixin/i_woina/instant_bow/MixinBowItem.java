package com.unascribed.fabrication.mixin.i_woina.instant_bow;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
@EligibleIf(configEnabled="*.instant_bow")
public class MixinBowItem {

	@Inject(method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;",
			at = @At("HEAD"), cancellable = true)
	private void getUseAction(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir){
		if(MixinConfigPlugin.isEnabled("*.instant_bow")) {
			ItemStack itemStack = user.getStackInHand(hand);
			Object self = this;
			if (user.getAbilities().creativeMode || !user.getArrowType(itemStack).isEmpty()) {
				((BowItem) self).onStoppedUsing(itemStack, world, user, 0);
			}
			cir.setReturnValue(TypedActionResult.fail(itemStack));
		}
	}
}
