package com.unascribed.fabrication.mixin.f_balance.infinity_crossbows;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity.PickupPermission;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

@Mixin(CrossbowItem.class)
@EligibleIf(configAvailable="*.infinity_crossbows")
public class MixinCrossbowItem {

	@ModifyVariable(at=@At("HEAD"), method="loadProjectile(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;ZZ)Z",
			argsOnly=true, index=4)
	private static boolean modifyCreativeModeLoadProjectile(boolean creative, LivingEntity shooter, ItemStack crossbow) {
		if (FabConf.isEnabled("*.infinity_crossbows") && EnchantmentHelper.getLevel(Enchantments.INFINITY, crossbow) > 0) {
			return true;
		}
		return creative;
	}

	@Inject(at=@At(value="INVOKE", target="net/minecraft/item/ItemStack.damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V"),
			method="shoot(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;FZFFF)V",
			locals=LocalCapture.CAPTURE_FAILHARD)
	private static void shoot(World world, LivingEntity shooter, Hand hand, ItemStack crossbow, ItemStack projectile, float soundPitch, boolean creative, float speed, float divergence, float simulated,
			CallbackInfo ci, boolean firework, ProjectileEntity proj) {
		if (!FabConf.isEnabled("*.infinity_crossbows")) return;
		if (projectile.getItem() == Items.ARROW && EnchantmentHelper.getLevel(Enchantments.INFINITY, crossbow) > 0
				&& proj instanceof PersistentProjectileEntity && ((PersistentProjectileEntity)proj).pickupType == PickupPermission.ALLOWED) {
			((PersistentProjectileEntity)proj).pickupType = PickupPermission.CREATIVE_ONLY;
		}
	}

}
