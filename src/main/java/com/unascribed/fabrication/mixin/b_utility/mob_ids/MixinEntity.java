package com.unascribed.fabrication.mixin.b_utility.mob_ids;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

@Mixin(Entity.class)
@EligibleIf(configEnabled="*.mob_ids", envMatches=Env.CLIENT)
public class MixinEntity {

	@Inject(at=@At("HEAD"), method="getCustomName()Lnet/minecraft/text/Text;", cancellable=true)
	public void getCustomName(CallbackInfoReturnable<Text> ci) {
		Entity e = ((Entity)(Object)this);
		if (!MixinConfigPlugin.isEnabled("*.mob_ids") || !e.world.isClient) return;
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player != null && mc.player.isCreative() && mc.options.debugEnabled) {
			ci.setReturnValue(new LiteralText(Integer.toString(e.getId())));
		}
	}

	@Inject(at=@At("HEAD"), method={"hasCustomName()Z","isCustomNameVisible()Z"}, cancellable=true)
	public void hasCustomNameAndIsVisible(CallbackInfoReturnable<Boolean> ci) {
		Entity e = ((Entity)(Object)this);
		if (!MixinConfigPlugin.isEnabled("*.mob_ids") || !e.world.isClient) return;
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player != null && mc.player.isCreative() && mc.options.debugEnabled) {
			ci.setReturnValue(true);
		}
	}

}
