package com.unascribed.fabrication.mixin.f_balance.pickup_skeleton_arrows;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.TagStrayArrow;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.mob.StrayEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(StrayEntity.class)
@EligibleIf(configAvailable="*.pickup_skeleton_arrows")
public abstract class MixinStrayEntity {

	@ModifyVariable(at=@At("STORE"), method="createArrowProjectile(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/projectile/PersistentProjectileEntity;")
	public PersistentProjectileEntity createArrowProjectile(PersistentProjectileEntity arrow) {
		if(FabConf.isEnabled("*.pickup_skeleton_arrows") && arrow instanceof TagStrayArrow) {
			((TagStrayArrow)arrow).fabrication$firedByStray();
		}
		return arrow;
	}
}
