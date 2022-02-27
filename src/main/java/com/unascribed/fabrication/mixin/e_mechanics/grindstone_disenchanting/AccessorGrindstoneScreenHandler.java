package com.unascribed.fabrication.mixin.e_mechanics.grindstone_disenchanting;

import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GrindstoneScreenHandler.class)
@EligibleIf(configAvailable="*.grindstone_disenchanting", modNotLoaded="fabric:grindenchantments", modLoaded="pollen")
public interface AccessorGrindstoneScreenHandler {
	@Accessor("context")
	ScreenHandlerContext fabrication$getContext();
}
