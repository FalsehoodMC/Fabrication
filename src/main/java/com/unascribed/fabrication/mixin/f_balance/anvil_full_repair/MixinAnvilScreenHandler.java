package com.unascribed.fabrication.mixin.f_balance.anvil_full_repair;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
@EligibleIf(configEnabled="*.anvil_full_repair")
public abstract class MixinAnvilScreenHandler {

	@ModifyArg(method = "updateResult()V", at=@At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setDamage(I)V"))
	public int fullrepair(int i) {
		if (MixinConfigPlugin.isEnabled("*.anvil_full_repair"))
			return 0;
		return i;
	}
}
