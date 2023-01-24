package com.unascribed.fabrication.mixin.b_utility.hide_armor;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.forgery_nonsense.ForgeryNbt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.interfaces.GetSuppressedSlots;
import com.unascribed.fabrication.support.EligibleIf;

import com.google.common.base.Enums;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

@Mixin(PlayerEntity.class)
@EligibleIf(configAvailable="*.hide_armor")
public abstract class MixinPlayerEntity implements GetSuppressedSlots {

	private final EnumSet<EquipmentSlot> fabrication$suppressedSlots = EnumSet.noneOf(EquipmentSlot.class);

	@Override
	public Set<EquipmentSlot> fabrication$getSuppressedSlots() {
		return fabrication$suppressedSlots;
	}

	@FabInject(at=@At("TAIL"), method="writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void writeCustomDataToTag(NbtCompound tag, CallbackInfo ci) {
		NbtList li = ForgeryNbt.getList();
		for (EquipmentSlot pt : fabrication$suppressedSlots) {
			li.add(NbtString.of(pt.name().toLowerCase(Locale.ROOT)));
		}
		if (!li.isEmpty()) {
			tag.put("fabrication:SuppressedSlots", li);
		}
	}

	@FabInject(at=@At("TAIL"), method="readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void readCustomDataFromTag(NbtCompound tag, CallbackInfo ci) {
		fabrication$suppressedSlots.clear();
		NbtList li = tag.getList("fabrication:SuppressedSlots", NbtType.STRING);
		for (int i = 0; i < li.size(); i++) {
			EquipmentSlot pt = Enums.getIfPresent(EquipmentSlot.class, li.getString(i).toUpperCase(Locale.ROOT)).orNull();
			if (pt == null) {
				FabLog.warn("Unrecognized slot "+li.getString(i)+" while loading player");
			} else {
				fabrication$suppressedSlots.add(pt);
			}
		}
	}

}
