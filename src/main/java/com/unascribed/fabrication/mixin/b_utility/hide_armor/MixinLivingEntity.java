package com.unascribed.fabrication.mixin.b_utility.hide_armor;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.mojang.datafixers.util.Pair;
import com.unascribed.fabrication.features.FeatureHideArmor;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.hide_armor")
public class MixinLivingEntity {

	@ModifyArg(at=@At(value="INVOKE", target="Lnet/minecraft/network/packet/s2c/play/EntityEquipmentUpdateS2CPacket;<init>(ILjava/util/List;)V"),
			method="setEquipment(Ljava/util/Map;)V")
	public List<Pair<EquipmentSlot, ItemStack>> constructUpdatePacket(List<Pair<EquipmentSlot, ItemStack>> equipmentList) {
		return FeatureHideArmor.muddle((Entity)(Object)this, equipmentList);
	}

}
