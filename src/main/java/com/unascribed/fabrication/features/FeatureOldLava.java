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
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EligibleIf(anyConfigAvailable={"*.old_lava", "*.old_lava_scaling"}, envMatches=Env.CLIENT)
public class FeatureOldLava implements Feature {

	public static void onLoaded(SpriteAtlasTexture atlas, SpriteLoader.StitchResult data) {
		try {
			if (FabConf.isAnyEnabled("*.old_lava") && atlas.getId().toString().equals("minecraft:textures/atlas/blocks.png")) {
				Identifier still = new Identifier("block/lava_still");
				Identifier flow = new Identifier("block/lava_flow");
				Sprite originalLava = atlas.getSprite(still);
				Sprite originalLavaFlow = atlas.getSprite(flow);
				int lavaWidth, lavaHeight, lavaFlowWidth,  lavaFlowHeight;
				if (FabConf.isEnabled("*.old_lava_scaling")) {
					lavaWidth = originalLava.getContents().getWidth();
					lavaHeight = originalLava.getContents().getHeight();
					lavaFlowWidth = originalLavaFlow.getContents().getWidth();
					lavaFlowHeight = originalLavaFlow.getContents().getHeight();
				} else {
					lavaWidth = lavaHeight = 16;
					lavaFlowWidth = lavaFlowHeight = 32;
				}
				SpriteLava newLava;
				SpriteLavaFlow newLavaFlow;
				{
					NativeImage image = new NativeImage(lavaWidth, lavaHeight, false);
					newLava = new SpriteLava(atlas.getId(), new SpriteContents(still, new SpriteDimensions(lavaWidth, lavaHeight), image, ResourceMetadata.NONE),
							data.width(), data.height(), FabRefl.Client.getX(originalLava), FabRefl.Client.getY(originalLava), image);
					image = new NativeImage(lavaFlowWidth, lavaFlowHeight, false);
					newLavaFlow = new SpriteLavaFlow(atlas.getId(), new SpriteContents(flow, new SpriteDimensions(lavaFlowWidth, lavaFlowHeight), image, ResourceMetadata.NONE),
							data.width(), data.height(), FabRefl.Client.getX(originalLavaFlow), FabRefl.Client.getY(originalLavaFlow), image);
				}
				Map<Identifier, Sprite> map = new HashMap<>(FabRefl.Client.getSprites(atlas));
				map.put(still, newLava);
				map.put(flow, newLavaFlow);
				FabRefl.Client.setSprites(atlas, map);
				List<Sprite.TickableAnimation> animatedSprites = new ArrayList<>(FabRefl.Client.getAnimatedSprites(atlas));
				animatedSprites.add(newLava);
				animatedSprites.add(newLavaFlow);
				FabRefl.Client.setAnimatedSprites(atlas, animatedSprites);
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
			FabricationMod.featureError(FeatureOldLava.class, "*.old_lava", t);
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
					mip.setColor(x, y, FabRefl.Client.MipmapHelper_blend(
							src.getColor(x * 2 + 0, y * 2 + 0),
							src.getColor(x * 2 + 1, y * 2 + 0),
							src.getColor(x * 2 + 0, y * 2 + 1),
							src.getColor(x * 2 + 1, y * 2 + 1), false));
				}
			}
		}
	}

}

