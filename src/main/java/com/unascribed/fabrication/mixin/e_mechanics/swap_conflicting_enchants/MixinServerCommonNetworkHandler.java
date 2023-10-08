package com.unascribed.fabrication.mixin.e_mechanics.swap_conflicting_enchants;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.ByteBufCustomPayload;
import com.unascribed.fabrication.util.SwappingEnchants;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonNetworkHandler.class)
@EligibleIf(configAvailable="*.swap_conflicting_enchants")
public class MixinServerCommonNetworkHandler {

	@FabInject(at=@At("HEAD"), method="onCustomPayload(Lnet/minecraft/network/packet/c2s/common/CustomPayloadC2SPacket;)V", cancellable=true)
	public void onCustomPayload(CustomPayloadC2SPacket packet, CallbackInfo ci) {
		Object self = this;
		if (!(self instanceof ServerPlayNetworkHandler)) return;
		ServerPlayerEntity player = ((ServerPlayNetworkHandler) self).getPlayer();
		CustomPayload payload = packet.payload();
		if (!(payload instanceof ByteBufCustomPayload)) return;
		Identifier channel = payload.id();
		if (channel.getNamespace().equals("fabrication") && channel.getPath().equals("swap_conflicting_enchants")) {
			PacketByteBuf recvdData = ((ByteBufCustomPayload) payload).buf;
			if (recvdData.readBoolean()) {
				ItemStack stack = player.getMainHandStack();
				World world = player.getWorld();
				if (stack != null && !stack.isEmpty() && world != null) {
					SwappingEnchants.swapEnchants(stack, world, player);
				}
			}
			ci.cancel();
		}
	}

}
