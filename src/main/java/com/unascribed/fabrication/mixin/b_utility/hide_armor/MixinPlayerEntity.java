package com.unascribed.fabrication.mixin.b_utility.hide_armor;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.interfaces.GetSuppressedSlots;
import com.unascribed.fabrication.support.EligibleIf;
import com.google.common.base.Enums;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

@Mixin(PlayerEntity.class)
@EligibleIf(configEnabled="*.hide_armor")
public abstract class MixinPlayerEntity implements GetSuppressedSlots {

	private final EnumSet<EquipmentSlot> fabrication$suppressedSlots = EnumSet.noneOf(EquipmentSlot.class);
	
	@Override
	public Set<EquipmentSlot> fabrication$getSuppressedSlots() {
		return fabrication$suppressedSlots;
	}
	
	@Inject(at=@At("TAIL"), method="writeCustomDataToTag(Lnet/minecraft/nbt/CompoundTag;)V", expect=1)
	public void writeCustomDataToTag(CompoundTag tag, CallbackInfo ci) {
		ListTag li = new ListTag();
		for (EquipmentSlot pt : fabrication$suppressedSlots) {
			li.add(StringTag.of(pt.name().toLowerCase(Locale.ROOT)));
		}
		if (!li.isEmpty()) {
			tag.put("fabrication:SuppressedSlots", li);
		}
	}
	
	@Inject(at=@At("TAIL"), method="readCustomDataFromTag(Lnet/minecraft/nbt/CompoundTag;)V", expect=1)
	public void readCustomDataFromTag(CompoundTag tag, CallbackInfo ci) {
		fabrication$suppressedSlots.clear();
		ListTag li = tag.getList("fabrication:SuppressedSlots", NbtType.STRING);
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
