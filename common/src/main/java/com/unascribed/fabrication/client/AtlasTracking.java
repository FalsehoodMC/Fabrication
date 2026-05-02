package com.unascribed.fabrication.client;

import net.minecraft.client.texture.SpriteAtlasTexture;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class AtlasTracking {

	public static final Set<SpriteAtlasTexture> allAtlases = Collections.newSetFromMap(new WeakHashMap<>());

}
