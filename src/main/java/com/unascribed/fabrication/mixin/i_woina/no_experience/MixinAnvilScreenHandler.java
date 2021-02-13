package com.unascribed.fabrication.mixin.i_woina.no_experience;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.AnvilScreenHandler;

@Mixin(AnvilScreenHandler.class)
@EligibleIf(configEnabled="*.no_experience")
public class MixinAnvilScreenHandler {

	@ModifyArg(at=@At(value="INVOKE", target="net/minecraft/screen/Property.set(I)V"), method="updateResult()V")
	public int modifyLevelCost(int lvl) {
		if (!MixinConfigPlugin.isEnabled("*.no_experience")) return lvl;
		return lvl >= 1 ? 1 : lvl;
	}
	
	@Inject(at=@At("HEAD"), method="canTakeOutput(Lnet/minecraft/entity/player/PlayerEntity;Z)Z", cancellable=true)
	public void canTakeOutput(PlayerEntity player, boolean present, CallbackInfoReturnable<Boolean> ci) {
		if (MixinConfigPlugin.isEnabled("*.no_experience")) {
			ci.setReturnValue(true);
		}
	}
	
}
