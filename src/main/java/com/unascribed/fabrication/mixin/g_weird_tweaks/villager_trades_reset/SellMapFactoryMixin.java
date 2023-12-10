package com.unascribed.fabrication.mixin.g_weird_tweaks.villager_trades_reset;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabModifyArg;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets="net.minecraft.village.TradeOffers$SellMapFactory")
@EligibleIf(configAvailable="*.villager_trades_reset")
public class SellMapFactoryMixin {

	@FabModifyArg(method="create(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/random/Random;)Lnet/minecraft/village/TradeOffer;", at=@At(value="INVOKE", target="Lnet/minecraft/server/world/ServerWorld;locateStructure(Lnet/minecraft/tag/TagKey;Lnet/minecraft/util/math/BlockPos;IZ)Lnet/minecraft/util/math/BlockPos;"))
	private boolean fabrication$dupeMaps(boolean dupe) {
		if (FabConf.isEnabled("*.villager_trades_reset")) return false;
		return dupe;
	}

}
