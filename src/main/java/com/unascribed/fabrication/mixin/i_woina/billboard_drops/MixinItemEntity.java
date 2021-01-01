package com.unascribed.fabrication.mixin.i_woina.billboard_drops;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.client.FlatItems;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;

@Mixin(ItemEntity.class)
@EligibleIf(configEnabled="*.billboard_drops", envMatches=Env.CLIENT)
public abstract class MixinItemEntity {
	
	@Shadow
	public ItemStack getStack() { return null; }
	
	@Inject(at=@At("HEAD"), method="method_27314(F)F", cancellable=true)
	public void getRotation(float partialTicks, CallbackInfoReturnable<Float> ci) {
		if (RuntimeChecks.check("*.billboard_drops") && FlatItems.hasGeneratedModel(getStack())) {
			ci.setReturnValue((float)Math.toRadians((-MinecraftClient.getInstance().gameRenderer.getCamera().getYaw())+180));
		}
	}
	
}
