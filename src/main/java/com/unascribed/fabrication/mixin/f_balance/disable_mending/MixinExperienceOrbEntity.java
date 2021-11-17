package com.unascribed.fabrication.mixin.f_balance.disable_mending;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.item.ItemStack;

@Mixin(ExperienceOrbEntity.class)
@EligibleIf(configEnabled="*.disable_mending")
public class MixinExperienceOrbEntity {

	/*
	For 1.17+

	@Inject(method = "repairPlayerGears(Lnet/minecraft/entity/player/PlayerEntity;I)I", at=@At("HEAD"), cancellable = true)
	public void staphhammertiem(PlayerEntity player, int amount, CallbackInfoReturnable<Integer> cir) {
		if (MixinConfigPlugin.isEnabled("*.disable_mending")) cir.setReturnValue(amount);
	}
	*/
	@ModifyVariable(at=@At("STORE"), method="onPlayerCollision(Lnet/minecraft/entity/player/PlayerEntity;)V")
	public Map.Entry<EquipmentSlot, ItemStack> getRepairCost(Map.Entry<EquipmentSlot, ItemStack> old) {
		if (MixinConfigPlugin.isEnabled("*.disable_mending"))
			return null;
		return old;
	}

}
