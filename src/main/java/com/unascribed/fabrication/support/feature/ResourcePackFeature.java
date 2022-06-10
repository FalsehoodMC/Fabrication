package com.unascribed.fabrication.support.feature;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.FabRefl;
import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.FabricationResourcePack;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.Feature;

import com.google.common.collect.Sets;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProfile.Factory;
import net.minecraft.resource.ResourcePackProfile.InsertionPosition;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.text.Text;

public abstract class ResourcePackFeature implements Feature, ResourcePackProvider {

	private final String configKey;
	private final String path;

	private boolean active = false;

	@Environment(EnvType.CLIENT)
	private ResourcePack pack;

	public ResourcePackFeature(String path) {
		this.configKey = "*."+path;
		this.path = path;
		if (Agnos.getCurrentEnv() == Env.CLIENT) {
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
			FabRefl.setProviders(MinecraftClient.getInstance().getResourcePackManager(), providers);
		}
	}

	@Override
	public void register(Consumer<ResourcePackProfile> consumer, Factory factory) {
		if (active) {
			Supplier<ResourcePack> f = () -> new FabricationResourcePack(path);
			consumer.accept(factory.create(FabricationMod.MOD_NAME+" "+path, Text.literal("Internal "+FabricationMod.MOD_NAME+" resources"),true, f,
					new PackResourceMetadata(Text.literal("Internal " + FabricationMod.MOD_NAME + " resources"), 7),
					InsertionPosition.TOP, ResourcePackSource.PACK_SOURCE_BUILTIN));
		}
	}

	@Override
	public void apply() {
		active = true;
		if (Agnos.getCurrentEnv() == Env.CLIENT) {
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
		if (Agnos.getCurrentEnv() == Env.CLIENT) {
			reloadClient();
		}
		return true;
	}

	@Override
	public String getConfigKey() {
		return configKey;
	}

}
