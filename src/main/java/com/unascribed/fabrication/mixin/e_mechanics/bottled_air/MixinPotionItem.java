package com.unascribed.fabrication.mixin.e_mechanics.bottled_air;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(PotionItem.class)
@EligibleIf(configAvailable="*.bottled_air")
public class MixinPotionItem {

	@Inject(at=@At("RETURN"), method="finishUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;", cancellable=true)
	public void finishUsing(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> ci) {
		if (MixinConfigPlugin.isEnabled("*.bottled_air") && ci.getReturnValue().getItem() == Items.GLASS_BOTTLE && user.isSubmergedInWater()) {
			ci.setReturnValue(PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER));
		}
	}

	@ModifyArgs(at=@At(value="INVOKE", target="Lnet/minecraft/entity/player/PlayerInventory;insertStack(Lnet/minecraft/item/ItemStack;)Z"),
			method="finishUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;")
	public void fabrication$bottledAir(Args args) {
		if (MixinConfigPlugin.isEnabled("*.bottled_air") && ((ItemStack)args.get(1)).getItem() == Items.GLASS_BOTTLE && ((PlayerInventory)args.get(0)).player.isSubmergedInWater()) {
			args.set(1, PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER));
		}
	}

}
