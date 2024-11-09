package com.unascribed.fabrication.support.feature;

import com.google.common.collect.Sets;
import com.unascribed.fabrication.EarlyAgnos;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.FabRefl;
import com.unascribed.fabrication.FabricationResourcePack;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.Feature;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourcePackPosition;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProfile.InsertionPosition;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public abstract class ResourcePackFeature implements Feature, ResourcePackProvider {

	private final String configKey;
	private final String path;

	public boolean active = false;

	public ResourcePackFeature(String path) {
		this.configKey = "*."+path;
		this.path = path;
		if (EarlyAgnos.getCurrentEnv() == Env.CLIENT) {
			initClient();
		}
	}

	@Environment(EnvType.CLIENT)
	private void initClient() {
		Set<ResourcePackProvider> providers = FabRefl.getProviders(MinecraftClient.getInstance().getResourcePackManager());
		try {
			providers.add(this);
		} catch (UnsupportedOperationException e) {
			FabLog.info("Injecting mutable resource pack provider set, as no-one else has yet.");
			providers = Sets.newHashSet(providers);
			providers.add(this);
			FabRefl.setProviders(MinecraftClient.getInstance().getResourcePackManager(), providers);
		}
	}
	@Override
	public void register(Consumer<ResourcePackProfile> consumer) {
		if (active) {
			consumer.accept(ResourcePackProfile.create(
				new ResourcePackInfo(
					MixinConfigPlugin.MOD_NAME + " " + path,
					Text.literal("Internal " + MixinConfigPlugin.MOD_NAME + " resources"),
					ResourcePackSource.BUILTIN,
					Optional.empty()
				),
				new ResourcePackProfile.PackFactory() {
					@Override
					public ResourcePack open(ResourcePackInfo info) {
						return new FabricationResourcePack(path);
					}

					@Override
					public ResourcePack openWithOverlays(ResourcePackInfo info, ResourcePackProfile.Metadata metadata) {
						return open(info);
					}
				}, ResourceType.CLIENT_RESOURCES, new ResourcePackPosition(true, InsertionPosition.TOP, false)
			));
		}
	}

	@Override
	public void apply() {
		active = true;
		if (EarlyAgnos.getCurrentEnv() == Env.CLIENT) {
			reloadClient();
		}
	}

	@Environment(EnvType.CLIENT)
	private void reloadClient() {
		if (MinecraftClient.getInstance().getResourceManager() != null) {
			MinecraftClient.getInstance().reloadResources();
		}
	}

	@Override
	public boolean undo() {
		active = false;
		if (EarlyAgnos.getCurrentEnv() == Env.CLIENT) {
			reloadClient();
		}
		return true;
	}

	@Override
	public String getConfigKey() {
		return configKey;
	}

}
