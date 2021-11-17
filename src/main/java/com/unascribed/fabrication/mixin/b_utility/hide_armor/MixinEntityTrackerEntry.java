package com.unascribed.fabrication.mixin.b_utility.hide_armor;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.datafixers.util.Pair;
import com.unascribed.fabrication.features.FeatureHideArmor;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;

@Mixin(EntityTrackerEntry.class)
@EligibleIf(configEnabled="*.hide_armor")
public class MixinEntityTrackerEntry {

	@Shadow @Final
	private Entity entity;

	@Redirect(at=@At(value="NEW", target="net/minecraft/network/packet/s2c/play/EntityEquipmentUpdateS2CPacket"),
			method="sendPackets(Ljava/util/function/Consumer;)V")
	public EntityEquipmentUpdateS2CPacket constructUpdatePacket(int id, List<Pair<EquipmentSlot, ItemStack>> equipmentList) {
		return new EntityEquipmentUpdateS2CPacket(id, FeatureHideArmor.muddle(entity, equipmentList));
	}

}
