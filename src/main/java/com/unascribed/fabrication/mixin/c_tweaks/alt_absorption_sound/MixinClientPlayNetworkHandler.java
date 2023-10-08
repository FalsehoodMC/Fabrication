package com.unascribed.fabrication.mixin.c_tweaks.alt_absorption_sound;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.ByteBufCustomPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
@EligibleIf(configAvailable="*.alt_absorption_sound", envMatches=Env.CLIENT)
public class MixinClientPlayNetworkHandler {

	@FabInject(at=@At("HEAD"), method="onCustomPayload(Lnet/minecraft/network/packet/CustomPayload;)V", cancellable=true)
	public void onCustomPayload(CustomPayload payload, CallbackInfo ci) {
		if (!(payload instanceof ByteBufCustomPayload)) return;
		if (payload.id().getNamespace().equals("fabrication") && payload.id().getPath().equals("play_absorp_sound")) {
			if (FabConf.isEnabled("*.alt_absorption_sound")) {
				int id = ((ByteBufCustomPayload) payload).buf.readInt();
				MinecraftClient.getInstance().send(() -> {
					World world = MinecraftClient.getInstance().world;
					if (world != null) {
						Entity e = world.getEntityById(id);
						e.timeUntilRegen = 20;
						if (e instanceof LivingEntity) {
							((LivingEntity)e).limbAnimator.setSpeed(1.5f);
							((LivingEntity)e).hurtTime = ((LivingEntity)e).maxHurtTime = 10;
						}
						world.playSound(e.getPos().x, e.getPos().y, e.getPos().z, FabricationMod.ABSORPTION_HURT, e.getSoundCategory(), 1.0f, 0.75f+(world.random.nextFloat()/2), false);
					}
				});
			}
			ci.cancel();
		}
	}

}
