package com.unascribed.fabrication.mixin.c_tweaks.legible_signs;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.block.MapColor;
import net.minecraft.util.DyeColor;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DyeColor.class)
@EligibleIf(configEnabled="*.legible_signs", envMatches=Env.CLIENT)
public class MixinDyeColor {;

	@Dynamic
	int fabrication$color;

	@Inject(at=@At("TAIL"), method="<init>(ILjava/lang/String;ILnet/minecraft/block/MapColor;II)V")
	private void modifySignTextColor(String enumName, int ordinal, int woolId, String name, int color, MapColor mapColor, int fireworkColor, int signColor, CallbackInfo ci) {
		fabrication$color = color;
	}
	@Inject(at=@At("HEAD"), method= "getSignColor()I", cancellable = true)
	public void signColorToColor(CallbackInfoReturnable<Integer> cir){
		if (MixinConfigPlugin.isEnabled("*.legible_signs"))
			cir.setReturnValue(fabrication$color);
	}
}
