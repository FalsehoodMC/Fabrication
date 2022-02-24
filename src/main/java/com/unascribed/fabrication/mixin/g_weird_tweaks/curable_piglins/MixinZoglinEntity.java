package com.unascribed.fabrication.mixin.g_weird_tweaks.curable_piglins;

import com.unascribed.fabrication.interfaces.GetPreZombified;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZoglinEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ZoglinEntity.class)
@EligibleIf(configAvailable="*.curable_piglins")
public class MixinZoglinEntity implements GetPreZombified {

	@Override
	public EntityType<? extends MobEntity> fabrication$getPreZombifiedType() {
		return EntityType.HOGLIN;
	}

}
