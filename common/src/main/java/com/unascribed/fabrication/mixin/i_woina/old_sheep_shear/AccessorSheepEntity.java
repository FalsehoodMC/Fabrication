package com.unascribed.fabrication.mixin.i_woina.old_sheep_shear;

import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(SheepEntity.class)
@EligibleIf(configAvailable="*.old_sheep_shear")
public interface AccessorSheepEntity {

	@Accessor("DROPS")
	static Map<DyeColor, ItemConvertible> fabrication$getDrops() {
		return null;
	}

}
