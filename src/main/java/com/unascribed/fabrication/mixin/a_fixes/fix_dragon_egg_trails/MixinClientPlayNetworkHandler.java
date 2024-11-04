package com.unascribed.fabrication.mixin.a_fixes.fix_dragon_egg_trails;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.ByteBufCustomPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(ClientPlayNetworkHandler.class)
@EligibleIf(configAvailable="*.fix_dragon_egg_trails", envMatches=Env.CLIENT)
public class MixinClientPlayNetworkHandler {
	private static final Random fabrication$RANDOM = new Random();

	@FabInject(at=@At("HEAD"), method="onCustomPayload(Lnet/minecraft/network/packet/CustomPayload;)V", cancellable=true)
	public void onCustomPayload(CustomPayload packet, CallbackInfo ci) {
		if (!(packet instanceof ByteBufCustomPayload)) return;
		if (((ByteBufCustomPayload) packet).id().getNamespace().equals("fabrication") && ((ByteBufCustomPayload) packet).id().getPath().equals("dragon_egg_trail")) {
			PacketByteBuf buf = ((ByteBufCustomPayload) packet).buf();
			BlockPos pos = buf.readBlockPos();
			BlockPos newPos = buf.readBlockPos();
			World world = MinecraftClient.getInstance().world;
			Random random = fabrication$RANDOM;
			if (world != null && world.isClient) {
				for(int j = 0; j < 128; ++j) {
					double d = random.nextDouble();
					float f = (random.nextFloat() - 0.5F) * 0.2F;
					float g = (random.nextFloat() - 0.5F) * 0.2F;
					float h = (random.nextFloat() - 0.5F) * 0.2F;
					double e = MathHelper.lerp(d, newPos.getX(), pos.getX()) + (random.nextDouble() - 0.5) + 0.5;
					double k = MathHelper.lerp(d, newPos.getY(), pos.getY()) + random.nextDouble() - 0.5;
					double l = MathHelper.lerp(d, newPos.getZ(), pos.getZ()) + (random.nextDouble() - 0.5) + 0.5;
					world.addParticle(ParticleTypes.PORTAL, e, k, l, f, g, h);
				}
			}
			ci.cancel();
		}
	}

}
