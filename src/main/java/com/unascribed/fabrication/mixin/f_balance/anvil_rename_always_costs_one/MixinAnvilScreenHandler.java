package com.unascribed.fabrication.mixin.f_balance.anvil_rename_always_costs_one;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;

@Mixin(AnvilScreenHandler.class)
@EligibleIf(configEnabled="*.anvil_rename_always_costs_one")
public abstract class MixinAnvilScreenHandler extends ForgingScreenHandler {

	public MixinAnvilScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
		super(type, syncId, playerInventory, context);
	}

	@Shadow @Final
	private Property levelCost;
	
	@Inject(at=@At("TAIL"), method="updateResult()V", expect=1)
	public void updateResult(CallbackInfo ci) {
		if (!MixinConfigPlugin.isEnabled("*.anvil_rename_always_costs_one")) return;
		if (this.input.getStack(1).isEmpty()) {
			levelCost.set(1);
		}
	}
	
}
