package com.unascribed.fabrication.mixin.b_utility.hide_armor;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabModifyArg;

import com.mojang.datafixers.util.Pair;
import com.unascribed.fabrication.features.FeatureHideArmor;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.EntityTrackerEntry;

@Mixin(EntityTrackerEntry.class)
@EligibleIf(configAvailable="*.hide_armor")
public class MixinEntityTrackerEntry {

	@Shadow @Final
	private Entity entity;

	@FabModifyArg(at=@At(value="INVOKE", target="Lnet/minecraft/network/packet/s2c/play/EntityEquipmentUpdateS2CPacket;<init>(ILjava/util/List;)V"),
			method="sendPackets(Lnet/minecraft/server/network/ServerPlayerEntity;Ljava/util/function/Consumer;)V")
	public List<Pair<EquipmentSlot, ItemStack>> constructUpdatePacket(List<Pair<EquipmentSlot, ItemStack>> equipmentList) {
		return FeatureHideArmor.muddle(entity, equipmentList);
	}

}
