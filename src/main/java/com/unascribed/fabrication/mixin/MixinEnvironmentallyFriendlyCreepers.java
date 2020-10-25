package com.unascribed.fabrication.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.world.explosion.Explosion.DestructionType;

@Mixin(CreeperEntity.class)
@EligibleIf(configEnabled="*.environmentally_friendly_creepers")
public class MixinEnvironmentallyFriendlyCreepers {

	@Redirect(at=@At(value="FIELD", opcode=Opcodes.GETSTATIC, target="net/minecraft/world/explosion/Explosion$DestructionType.DESTROY"),
			method="explode()V")
	public DestructionType nonMobGriefingDestructionType() {
		return RuntimeChecks.check("*.environmentally_friendly_creepers") ? DestructionType.NONE : DestructionType.DESTROY;
	}
	
}
