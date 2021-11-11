package com.unascribed.fabrication.mixin.g_weird_tweaks.villager_trades_reset;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.TradeOfferList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
@EligibleIf(configAvailable="*.villager_trades_reset")
public abstract class MixinVillagerEntity  {

	@Shadow public abstract void setOffers(TradeOfferList offers);

	@Shadow protected abstract void fillRecipes();

	@Inject(method={"restock()V", "restockAndUpdateDemandBonus()V"}, at=@At("TAIL"))
	public void resetTrades(CallbackInfo ci){
		if(!MixinConfigPlugin.isEnabled("*.villager_trades_reset")) return;
		this.setOffers(new TradeOfferList());
		this.fillRecipes();
	}

}
