package com.unascribed.fabrication.features;

import java.util.Locale;

import com.mojang.blaze3d.systems.RenderSystem;
import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.client.FabricationConfigScreen;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.Feature;

import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

@EligibleIf(configAvailable="*.toggle_stance", envMatches=Env.CLIENT)
public class FeatureToggleStance implements Feature {

	public enum Stance {
		STANDING(false, false),
		SNEAKING(true, false),
		CRAWLING(false, true),
		;
		public final boolean sneaking, crawling;
		private Stance(boolean sneaking, boolean crawling) {
			this.sneaking = sneaking;
			this.crawling = crawling;
		}
		public Stance next() {
			switch (this) {
				case STANDING: return SNEAKING;
				case SNEAKING: return FabConf.isEnabled("*.crawling") ? CRAWLING : STANDING;
				case CRAWLING: return STANDING;
				default: throw new AssertionError("missing case for "+this);
			}
		}
	}

	public static KeyBinding keybind;
	public static Stance currentStance = Stance.STANDING;
	public static int toggleTime = 1000;
	public static int lastAge = 0;

	@Override
	public void apply() {
		keybind = new KeyBinding("["+ MixinConfigPlugin.MOD_NAME+"] Toggle Stance", InputUtil.UNKNOWN_KEY.getCode(), "key.categories.movement") {
			@Override
			public void setPressed(boolean pressed) {
				if (!pressed && MinecraftClient.getInstance().getNetworkHandler() == null) {
					// reset() was probably called, so, reset
					currentStance = Stance.STANDING;
				}
				if (!isPressed() && pressed) {
					Stance prev = currentStance;
					currentStance = currentStance.next();
					if (prev.crawling != currentStance.crawling) {
						FeatureCrawling.setCrawling(currentStance.crawling, currentStance.crawling);
					}
					toggleTime = 0;
				}
				super.setPressed(pressed);
			}
		};
		Agnos.registerKeyBinding(keybind);
		Agnos.runForHudRender((dc, d) -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			PlayerEntity p = mc.player;
			if (p == null) return;
			if (toggleTime < 40) {
				Window w = mc.getWindow();
				float a = FabricationConfigScreen.sCurve5(1-(toggleTime/40f));
				Identifier tex = new Identifier("fabrication", "textures/stance/"+currentStance.name().toLowerCase(Locale.ROOT)+".png");
				RenderSystem.defaultBlendFunc();
				//GlStateManager.disableAlphaTest();
				RenderSystem.setShaderTexture(0, tex);
				RenderSystem.setShaderColor(1, 1, 1, a);
				dc.drawTexture(tex, (w.getScaledWidth()/2)-48, (w.getScaledHeight()-32)/2, 0, 0, 0, 32, 32, 32, 32);
				RenderSystem.setShaderColor(1, 1, 1, 1);
				//GlStateManager.enableAlphaTest();
			}
			if (p.age != lastAge) {
				lastAge = p.age;
				toggleTime++;
			}
		});
	}

	@Override
	public boolean undo() {
		return false;
	}

	@Override
	public String getConfigKey() {
		return "*.toggle_stance";
	}

}
