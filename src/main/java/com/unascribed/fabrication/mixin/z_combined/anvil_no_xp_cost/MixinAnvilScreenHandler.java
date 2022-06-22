package com.unascribed.fabrication.mixin.z_combined.anvil_no_xp_cost;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilScreenHandler.class)
@EligibleIf(anyConfigAvailable={"*.no_experience", "*.anvil_no_xp_cost"})
public class MixinAnvilScreenHandler {

	@Shadow @Final
	private Property levelCost;

	@FabInject(at=@At("TAIL"), method="updateResult()V")
	public void modifyLevelCost(CallbackInfo ci) {
		if (!(FabConf.isEnabled("*.no_experience") || FabConf.isEnabled("*.anvil_no_xp_cost"))) return;
		levelCost.set(1);
	}

	@FabInject(at=@At("HEAD"), method="canTakeOutput(Lnet/minecraft/entity/player/PlayerEntity;Z)Z", cancellable=true)
	public void canTakeOutput(PlayerEntity player, boolean present, CallbackInfoReturnable<Boolean> ci) {
		if (FabConf.isEnabled("*.no_experience") || FabConf.isEnabled("*.anvil_no_xp_cost")) {
			ci.setReturnValue(true);
		}
	}

	@FabInject(at=@At("HEAD"), method="getLevelCost()I", cancellable=true)
	public void getLevelCost(CallbackInfoReturnable<Integer> ci) {
		if (FabConf.isEnabled("*.no_experience")) {
			ci.setReturnValue(0);
		}
	}

}
