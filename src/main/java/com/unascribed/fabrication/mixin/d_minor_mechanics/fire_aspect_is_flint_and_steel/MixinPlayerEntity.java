package com.unascribed.fabrication.mixin.d_minor_mechanics.fire_aspect_is_flint_and_steel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

@Mixin(PlayerEntity.class)
@EligibleIf(configEnabled="*.fire_aspect_is_flint_and_steel")
public class MixinPlayerEntity {

	@Inject(at=@At("RETURN"), method="interact(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;",
			cancellable=true)
	public void interact(Entity e, Hand hand, CallbackInfoReturnable<ActionResult> ci) {
		if (MixinConfigPlugin.isEnabled("*.fire_aspect_is_flint_and_steel") && ci.getReturnValue() == ActionResult.PASS) {
			PlayerEntity self = (PlayerEntity)(Object)this;
			ItemStack stack = self.getStackInHand(hand);
			if (EnchantmentHelper.getLevel(Enchantments.FIRE_ASPECT, stack) > 0) {
				ItemStack flintAndSteel = new ItemStack(Items.FLINT_AND_STEEL);
				try {
					self.setStackInHand(hand, flintAndSteel);
					ActionResult ar = self.interact(e, hand);
					if (ar.isAccepted()) {
						self.swingHand(hand, true);
						self.world.playSound(null, e.getPos().x, e.getPos().y, e.getPos().z, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1, self.world.random.nextFloat() * 0.4f + 0.8f);
					}
					if (flintAndSteel.getDamage() > 0) {
						stack.damage(flintAndSteel.getDamage(), self, (en) -> {
							en.sendEquipmentBreakStatus(hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
						});
					}
					ci.setReturnValue(ar);
				} finally {
					self.setStackInHand(hand, stack);
				}
			}
		}
	}


}
