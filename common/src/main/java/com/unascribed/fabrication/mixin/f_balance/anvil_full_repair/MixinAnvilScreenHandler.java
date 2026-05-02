package com.unascribed.fabrication.mixin.f_balance.anvil_full_repair;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabModifyArg;
import net.minecraft.screen.AnvilScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AnvilScreenHandler.class)
@EligibleIf(configAvailable="*.anvil_full_repair")
public abstract class MixinAnvilScreenHandler {

	@FabModifyArg(method = "updateResult()V", at=@At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setDamage(I)V"))
	public int fullrepair(int i) {
		if (FabConf.isEnabled("*.anvil_full_repair"))
			return 0;
		return i;
	}
}
