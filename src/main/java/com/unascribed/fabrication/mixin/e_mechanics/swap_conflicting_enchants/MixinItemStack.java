package com.unascribed.fabrication.mixin.e_mechanics.swap_conflicting_enchants;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.SwappingEnchants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
@EligibleIf(configAvailable="*.swap_conflicting_enchants")
public abstract class MixinItemStack {

	@FabInject(method="use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;", at=@At("HEAD"), cancellable=true)
	public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
		if (SwappingEnchants.swapEnchants((ItemStack)(Object)this, world, user)) cir.setReturnValue(TypedActionResult.consume((ItemStack)(Object)this));
	}

}
