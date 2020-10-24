package com.unascribed.fabrication.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import com.unascribed.fabrication.interfaces.SetVelocity;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.particle.NoteParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;

@Mixin(NoteParticle.class)
@EligibleIf(configEnabled="*.less_restrictive_note_blocks", envMatches=Env.CLIENT)
public abstract class MixinLessRestrictiveNoteBlocksParticle extends Particle implements SetVelocity {

	protected MixinLessRestrictiveNoteBlocksParticle(ClientWorld world, double x, double y, double z) {
		super(world, x, y, z);
	}

	@Override
	public void fabrication$setVelocity(double vX, double vY, double vZ) {
		this.velocityX = vX;
		this.velocityY = vY;
		this.velocityZ = vZ;
	}
	
}
