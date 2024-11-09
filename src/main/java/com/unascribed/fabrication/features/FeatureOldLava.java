package com.unascribed.fabrication.features;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.FabRefl;
import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.client.SpriteLava;
import com.unascribed.fabrication.client.SpriteLavaFlow;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.Feature;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.Sprite.Info;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

@EligibleIf(anyConfigAvailable={"*.old_lava", "*.old_lava_scaling"}, envMatches=Env.CLIENT)
public class FeatureOldLava implements Feature {

	public static void onLoaded(SpriteAtlasTexture atlas, SpriteAtlasTexture.Data data) {
		try {
			if (FabConf.isAnyEnabled("*.old_lava") && atlas.getId().toString().equals("minecraft:textures/atlas/blocks.png")) {
				Identifier still = new Identifier("block/lava_still");
				Identifier flow = new Identifier("block/lava_flow");
				Sprite originalLava = atlas.getSprite(still);
				Sprite originalLavaFlow = atlas.getSprite(flow);
				int lavaWidth, lavaHeight, lavaFlowWidth,  lavaFlowHeight;
				if (FabConf.isEnabled("*.old_lava_scaling")) {
					lavaWidth = originalLava.getWidth();
					lavaHeight = originalLava.getHeight();
					lavaFlowWidth = originalLavaFlow.getWidth();
					lavaFlowHeight = originalLavaFlow.getHeight();
				} else {
					lavaWidth = lavaHeight = 16;
					lavaFlowWidth = lavaFlowHeight = 32;
				}
				SpriteLava newLava = new SpriteLava(atlas, new Info(still, lavaWidth, lavaHeight, AnimationResourceMetadata.EMPTY), FabRefl.Client.getMaxLevel(data),
						FabRefl.Client.getWidth(data), FabRefl.Client.getHeight(data), FabRefl.Client.getX(originalLava), FabRefl.Client.getY(originalLava), new NativeImage(lavaWidth, lavaHeight, false));
				SpriteLavaFlow newLavaFlow = new SpriteLavaFlow(atlas, new Info(flow, lavaFlowWidth, lavaFlowHeight, AnimationResourceMetadata.EMPTY), FabRefl.Client.getMaxLevel(data),
						FabRefl.Client.getWidth(data), FabRefl.Client.getHeight(data), FabRefl.Client.getX(originalLavaFlow), FabRefl.Client.getY(originalLavaFlow), new NativeImage(lavaFlowWidth, lavaFlowHeight, false));
				FabRefl.Client.getSprites(atlas).put(still, newLava);
				FabRefl.Client.getSprites(atlas).put(flow, newLavaFlow);
				int origIdx = FabRefl.Client.getAnimatedSprites(atlas).indexOf(originalLava);
				int origFlowIdx = FabRefl.Client.getAnimatedSprites(atlas).indexOf(originalLavaFlow);
				if (origIdx != -1) {
					FabRefl.Client.getAnimatedSprites(atlas).set(origIdx, newLava);
				} else {
					FabRefl.Client.getAnimatedSprites(atlas).add(newLava);
				}
				if (origFlowIdx != -1) {
					FabRefl.Client.getAnimatedSprites(atlas).set(origFlowIdx, newLavaFlow);
				} else {
					FabRefl.Client.getAnimatedSprites(atlas).add(newLavaFlow);
				}
				// simulate the automata for 100 ticks to prevent a "fade-in" effect
				for (int i = 0; i < 100; i++) {
					newLava.tickAnimation();
					newLavaFlow.tickAnimation();
				}
				// now tick it for real so it mips and uploads
				newLavaFlow.tick();
				newLava.tick();
			}
		} catch (Throwable t) {
			FabricationMod.featureError(FeatureOldLava.class, "*.old_lava", t, "Unknown");
		}
	}

	@Override
	public void apply() {
		if (MinecraftClient.getInstance().getResourceManager() != null) {
			MinecraftClient.getInstance().reloadResources();
		}
	}

	@Override
	public boolean undo() {
		apply();
		return true;
	}

	@Override
	public String getConfigKey() {
		return "*.old_lava";
	}

	public static void mip(NativeImage[] images) {
		if (images.length <= 1) return;
		for (int i = 1; i <= images.length-1; ++i) {
			NativeImage src = images[i - 1];
			NativeImage mip = images[i];
			int w = mip.getWidth();
			int h = mip.getHeight();
			for (int x = 0; x < w; ++x) {
				for (int y = 0; y < h; ++y) {
					mip.setPixelColor(x, y, FabRefl.Client.MipmapHelper_blend(
							src.getPixelColor(x * 2 + 0, y * 2 + 0),
							src.getPixelColor(x * 2 + 1, y * 2 + 0),
							src.getPixelColor(x * 2 + 0, y * 2 + 1),
							src.getPixelColor(x * 2 + 1, y * 2 + 1), false));
				}
			}
		}
	}

}
