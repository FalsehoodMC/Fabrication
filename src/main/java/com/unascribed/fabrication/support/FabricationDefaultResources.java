package com.unascribed.fabrication.support;

import com.google.common.collect.Sets;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.FabRefl;
import com.unascribed.fabrication.FabricationResourcePack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProfile.Factory;
import net.minecraft.resource.ResourcePackProfile.InsertionPosition;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.text.Text;

import java.util.Set;
import java.util.function.Consumer;

public class FabricationDefaultResources implements ResourcePackProvider {

	public static void apply() {
		Set<ResourcePackProvider> providers = FabRefl.getProviders(MinecraftClient.getInstance().getResourcePackManager());
		try {
			providers.add(new FabricationDefaultResources());
		} catch (UnsupportedOperationException e) {
			FabLog.info("Injecting mutable resource pack provider set, as no-one else has yet.");
			providers = Sets.newHashSet(providers);
			FabRefl.setProviders(MinecraftClient.getInstance().getResourcePackManager(), providers);
		}
	}
	@Override
	public void register(Consumer<ResourcePackProfile> consumer, Factory factory) {
		consumer.accept(factory.create(MixinConfigPlugin.MOD_NAME, Text.literal("Internal " + MixinConfigPlugin.MOD_NAME + " resources"),true,
				() -> new FabricationResourcePack("default"),
				new PackResourceMetadata(Text.literal("Internal "+MixinConfigPlugin.MOD_NAME+" resources"), 7),
				InsertionPosition.TOP, ResourcePackSource.PACK_SOURCE_BUILTIN));
	}

}
