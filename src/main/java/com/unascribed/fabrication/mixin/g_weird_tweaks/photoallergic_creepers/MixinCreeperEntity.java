package com.unascribed.fabrication.mixin.g_weird_tweaks.photoallergic_creepers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(CreeperEntity.class)
@EligibleIf(configAvailable="*.photoallergic_creepers")
public abstract class MixinCreeperEntity extends HostileEntity {

	protected MixinCreeperEntity(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(at=@At("HEAD"), method="tick()V")
	public void onTick(CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.photoallergic_creepers")) {
			if (isAffectedByDaylight()) {
				ItemStack helmet = this.getEquippedStack(EquipmentSlot.HEAD);
				if (!helmet.isEmpty()) {
					if (helmet.isDamageable()) {
						helmet.setDamage(helmet.getDamage() + random.nextInt(2));
						if (helmet.getDamage() >= helmet.getMaxDamage()) {
							sendEquipmentBreakStatus(EquipmentSlot.HEAD);
							equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
						}
					}
					return;
				}
				setOnFireFor(8);
			}
		}
	}

}
