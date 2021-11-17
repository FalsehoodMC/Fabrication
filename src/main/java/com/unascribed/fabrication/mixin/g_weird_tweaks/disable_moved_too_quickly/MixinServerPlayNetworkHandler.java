package com.unascribed.fabrication.mixin.g_weird_tweaks.disable_moved_too_quickly;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ServerPlayNetworkHandler.class)
@EligibleIf(configAvailable="*.disable_moved_too_quickly")
public abstract class MixinServerPlayNetworkHandler {

	@ModifyConstant(constant={@Constant(floatValue=300.0F), @Constant(floatValue=100.0F)}, method="onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V")
	private float disableMoveTooQuick(float old) {
		if (!MixinConfigPlugin.isEnabled("*.disable_moved_too_quickly")) return old;
		return Float.MAX_VALUE;
	}

	@ModifyConstant(constant=@Constant(doubleValue=100.0), method="onVehicleMove(Lnet/minecraft/network/packet/c2s/play/VehicleMoveC2SPacket;)V")
	private double getMaxPlayerVehicleSpeed(double old) {
		if (!MixinConfigPlugin.isEnabled("*.disable_moved_too_quickly")) return old;
		return Double.MAX_VALUE;
	}
	
}
