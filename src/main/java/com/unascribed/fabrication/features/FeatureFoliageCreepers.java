package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.feature.ResourcePackFeature;
import com.unascribed.fabrication.util.GrayscaleResourcePack;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class FeatureFoliageCreepers extends ResourcePackFeature {
	public FeatureFoliageCreepers() {
		super("foliage_creepers");
	}
	@Override
	public void register(Consumer<ResourcePackProfile> consumer) {
		if (active) {
			consumer.accept(ResourcePackProfile.create(MixinConfigPlugin.MOD_NAME+" grayscale", Text.literal("Internal "+ MixinConfigPlugin.MOD_NAME+" grayscale resources"),true,
					s -> new GrayscaleResourcePack(), ResourceType.CLIENT_RESOURCES, ResourcePackProfile.InsertionPosition.TOP, ResourcePackSource.BUILTIN));
		}
	}
}
