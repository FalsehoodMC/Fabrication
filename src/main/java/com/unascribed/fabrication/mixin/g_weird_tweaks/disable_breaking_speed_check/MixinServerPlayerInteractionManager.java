package com.unascribed.fabrication.mixin.g_weird_tweaks.disable_breaking_speed_check;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ServerPlayerInteractionManager.class)
@EligibleIf(configAvailable="*.disable_breaking_speed_check")
public abstract class MixinServerPlayerInteractionManager {

	@ModifyConstant(constant=@Constant(floatValue=0.7F), method="processBlockBreakingAction(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/network/packet/c2s/play/PlayerActionC2SPacket$Action;Lnet/minecraft/util/math/Direction;I)V")
	private float disableMoveTooQuick(float old) {
		if (!MixinConfigPlugin.isEnabled("*.disable_breaking_speed_check")) return old;
		return 0.1F;
	}
	
}
