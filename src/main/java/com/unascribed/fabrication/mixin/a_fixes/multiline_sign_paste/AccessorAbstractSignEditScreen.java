package com.unascribed.fabrication.mixin.a_fixes.multiline_sign_paste;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractSignEditScreen.class)
@EligibleIf(envMatches=Env.CLIENT, configAvailable="*.multiline_sign_paste")
public interface AccessorAbstractSignEditScreen {
	@Accessor("currentRow")
	int getCurrentRow();
	@Accessor("currentRow")

	void setCurrentRow(int i);
}
