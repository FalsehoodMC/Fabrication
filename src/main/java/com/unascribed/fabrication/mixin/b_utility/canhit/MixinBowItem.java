package com.unascribed.fabrication.mixin.b_utility.canhit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.unascribed.fabrication.CanHitUtil;
import com.unascribed.fabrication.interfaces.SetCanHitList;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity.PickupPermission;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.World;

@Mixin(BowItem.class)
@EligibleIf(configEnabled="*.canhit")
public class MixinBowItem {

	@Inject(at=@At(value="INVOKE", target="net/minecraft/item/ItemStack.damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V"),
			method="onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
			locals=LocalCapture.CAPTURE_FAILHARD, expect=1)
	public void onStoppedUsing(ItemStack bowStack, World world, LivingEntity user, int remaining, CallbackInfo ci,
			PlayerEntity entity, boolean infinity, ItemStack arrowStack, int i, float f, boolean b2, ArrowItem ai, PersistentProjectileEntity arrow) {
		if (!MixinConfigPlugin.isEnabled("*.canhit") || CanHitUtil.isExempt(entity)) return;
		ListTag canHitList = bowStack.hasTag() && bowStack.getTag().contains("CanHit") ? bowStack.getTag().getList("CanHit", NbtType.STRING) : null;
		ListTag canHitList2 = arrowStack.hasTag() && arrowStack.getTag().contains("CanHit") ? arrowStack.getTag().getList("CanHit", NbtType.STRING) : null;
		if (arrow instanceof SetCanHitList) {
			((SetCanHitList)arrow).fabrication$setCanHitLists(canHitList, canHitList2);
			if (canHitList2 != null && arrow.pickupType == PickupPermission.ALLOWED) {
				arrow.pickupType = PickupPermission.CREATIVE_ONLY;
			}
		}
	}

	
}
