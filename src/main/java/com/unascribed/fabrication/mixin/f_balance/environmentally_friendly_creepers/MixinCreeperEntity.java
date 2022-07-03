package com.unascribed.fabrication.mixin.f_balance.environmentally_friendly_creepers;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.support.injection.HijackReturn;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CreeperEntity.class)
@EligibleIf(configAvailable="*.environmentally_friendly_creepers")
public class MixinCreeperEntity {

	@Hijack(target="Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z",
			method="explode()V")
	private static HijackReturn fabrication$nonMobGriefingDestructionType(GameRules rules, GameRules.Key<GameRules.BooleanRule> gamerule) {
		return FabConf.isEnabled("*.environmentally_friendly_creepers") && gamerule == GameRules.DO_MOB_GRIEFING ? new HijackReturn(false) : null;
	}

}
