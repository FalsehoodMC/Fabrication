package com.unascribed.fabrication.mixin.a_fixes.declared_travel;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.DimInformedScreen;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(DownloadingTerrainScreen.class)
@EligibleIf(configAvailable="*.declared_travel", envMatches=Env.CLIENT)
public class MixinDownloadingTerrainScreen implements DimInformedScreen {
	private Text fabrication$destinationText = null;

	@ModifyArg(method="render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V", at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/screen/DownloadingTerrainScreen;drawCenteredTextWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V"))
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
