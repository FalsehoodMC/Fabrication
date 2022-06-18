package com.unascribed.fabrication.mixin.i_woina.void_fog_particles;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.util.DepthSuspendParticle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ClientWorld.class)
@EligibleIf(configAvailable="*.void_fog_particles", envMatches=Env.CLIENT)
public abstract class MixinClientWorld extends World {

	@Shadow
	@Final
	private MinecraftClient client;

	protected MixinClientWorld(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> registryEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
		super(properties, registryRef, registryEntry, profiler, isClient, debugWorld, seed);
	}

	//Ported from 1.7.10
	//note that the source did not render the particles in flat worlds, however this is client-side so that can't be checked
	//also note that i'm trash at locating mappings so any of the fairly many assumptions made could be wrong
	@Inject(at=@At("HEAD"), method="doRandomBlockDisplayTicks(III)V")
	public void voidParticles(int centerX, int centerY, int centerZ, CallbackInfo ci){
		if (!FabConf.isEnabled("*.void_fog_particles") || this.getDimension().hasCeiling()) return;
		int floor = this.getDimension().getMinimumY();
		if (floor+24<centerY) return;
		BlockPos.Mutable mutablePos = new BlockPos.Mutable();
		for(int i=0; i<1000; ++i) {
			int x = centerX + this.random.nextInt(16) - this.random.nextInt(16);
			int y = centerY + this.random.nextInt(16) - this.random.nextInt(16);
			int z = centerZ + this.random.nextInt(16) - this.random.nextInt(16);
			//Source checked for Material.AIR, assuming this is the same
			if(this.isAir(mutablePos.set(x, y, z))) {
				if(this.random.nextInt(8)+floor > y) {
					//Source specified "depthsuspend" particle, which has been removed
					//Source used nextFloat, see #483
					this.client.particleManager.addParticle(new DepthSuspendParticle((ClientWorld)(Object)this, x + this.random.nextDouble(), y + this.random.nextDouble(), z + this.random.nextDouble()));
				}
			}
		}
	}
}
