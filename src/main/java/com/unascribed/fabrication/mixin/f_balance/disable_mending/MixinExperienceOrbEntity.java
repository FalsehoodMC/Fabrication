package com.unascribed.fabrication.mixin.f_balance.disable_mending;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ExperienceOrbEntity.class)
@EligibleIf(configEnabled="*.disable_mending")
public class MixinExperienceOrbEntity {

	@Inject(method = "repairPlayerGears(Lnet/minecraft/entity/player/PlayerEntity;I)I", at=@At("HEAD"), cancellable = true)
	public void no_repair(PlayerEntity player, int amount, CallbackInfoReturnable<Integer> cir) {
		if (MixinConfigPlugin.isEnabled("*.disable_mending")) cir.setReturnValue(amount);
	}
	
}
