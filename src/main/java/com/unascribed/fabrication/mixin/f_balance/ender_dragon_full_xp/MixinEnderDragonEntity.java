package com.unascribed.fabrication.mixin.f_balance.ender_dragon_full_xp;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabModifyVariable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;

@Mixin(EnderDragonEntity.class)
@EligibleIf(configAvailable="*.ender_dragon_full_xp")
public class MixinEnderDragonEntity {

	@FabModifyVariable(method="updatePostDeath()V", at=@At(value="STORE", ordinal=0), ordinal=0)
	private int fullXp(int old){
		return FabConf.isEnabled("*.ender_dragon_full_xp") && old == 500 ? 1500 : old;
	}
}
