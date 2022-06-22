package com.unascribed.fabrication.mixin.b_utility.canhit;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.unascribed.fabrication.interfaces.SetCanHitList;
import com.unascribed.fabrication.logic.CanHitUtil;
import com.unascribed.fabrication.support.EligibleIf;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity.PickupPermission;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.world.World;

@Mixin(BowItem.class)
@EligibleIf(configAvailable="*.canhit")
public class MixinBowItem {

	@FabInject(at=@At(value="INVOKE", target="net/minecraft/item/ItemStack.damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V"),
			method="onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
			locals=LocalCapture.CAPTURE_FAILHARD)
	public void onStoppedUsing(ItemStack bowStack, World world, LivingEntity user, int remaining, CallbackInfo ci,
			PlayerEntity entity, boolean infinity, ItemStack arrowStack, int i, float f, boolean b2, ArrowItem ai, PersistentProjectileEntity arrow) {
		if (!FabConf.isEnabled("*.canhit") || CanHitUtil.isExempt(entity)) return;
		NbtList canHitList = bowStack.hasNbt() && bowStack.getNbt().contains("CanHit") ? bowStack.getNbt().getList("CanHit", NbtType.STRING) : null;
		NbtList canHitList2 = arrowStack.hasNbt() && arrowStack.getNbt().contains("CanHit") ? arrowStack.getNbt().getList("CanHit", NbtType.STRING) : null;
		if (arrow instanceof SetCanHitList) {
			((SetCanHitList)arrow).fabrication$setCanHitLists(canHitList, canHitList2);
			if (canHitList2 != null && arrow.pickupType == PickupPermission.ALLOWED) {
				arrow.pickupType = PickupPermission.CREATIVE_ONLY;
			}
		}
	}


}
