package com.unascribed.fabrication.mixin.c_tweaks.alt_absorption_sound;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;

@Mixin(ClientPlayNetworkHandler.class)
@EligibleIf(configEnabled="*.sync_attacker_yaw", envMatches=Env.CLIENT)
public class MixinClientPlayNetworkHandler {
	
	@Shadow @Final
	private ClientConnection connection;
	
	@Inject(at=@At("HEAD"), method="onCustomPayload(Lnet/minecraft/network/packet/s2c/play/CustomPayloadS2CPacket;)V", cancellable=true)
	public void onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
		if (packet.getChannel().getNamespace().equals("fabrication") && packet.getChannel().getPath().equals("play_absorp_sound")) {
			if (MixinConfigPlugin.isEnabled("*.alt_absorption_sound")) {
				if (MinecraftClient.getInstance().world != null) {
					PacketByteBuf buf = packet.getData();
					Entity e = MinecraftClient.getInstance().world.getEntityById(buf.readInt());
					e.timeUntilRegen = 20;
					if (e instanceof LivingEntity) {
						((LivingEntity)e).limbDistance = 1.5f;
						((LivingEntity)e).hurtTime = ((LivingEntity)e).maxHurtTime = 10;
					}
					e.world.playSound(e.getPos().x, e.getPos().y, e.getPos().z, FabricationMod.ABSORPTION_HURT, e.getSoundCategory(), 1.0f, 0.75f+(e.world.random.nextFloat()/2), false);
				}
			}
			ci.cancel();
		}
	}
	
}
