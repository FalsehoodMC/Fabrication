package com.unascribed.fabrication.features;

import com.unascribed.fabrication.client.SpriteLava;
import com.unascribed.fabrication.client.SpriteLavaFlow;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.Feature;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.MipmapHelper;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.Sprite.Info;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

@EligibleIf(configEnabled="*.old_lava", envMatches=Env.CLIENT)
public class FeatureOldLava implements Feature {

	private static Sprite originalLava;
	private static Sprite originalLavaFlow;
	
	private static boolean applied = true;
	
	public static void onLoaded(SpriteAtlasTexture atlas, SpriteAtlasTexture.Data data) {
		if (applied && atlas.getId().toString().equals("minecraft:textures/atlas/blocks.png")) {
			Identifier still = new Identifier("block/lava_still");
			Identifier flow = new Identifier("block/lava_flow");
			originalLava = atlas.getSprite(still);
			originalLavaFlow = atlas.getSprite(flow);
			SpriteLava newLava = new SpriteLava(atlas, new Info(still, 16, 16, AnimationResourceMetadata.EMPTY), data.maxLevel,
					data.width, data.height, originalLava.x, originalLava.y);
			SpriteLavaFlow newLavaFlow = new SpriteLavaFlow(atlas, new Info(flow, 32, 32, AnimationResourceMetadata.EMPTY), data.maxLevel,
					data.width, data.height, originalLavaFlow.x, originalLavaFlow.y);
			atlas.sprites.put(still, newLava);
			atlas.sprites.put(flow, newLavaFlow);
			int origIdx = atlas.animatedSprites.indexOf(originalLava);
			int origFlowIdx = atlas.animatedSprites.indexOf(originalLavaFlow);
			if (origIdx != -1) {
				atlas.animatedSprites.set(origIdx, newLava);
			} else {
				atlas.animatedSprites.add(newLava);
			}
			if (origFlowIdx != -1) {
				atlas.animatedSprites.set(origFlowIdx, newLavaFlow);
			} else {
				atlas.animatedSprites.add(newLavaFlow);
			}
			newLava.tickAnimation();
			newLava.upload();
			newLavaFlow.tickAnimation();
			newLavaFlow.upload();
		}
	}
	
	@Override
	public void apply() {
		applied = true;
		if (originalLava == null) {
			if (MinecraftClient.getInstance().getResourceManager() != null) {
				MinecraftClient.getInstance().reloadResources();
			}
		}
	}

	@Override
	public boolean undo() {
		applied = false;
		if (originalLava != null) {
			if (MinecraftClient.getInstance().getResourceManager() != null) {
				MinecraftClient.getInstance().reloadResources();
			}
			originalLava = null;
			originalLavaFlow = null;
		}
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
					mip.setPixelColor(x, y, MipmapHelper.blend(
							src.getPixelColor(x * 2 + 0, y * 2 + 0),
							src.getPixelColor(x * 2 + 1, y * 2 + 0),
							src.getPixelColor(x * 2 + 0, y * 2 + 1),
							src.getPixelColor(x * 2 + 1, y * 2 + 1), false));
				}
			}
		}
	}

}
