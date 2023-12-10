package com.unascribed.fabrication.mixin.f_balance.environmentally_friendly_creepers;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.SpecialEligibility;
import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.support.injection.HijackReturn;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(World.class)
@EligibleIf(configAvailable="*.environmentally_friendly_creepers")
@FailOn(invertedSpecialConditions= SpecialEligibility.NOT_FORGE)
public class MixinWorld {

	@Hijack(target="Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z",
			method="createExplosion(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/World$ExplosionSourceType;ZLnet/minecraft/particle/ParticleEffect;Lnet/minecraft/particle/ParticleEffect;Lnet/minecraft/sound/SoundEvent;)Lnet/minecraft/world/explosion/Explosion;")
	private static HijackReturn fabrication$nonMobGriefingDestructionType(GameRules rules, GameRules.Key<GameRules.BooleanRule> gamerule) {
		return FabConf.isEnabled("*.environmentally_friendly_creepers") && gamerule == GameRules.DO_MOB_GRIEFING ? HijackReturn.FALSE : null;
	}

}
