package com.unascribed.fabrication.mixin.b_utility.canhit;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.unascribed.fabrication.interfaces.SetCanHitList;
import com.unascribed.fabrication.logic.CanHitUtil;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity.PickupPermission;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;

import java.util.List;

@Mixin(RangedWeaponItem.class)
@EligibleIf(configAvailable="*.canhit")
public class MixinRangedWeaponItem {

	@FabInject(at=@At(value="INVOKE", target="Lnet/minecraft/item/ItemStack;damage(ILnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;)V"),
			method="shootAll(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;Ljava/util/List;FFZLnet/minecraft/entity/LivingEntity;)V",
			locals=LocalCapture.CAPTURE_FAILHARD)
	public void shootAll$bow(ServerWorld world, LivingEntity shooter, Hand hand, ItemStack bowStack, List<ItemStack> projectiles, float speed, float divergence, boolean critical, LivingEntity target, CallbackInfo ci, float f, float g, float h, float i, int j, ItemStack arrowStack, float k, ProjectileEntity arrow) {
		if (!FabConf.isEnabled("*.canhit") || !(bowStack.getItem() instanceof BowItem) || CanHitUtil.isExempt(shooter)) return;
		NbtList canHitList = bowStack.contains(DataComponentTypes.CUSTOM_DATA) && bowStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).getNbt().contains("CanHit") ? bowStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).getNbt().getList("CanHit", NbtElement.STRING_TYPE) : null;
		NbtList canHitList2 = arrowStack.contains(DataComponentTypes.CUSTOM_DATA) && arrowStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).getNbt().contains("CanHit") ? arrowStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).getNbt().getList("CanHit", NbtElement.STRING_TYPE) : null;
		if (arrow instanceof SetCanHitList) {
			((SetCanHitList)arrow).fabrication$setCanHitLists(canHitList, canHitList2);
			if (arrow instanceof PersistentProjectileEntity && canHitList2 != null && ((PersistentProjectileEntity)arrow).pickupType == PickupPermission.ALLOWED) {
				((PersistentProjectileEntity)arrow).pickupType = PickupPermission.CREATIVE_ONLY;
			}
		}
	}

	@FabInject(at=@At(value="INVOKE", target="Lnet/minecraft/item/ItemStack;damage(ILnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;)V"),
		method="shootAll(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;Ljava/util/List;FFZLnet/minecraft/entity/LivingEntity;)V",
		locals=LocalCapture.CAPTURE_FAILHARD)
	public void shootAll$crossbow(ServerWorld world, LivingEntity shooter, Hand hand, ItemStack crossbow, List<ItemStack> projectiles, float speed, float divergence, boolean critical, LivingEntity target, CallbackInfo ci, float f, float g, float h, float i, int j, ItemStack projectile, float k, ProjectileEntity proj) {
		if (!FabConf.isEnabled("*.canhit") || !(crossbow.getItem() instanceof CrossbowItem) || CanHitUtil.isExempt(shooter)) return;
		NbtList canHitList = crossbow.contains(DataComponentTypes.CUSTOM_DATA) && crossbow.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).getNbt().contains("CanHit") ? crossbow.get(DataComponentTypes.CUSTOM_DATA).getNbt().getList("CanHit", NbtElement.STRING_TYPE) : null;
		NbtList canHitList2 = projectile.contains(DataComponentTypes.CUSTOM_DATA) && projectile.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).getNbt().contains("CanHit") ? projectile.get(DataComponentTypes.CUSTOM_DATA).getNbt().getList("CanHit", NbtElement.STRING_TYPE) : null;
		if (proj instanceof SetCanHitList) {
			((SetCanHitList)proj).fabrication$setCanHitLists(canHitList, canHitList2);
			if (canHitList2 != null && proj instanceof PersistentProjectileEntity && ((PersistentProjectileEntity)proj).pickupType == PickupPermission.ALLOWED) {
				((PersistentProjectileEntity)proj).pickupType = PickupPermission.CREATIVE_ONLY;
			}
		}
	}


}
