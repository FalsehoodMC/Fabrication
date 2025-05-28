package com.unascribed.fabrication.mixin.h_unsafe.disable_moved_too_quickly;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayNetworkHandler.class)
@EligibleIf(configAvailable="*.disable_moved_too_quickly")
public abstract class MixinServerPlayNetworkHandler {

	@ModifyReturn(method={"onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V", "onVehicleMove(Lnet/minecraft/network/packet/c2s/play/VehicleMoveC2SPacket;)V"},
		target="Lnet/minecraft/server/network/ServerPlayNetworkHandler;isHost()Z")
	private boolean disableMoveTooQuick(boolean old) {
		return FabConf.isEnabled("*.disable_moved_too_quickly") || old;
	}

}
