package com.unascribed.fabrication.mixin.i_woina.more_explosion_particles;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Explosion.class)
@EligibleIf(configAvailable="*.more_explosion_particles", envMatches=Env.CLIENT)
public class MixinExplosion {

	@Shadow @Final
	private World world;

	@Shadow @Final
	private double x;

	@Shadow @Final
	private double y;

	@Shadow @Final
	private double z;

	@Shadow @Final
	private float power;

	@Shadow @Final
	private ObjectArrayList<BlockPos> affectedBlocks;

	@Inject(method="affectWorld(Z)V", at=@At("HEAD"))
	private void oldParticles(boolean particles, CallbackInfo ci) {
		if (!(FabConf.isEnabled("*.more_explosion_particles") && particles)) return;
		for (BlockPos blockPos : affectedBlocks) {
			double double_1 = ((float) blockPos.getX() + this.world.random.nextFloat());
			double double_2 = ((float) blockPos.getY() + this.world.random.nextFloat());
			double double_3 = ((float) blockPos.getZ() + this.world.random.nextFloat());
			double double_4 = double_1 - this.x;
			double double_5 = double_2 - this.y;
			double double_6 = double_3 - this.z;
			double double_7 = MathHelper.sqrt((float) (double_4 * double_4 + double_5 * double_5 + double_6 * double_6));
			double_4 /= double_7;
			double_5 /= double_7;
			double_6 /= double_7;
			double double_8 = 0.5D / (double_7 / (double) this.power + 0.1D);
			double_8 *= (this.world.random.nextFloat() * this.world.random.nextFloat() + 0.3F);
			double_4 *= double_8;
			double_5 *= double_8;
			double_6 *= double_8;
			this.world.addParticle(ParticleTypes.POOF, (double_1 + this.x) / 2.0D, (double_2 + this.y) / 2.0D, (double_3 + this.z) / 2.0D, double_4, double_5, double_6);
			this.world.addParticle(ParticleTypes.SMOKE, double_1, double_2, double_3, double_4, double_5, double_6);
		}
	}

}
