package com.unascribed.fabrication.mixin.i_woina.no_experience;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.ModifyGetField;
import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.screen.EnchantmentScreenHandler;

@Mixin(EnchantmentScreenHandler.class)
@EligibleIf(configAvailable="*.no_experience")
public class MixinEnchantmentScreenHandler {

	@ModifyGetField(target="net/minecraft/entity/player/PlayerEntity.experienceLevel:I",
			method={
					"onButtonClick(Lnet/minecraft/entity/player/PlayerEntity;I)Z",
					/*"onContentChanged(Lnet/minecraft/inventory/Inventory;)V"*/
	})
	private static int fabrication$amendExperienceLevel(int old) {
		if (FabConf.isEnabled("*.no_experience")) return 65535;
		return old;
	}

}
