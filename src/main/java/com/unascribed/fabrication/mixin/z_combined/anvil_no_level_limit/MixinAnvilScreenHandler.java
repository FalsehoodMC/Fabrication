package com.unascribed.fabrication.mixin.z_combined.anvil_no_level_limit;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabModifyConst;
import net.minecraft.screen.AnvilScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;

@Mixin(AnvilScreenHandler.class)
@EligibleIf(anyConfigAvailable={"*.anvil_no_level_limit", "*.anvil_no_xp_cost"})
public abstract class MixinAnvilScreenHandler {

	@FabModifyConst(method="updateResult()V", constant=@Constant(ordinal=2, intValue=40))
	public int removeLimit(int i) {
		if (!(FabConf.isEnabled("*.anvil_no_level_limit") || FabConf.isEnabled("*.anvil_no_xp_cost"))) return i;
		return Integer.MAX_VALUE;
	}

}
