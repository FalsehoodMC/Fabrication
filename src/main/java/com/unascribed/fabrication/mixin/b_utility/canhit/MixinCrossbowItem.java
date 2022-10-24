package com.unascribed.fabrication.mixin.b_utility.canhit;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.unascribed.fabrication.interfaces.SetCanHitList;
import com.unascribed.fabrication.logic.CanHitUtil;
import com.unascribed.fabrication.support.EligibleIf;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity.PickupPermission;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

@Mixin(CrossbowItem.class)
@EligibleIf(configAvailable="*.canhit")
public class MixinCrossbowItem {

	@FabInject(at=@At(value="INVOKE", target="net/minecraft/item/ItemStack.damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V"),
			method="shoot(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;FZFFF)V",
			locals=LocalCapture.CAPTURE_FAILHARD)
	private static void shoot(World world, LivingEntity shooter, Hand hand, ItemStack crossbow, ItemStack projectile, float soundPitch, boolean creative, float speed, float divergence, float simulated,
			CallbackInfo ci, boolean firework, ProjectileEntity proj) {
		if (!FabConf.isEnabled("*.canhit") || CanHitUtil.isExempt(shooter)) return;
		NbtList canHitList = crossbow.hasTag() && crossbow.getTag().contains("CanHit") ? crossbow.getTag().getList("CanHit", NbtType.STRING) : null;
		NbtList canHitList2 = projectile.hasTag() && projectile.getTag().contains("CanHit") ? projectile.getTag().getList("CanHit", NbtType.STRING) : null;
		if (proj instanceof SetCanHitList) {
			((SetCanHitList)proj).fabrication$setCanHitLists(canHitList, canHitList2);
			if (canHitList2 != null && proj instanceof PersistentProjectileEntity && ((PersistentProjectileEntity)proj).pickupType == PickupPermission.ALLOWED) {
				((PersistentProjectileEntity)proj).pickupType = PickupPermission.CREATIVE_ONLY;
			}
		}
	}


}
