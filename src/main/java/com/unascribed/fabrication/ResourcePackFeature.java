package com.unascribed.fabrication;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;

import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.Feature;

import com.google.common.collect.Sets;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourcePackProfile.Factory;
import net.minecraft.resource.ResourcePackProfile.InsertionPosition;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.text.LiteralText;

public abstract class ResourcePackFeature implements Feature, ResourcePackProvider {

	private final String configKey;
	private final String path;
	
	private boolean active = false;
	
	@Environment(EnvType.CLIENT)
	private ResourcePack pack;
	
	public ResourcePackFeature(String path) {
		this.configKey = "*."+path;
		this.path = path;
		if (Agnos.INST.getCurrentEnv() == Env.CLIENT) {
			initClient();
		}
	}
	
	@Environment(EnvType.CLIENT)
	private void initClient() {
		Set<ResourcePackProvider> providers = MinecraftClient.getInstance().getResourcePackManager().providers;
		try {
			providers.add(this);
		} catch (UnsupportedOperationException e) {
			FabLog.info("Injecting mutable resource pack provider set, as no-one else has yet.");
			providers = Sets.newHashSet(providers);
			MinecraftClient.getInstance().getResourcePackManager().providers = providers;
		}
	}

	@Override
	public void register(Consumer<ResourcePackProfile> consumer, Factory factory) {
		if (active) {
			Supplier<ResourcePack> f = () -> new FabricationResourcePack(path);
			consumer.accept(factory.create("Fabrication Internal Resource Pack: "+path, true, f, f.get(),
					new PackResourceMetadata(new LiteralText("Internal Fabrication resources"), 6),
					InsertionPosition.TOP, ResourcePackSource.PACK_SOURCE_BUILTIN));
		}
	}
	
	@Override
	public void apply() {
		active = true;
		if (Agnos.INST.getCurrentEnv() == Env.CLIENT) {
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
		if (Agnos.INST.getCurrentEnv() == Env.CLIENT) {
			reloadClient();
		}
		return true;
	}
	
	@Override
	public String getConfigKey() {
		return configKey;
	}
	
}
