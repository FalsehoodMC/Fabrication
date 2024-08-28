package com.unascribed.fabrication.mixin.a_fixes.fix_dragon_egg_trails;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
@EligibleIf(configAvailable="*.fix_dragon_egg_trails", envMatches=Env.CLIENT)
public class MixinClientPlayNetworkHandler {

	@FabInject(at=@At("HEAD"), method="onCustomPayload(Lnet/minecraft/network/packet/s2c/play/CustomPayloadS2CPacket;)V", cancellable=true)
	public void onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
		if (packet.getChannel().getNamespace().equals("fabrication") && packet.getChannel().getPath().equals("dragon_egg_trail")) {
			PacketByteBuf buf = packet.getData();
			BlockPos pos = buf.readBlockPos();
			BlockPos newPos = buf.readBlockPos();
			World world = MinecraftClient.getInstance().world;
			if (world != null && world.isClient) {
				for(int j = 0; j < 128; ++j) {
					double d = world.random.nextDouble();
					float f = (world.random.nextFloat() - 0.5F) * 0.2F;
					float g = (world.random.nextFloat() - 0.5F) * 0.2F;
					float h = (world.random.nextFloat() - 0.5F) * 0.2F;
					double e = MathHelper.lerp(d, newPos.getX(), pos.getX()) + (world.random.nextDouble() - 0.5) + 0.5;
					double k = MathHelper.lerp(d, newPos.getY(), pos.getY()) + world.random.nextDouble() - 0.5;
					double l = MathHelper.lerp(d, newPos.getZ(), pos.getZ()) + (world.random.nextDouble() - 0.5) + 0.5;
					world.addParticle(ParticleTypes.PORTAL, e, k, l, f, g, h);
				}
			}
			ci.cancel();
		}
	}

}
