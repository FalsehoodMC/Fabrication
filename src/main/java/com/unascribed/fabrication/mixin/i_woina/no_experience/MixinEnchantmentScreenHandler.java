package com.unascribed.fabrication.mixin.i_woina.no_experience;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.EnchantmentScreenHandler;

@Mixin(EnchantmentScreenHandler.class)
@EligibleIf(configEnabled="*.no_experience")
public class MixinEnchantmentScreenHandler {

	@Redirect(at=@At(value="FIELD", target="net/minecraft/entity/player/PlayerEntity.experienceLevel:I"),
			method={
					"onButtonClick(Lnet/minecraft/entity/player/PlayerEntity;I)Z",
					"onContentChanged(Lnet/minecraft/inventory/Inventory;)V"
			})
	public int amendExperienceLevel(PlayerEntity subject) {
		if (MixinConfigPlugin.isEnabled("*.no_experience")) return 65535;
		return subject.experienceLevel;
	}
	
}
