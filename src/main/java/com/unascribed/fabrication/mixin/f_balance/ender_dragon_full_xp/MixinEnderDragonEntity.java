package com.unascribed.fabrication.mixin.f_balance.ender_dragon_full_xp;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnderDragonEntity.class)
@EligibleIf(configAvailable="*.ender_dragon_full_xp")
public class MixinEnderDragonEntity {

	@ModifyVariable(method="updatePostDeath", at=@At(value="STORE", ordinal=0), ordinal=0)
	private int fullXp(int old){
		return MixinConfigPlugin.isEnabled("*.ender_dragon_full_xp") && old == 500 ? 1500 : old;
	}
}
