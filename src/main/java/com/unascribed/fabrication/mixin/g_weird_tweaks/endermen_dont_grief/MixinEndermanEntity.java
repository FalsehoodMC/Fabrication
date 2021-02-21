package com.unascribed.fabrication.mixin.g_weird_tweaks.endermen_dont_grief;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.Key;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = {"net.minecraft.entity.mob.EndermanEntity$PickUpBlockGoal","net.minecraft.entity.mob.EndermanEntity$PlaceBlockGoal"})
@EligibleIf(configEnabled="*.endermen_dont_grief")
public class MixinEndermanEntity {
	@Redirect(method = "canStart()Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
	private boolean canStart(GameRules gameRules, Key<GameRules.BooleanRule> rule) {
		return !MixinConfigPlugin.isEnabled("*.endermen_dont_grief");
	}
}
