package com.unascribed.fabrication.util;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;

public class GrayscaleResourcePack implements ResourcePack {
	@Override
	public InputSupplier<InputStream> openRoot(String... seg) {
		if (seg.length == 0) return null;
		if ("pack.png".equals(seg[0])) {
			return new InputSupplier<InputStream>() {
				@Override
				public InputStream get() throws IOException {
					return getClass().getClassLoader().getResourceAsStream("assets/fabrication/icon.png");
				}
			};
		}
		return new InputSupplier<InputStream>() {
			@Override
			public InputStream get() throws IOException {
				return getIS(String.join("/", seg));
			}
		};
	}

	public static InputStream getIS(String name) throws IOException {
		Optional<Resource> optional = MinecraftClient.getInstance().getResourceManager().getResource(new Identifier(name));
		if (optional.isEmpty()) return null;
		return new Grayscale(optional.get().getInputStream());
	}


	@Override
	public InputSupplier<InputStream> open(ResourceType type, Identifier id) {
		if (type!= ResourceType.CLIENT_RESOURCES) return null;
		if (!id.getPath().endsWith(".png")) return null;
		return new InputSupplier<InputStream>() {
			@Override
			public InputStream get() throws IOException {
				return getIS(id.getPath());
			}
		};
	}

	@Override
	public void findResources(ResourceType type, String namespace, String prefix, ResultConsumer consumer) {

	}

	@Override
	public Set<String> getNamespaces(ResourceType type) {
		return ImmutableSet.of("fabrication_grayscale");
	}

	@Override
	public <T> T parseMetadata(ResourceMetadataReader<T> metaReader) throws IOException {
		return null;
	}

	@Override
	public String getName() {
		return "Fabrication Grayscale";
	}

	@Override
	public void close() {

	}
}
