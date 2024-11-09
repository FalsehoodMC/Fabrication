package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.feature.ResourcePackFeature;
import com.unascribed.fabrication.util.GrayscaleResourcePack;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackCompatibility;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourcePackPosition;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.function.Consumer;

@EligibleIf(configAvailable="*.foliage_creepers", envMatches=Env.CLIENT)
public class FeatureFoliageCreepers extends ResourcePackFeature {
	public FeatureFoliageCreepers() {
		super("foliage_creepers");
	}
	@Override
	public void register(Consumer<ResourcePackProfile> consumer) {
		consumer.accept(new ResourcePackProfile(
			new ResourcePackInfo(
				MixinConfigPlugin.MOD_NAME + " grayscale",
				Text.literal("Internal " + MixinConfigPlugin.MOD_NAME + " grayscale resources"),
				ResourcePackSource.BUILTIN,
				Optional.empty()
			),
			new ResourcePackProfile.PackFactory() {
				@Override
				public ResourcePack open(ResourcePackInfo info) {
					return new GrayscaleResourcePack();
				}

				@Override
				public ResourcePack openWithOverlays(ResourcePackInfo info, ResourcePackProfile.Metadata metadata) {
					return open(info);
				}
			},
			new ResourcePackProfile.Metadata(Text.of(MixinConfigPlugin.MOD_NAME+" grayscale internal pack"), ResourcePackCompatibility.COMPATIBLE, FeatureSet.empty(), null),
			new ResourcePackPosition(true, ResourcePackProfile.InsertionPosition.TOP, false)
		));
	}
	@Override
	public void apply() {
	}
	@Override
	public boolean undo() {
		return true;
	}
}
