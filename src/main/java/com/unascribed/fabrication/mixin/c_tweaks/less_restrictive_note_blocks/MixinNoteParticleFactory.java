package com.unascribed.fabrication.mixin.c_tweaks.less_restrictive_note_blocks;

import net.minecraft.client.particle.NoteParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.interfaces.SetVelocity;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.Direction;

@Mixin(NoteParticle.Factory.class)
@EligibleIf(configAvailable="*.less_restrictive_note_blocks", envMatches=Env.CLIENT)
public class MixinNoteParticleFactory {

	@FabInject(at=@At("RETURN"), method="createParticle(Lnet/minecraft/particle/DefaultParticleType;Lnet/minecraft/client/world/ClientWorld;DDDDDD)Lnet/minecraft/client/particle/Particle;")
	public void createParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double vX, double vY, double vZ, CallbackInfoReturnable<Particle> ci) {
		Direction dir = Direction.byId((int)vZ+1);
		Particle p = ci.getReturnValue();
		if (p instanceof SetVelocity) {
			((SetVelocity)p).fabrication$setVelocity(dir.getOffsetX()*0.2, dir.getOffsetY()*0.2, dir.getOffsetZ()*0.2);
		}
	}

}
