package com.unascribed.fabrication.mixin.i_woina.no_experience;

import java.util.List;
import java.util.stream.Collectors;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabModifyConst;
import com.unascribed.fabrication.support.injection.FabModifyVariable;
import com.unascribed.fabrication.support.injection.Hijack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import com.unascribed.fabrication.support.injection.FabModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;


@Mixin(EnchantmentScreen.class)
@EligibleIf(configAvailable="*.no_experience", envMatches=Env.CLIENT)
public abstract class MixinEnchantmentScreen extends HandledScreen<EnchantmentScreenHandler> {

	public MixinEnchantmentScreen(EnchantmentScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
	}

	@Redirect(at=@At(value="FIELD", target="net/minecraft/client/network/ClientPlayerEntity.experienceLevel:I"),
			method={
					"drawBackground(Lnet/minecraft/client/util/math/MatrixStack;FII)V",
					"render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V"
	})
	public int amendExperienceLevel(ClientPlayerEntity subject) {
		if (FabConf.isEnabled("*.no_experience")) return 65535;
		return subject.experienceLevel;
	}

	@FabModifyArg(method="render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V", index=1,
			at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/screen/ingame/EnchantmentScreen;renderTooltip(Lnet/minecraft/client/util/math/MatrixStack;Ljava/util/List;II)V"))
	public List<Text> removeLevelText(List<Text> original){
		if (FabConf.isEnabled("*.no_experience")){
			original = original.stream().filter(text ->{
				if (text instanceof TranslatableText) {
					return !((TranslatableText) text).getKey().startsWith("container.enchant.level");
				}
				return true;
			}).collect(Collectors.toList());
		}
		return original;
	}

	@Hijack(target="net/minecraft/client/gui/screen/ingame/EnchantmentScreen.drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V",
			method="drawBackground(Lnet/minecraft/client/util/math/MatrixStack;FII)V")
	public boolean fabrication$noXpHijackDrawTexture(EnchantmentScreen subject, MatrixStack matrices, int x, int y, int u, int v) {
		if (FabConf.isEnabled("*.no_experience") && (v == 223 || v == 239)) {
			if (v == 223) {
				textRenderer.drawWithShadow(matrices, ""+((u/16)+1), x+98, y+8, 0x5577FF);
			}
			return true;
		}
		return false;
	}

	@FabModifyVariable(at=@At(value="INVOKE", target="net/minecraft/client/font/TextRenderer.getWidth(Ljava/lang/String;)I", ordinal=0),
			method="drawBackground(Lnet/minecraft/client/util/math/MatrixStack;FII)V", ordinal=0)
	public String modifyLevelText(String orig) {
		if (FabConf.isEnabled("*.no_experience")) return "";
		return orig;
	}

	@FabModifyConst(constant=@Constant(intValue=20, ordinal=0),
			method="drawBackground(Lnet/minecraft/client/util/math/MatrixStack;FII)V", require=0)
	public int modifyPhraseOffset(int orig) {
		if (FabConf.isEnabled("*.no_experience")) return 3;
		return orig;
	}

}
