package com.unascribed.fabrication.mixin.f_balance.ender_dragon_full_xp;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnderDragonEntity.class)
@EligibleIf(configEnabled="*.ender_dragon_full_xp")
public class MixinEnderDragonEntity {

	@Redirect(method = "updatePostDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/boss/dragon/EnderDragonFight;hasPreviouslyKilled()Z"))
	public boolean hasPreviouslyKilled(EnderDragonFight enderDragonFight){
		return !MixinConfigPlugin.isEnabled("*.ender_dragon_full_xp") && enderDragonFight.hasPreviouslyKilled();
	}
}
