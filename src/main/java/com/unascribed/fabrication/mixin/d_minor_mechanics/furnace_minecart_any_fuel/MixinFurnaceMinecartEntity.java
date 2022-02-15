package com.unascribed.fabrication.mixin.d_minor_mechanics.furnace_minecart_any_fuel;

import com.unascribed.fabrication.support.injection.ModifyReturn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

@Mixin(FurnaceMinecartEntity.class)
@EligibleIf(configAvailable="*.furnace_minecart_any_fuel")
public abstract class MixinFurnaceMinecartEntity extends AbstractMinecartEntity {

	protected MixinFurnaceMinecartEntity(EntityType<?> entityType, World world) {
		super(entityType, world);
	}

	@Shadow
	private int fuel;

	@Inject(at=@At("HEAD"), method="interact(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;")
	public void interact(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> ci) {
		if (!MixinConfigPlugin.isEnabled("*.furnace_minecart_any_fuel")) return;
		ItemStack itemStack = player.getStackInHand(hand);
		if (FurnaceBlockEntity.canUseAsFuel(itemStack)) {
			int value = FurnaceBlockEntity.createFuelTimeMap().get(itemStack.getItem())*2;
			if (this.fuel + value <= 32000) {
				if (!player.getAbilities().creativeMode) {
					itemStack.decrement(1);
				}
				this.fuel += value;
			}
		}
	}

	@ModifyReturn(target={"net/minecraft/recipe/Ingredient.test(Lnet/minecraft/item/ItemStack;)Z", "Lnet/minecraft/class_1856;method_8093(Lnet/minecraft/class_1799;)Z"},
			method={"interact(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;", "method_5688(Lnet/minecraft/class_1657;Lnet/minecraft/class_1268;)Lnet/minecraft/class_1269;"})
	private static boolean fabrication$disableVanillaFuel(boolean original) {
		return !MixinConfigPlugin.isEnabled("*.furnace_minecart_any_fuel") && original;
	}

}
