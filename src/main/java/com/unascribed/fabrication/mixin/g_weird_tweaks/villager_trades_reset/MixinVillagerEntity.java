package com.unascribed.fabrication.mixin.g_weird_tweaks.villager_trades_reset;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerData;
import net.minecraft.world.World;

@Mixin(VillagerEntity.class)
@EligibleIf(configAvailable="*.villager_trades_reset")
public abstract class MixinVillagerEntity extends MerchantEntity {

	public MixinVillagerEntity(EntityType<? extends MerchantEntity> entityType, World world) {
		super(entityType, world);
	}

	@Shadow public abstract void setOffers(TradeOfferList offers);

	@Override
	@Shadow protected abstract void fillRecipes();

	@Shadow public abstract VillagerData getVillagerData();

	@Inject(method={"restock()V", "restockAndUpdateDemandBonus()V"}, at=@At("TAIL"))
	public void resetTrades(CallbackInfo ci){
		if(!MixinConfigPlugin.isEnabled("*.villager_trades_reset")) return;
		TradeOfferList tradeOfferList = new TradeOfferList();
		VillagerData villagerData = this.getVillagerData();
		Int2ObjectMap<TradeOffers.Factory[]> int2ObjectMap = TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(villagerData.getProfession());
		if (int2ObjectMap != null && !int2ObjectMap.isEmpty()) {
			for (int i = 1; i<=villagerData.getLevel(); i++) {
				TradeOffers.Factory[] factorys = int2ObjectMap.get(i);
				if (factorys != null) this.fillRecipesFromPool(tradeOfferList, factorys, 1);
			}
			this.setOffers(tradeOfferList);
		}
	}

}
