package com.unascribed.fabrication.mixin.d_minor_mechanics.fire_aspect_is_flint_and_steel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

@Mixin(ServerPlayerInteractionManager.class)
@EligibleIf(configEnabled="*.fire_aspect_is_flint_and_steel")
public class MixinServerPlayerInteractionManager {

	@Inject(at=@At("RETURN"), method="interactBlock(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;",
		cancellable=true)
	public void interactBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> ci) {
		if (MixinConfigPlugin.isEnabled("*.fire_aspect_is_flint_and_steel") && ci.getReturnValue() == ActionResult.PASS) {
			if (EnchantmentHelper.getLevel(Enchantments.FIRE_ASPECT, stack) > 0) {
				ServerPlayerInteractionManager self = (ServerPlayerInteractionManager)(Object)this;
				ItemStack flintAndSteel = new ItemStack(Items.FLINT_AND_STEEL);
				try {
					player.setStackInHand(hand, flintAndSteel);
					ActionResult ar = self.interactBlock(player, world, flintAndSteel, hand, hitResult);
					if (ar.isAccepted()) {
						player.swingHand(hand, true);
						world.playSound(null, hitResult.getBlockPos(), SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1, world.random.nextFloat() * 0.4f + 0.8f);
					}
					if (flintAndSteel.getDamage() > 0) {
						stack.damage(flintAndSteel.getDamage(), world.random, player);
					}
					ci.setReturnValue(ar);
				} finally {
					player.setStackInHand(hand, stack);
				}
			}
		}
	}


}
