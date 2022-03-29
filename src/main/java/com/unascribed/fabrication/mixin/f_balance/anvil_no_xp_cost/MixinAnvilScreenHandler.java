package com.unascribed.fabrication.mixin.f_balance.anvil_no_xp_cost;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
@EligibleIf(configAvailable="*.anvil_no_xp_cost")
public abstract class MixinAnvilScreenHandler {

	@Shadow @Final
	private Property levelCost;

	@Inject(method="updateResult()V", at=@At("TAIL"))
	public void removeCost(CallbackInfo ci) {
		if (!FabConf.isEnabled("*.anvil_no_xp_cost")) return;
		levelCost.set(0);
	}

	@ModifyConstant(method="canTakeOutput(Lnet/minecraft/entity/player/PlayerEntity;Z)Z", constant=@Constant(intValue=0))
	public int allowZero(int i) {
		if (!(FabConf.isEnabled("*.anvil_no_xp_cost") && i == 0)) return i;
		return -1;
	}

}
