package com.unascribed.fabrication.mixin.g_weird_tweaks.curable_piglins;

import com.unascribed.fabrication.interfaces.GetPreZombified;
import com.unascribed.fabrication.interfaces.SetPreZombified;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ZombifiedPiglinEntity.class)
@EligibleIf(configAvailable="*.curable_piglins")
public class MixinZombifiedPiglinEntity implements GetPreZombified, SetPreZombified {

	public EntityType<? extends MobEntity> fabrication$preZombifiedEntityType;

	@Override
	public EntityType<? extends MobEntity> fabrication$getPreZombifiedType() {
		if (fabrication$preZombifiedEntityType != null) return  fabrication$preZombifiedEntityType;
		return EntityType.PIGLIN;
	}

	@Override
	public void fabrication$setPreZombifiedType(EntityType<? extends MobEntity> type) {
		fabrication$preZombifiedEntityType = type;
	}
}
