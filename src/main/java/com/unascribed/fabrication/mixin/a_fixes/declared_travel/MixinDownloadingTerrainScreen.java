package com.unascribed.fabrication.mixin.a_fixes.declared_travel;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.DimInformedScreen;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabModifyArg;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DownloadingTerrainScreen.class)
@EligibleIf(configAvailable="*.declared_travel", envMatches=Env.CLIENT)
public class MixinDownloadingTerrainScreen implements DimInformedScreen {
	private Text fabrication$destinationText = null;

	@FabModifyArg(method="render(Lnet/minecraft/client/gui/DrawContext;IIF)V", at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/DrawContext;drawCenteredTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V"))
	private Text addDimensionDataToTerrainScreen(Text par1) {
		if (fabrication$destinationText == null) return par1;
		if (!FabConf.isEnabled("*.declared_travel")) return par1;
		return fabrication$destinationText;
	}

	@Override
	public void fabrication$setDimText(String text) {
		fabrication$destinationText = Text.of(text);
	}
}
