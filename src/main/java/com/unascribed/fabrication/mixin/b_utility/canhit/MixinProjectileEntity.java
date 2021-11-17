package com.unascribed.fabrication.mixin.b_utility.canhit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.interfaces.SetCanHitList;
import com.unascribed.fabrication.logic.CanHitUtil;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;

@Mixin(ProjectileEntity.class)
@EligibleIf(configAvailable="*.canhit")
public class MixinProjectileEntity implements SetCanHitList {

	private NbtList fabrication$canHitList;
	private NbtList fabrication$canHitList2;

	@Inject(at=@At("HEAD"), method="onCollision(Lnet/minecraft/util/hit/HitResult;)V", cancellable=true)
	public void onCollision(HitResult result, CallbackInfo ci) {
		if (!MixinConfigPlugin.isEnabled("*.canhit")) return;
		if (result.getType() == Type.ENTITY) {
			Entity e = ((EntityHitResult)result).getEntity();
			if (!CanHitUtil.canHit(fabrication$getCanHitList(), e) || !CanHitUtil.canHit(fabrication$getCanHitList2(), e)) {
				ci.cancel();
			}
		}
	}

	@Inject(at=@At("TAIL"), method="writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void writeCustomDataToTag(NbtCompound tag, CallbackInfo ci) {
		if (fabrication$canHitList != null) {
			tag.put("fabrication:CanHitList", fabrication$canHitList);
		}
		if (fabrication$canHitList2 != null) {
			tag.put("fabrication:CanHitList2", fabrication$canHitList2);
		}
	}

	@Inject(at=@At("TAIL"), method="readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void readCustomDataFromTag(NbtCompound tag, CallbackInfo ci) {
		if (tag.contains("fabrication:CanHitList")) {
			fabrication$canHitList = tag.getList("fabrication:CanHitList", NbtType.STRING);
		} else {
			fabrication$canHitList = null;
		}
		if (tag.contains("fabrication:CanHitList2")) {
			fabrication$canHitList2 = tag.getList("fabrication:CanHitList2", NbtType.STRING);
		} else {
			fabrication$canHitList2 = null;
		}
	}

	@Override
	public void fabrication$setCanHitLists(NbtList list, NbtList list2) {
		fabrication$canHitList = list;
		fabrication$canHitList2 = list2;
	}

	@Override
	public NbtList fabrication$getCanHitList() {
		return fabrication$canHitList;
	}

	@Override
	public NbtList fabrication$getCanHitList2() {
		return fabrication$canHitList2;
	}

}
