package com.unascribed.fabrication.mixin.e_mechanics.swap_conflicting_enchants;

import com.unascribed.fabrication.FabRefl;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.SwappingEnchants;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
@EligibleIf(configAvailable="*.swap_conflicting_enchants")
public class MixinServerPlayNetworkHandler {

	@Shadow
	public ServerPlayerEntity player;

	@FabInject(at=@At("HEAD"), method="onCustomPayload(Lnet/minecraft/network/packet/c2s/play/CustomPayloadC2SPacket;)V", cancellable=true)
	public void onCustomPayload(CustomPayloadC2SPacket packet, CallbackInfo ci) {
		Identifier channel = FabRefl.getChannel(packet);
		if (channel.getNamespace().equals("fabrication") && channel.getPath().equals("swap_conflicting_enchants")) {
			PacketByteBuf recvdData = FabRefl.getData(packet);
			if (recvdData.readBoolean()) {
				ItemStack stack = player.getMainHandStack();
				if (stack != null && !stack.isEmpty() && player.world != null) {
					SwappingEnchants.swapEnchants(stack, player.world, player);
				}
			}
			ci.cancel();
		}
	}

}
