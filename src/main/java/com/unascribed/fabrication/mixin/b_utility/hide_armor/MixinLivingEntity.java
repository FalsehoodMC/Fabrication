package com.unascribed.fabrication.mixin.b_utility.hide_armor;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.datafixers.util.Pair;
import com.unascribed.fabrication.features.FeatureHideArmor;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;

@Mixin(LivingEntity.class)
@EligibleIf(configEnabled="*.hide_armor")
public class MixinLivingEntity {

	@Redirect(at=@At(value="NEW", target="net/minecraft/network/packet/s2c/play/EntityEquipmentUpdateS2CPacket"),
			method="method_30123(Ljava/util/Map;)V")
	public EntityEquipmentUpdateS2CPacket constructUpdatePacket(int id, List<Pair<EquipmentSlot, ItemStack>> equipmentList) {
		return new EntityEquipmentUpdateS2CPacket(id, FeatureHideArmor.muddle((Entity)(Object)this, equipmentList));
	}

}
