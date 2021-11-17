package com.unascribed.fabrication.mixin.f_balance.environmentally_friendly_creepers;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.world.explosion.Explosion.DestructionType;

@Mixin(CreeperEntity.class)
@EligibleIf(configAvailable="*.environmentally_friendly_creepers")
public class MixinCreeperEntity {

	@Redirect(at=@At(value="FIELD", opcode=Opcodes.GETSTATIC, target="net/minecraft/world/explosion/Explosion$DestructionType.DESTROY:Lnet/minecraft/world/explosion/Explosion$DestructionType;"),
			method="explode()V")
	public DestructionType nonMobGriefingDestructionType() {
		return MixinConfigPlugin.isEnabled("*.environmentally_friendly_creepers") ? DestructionType.NONE : DestructionType.DESTROY;
	}

}
