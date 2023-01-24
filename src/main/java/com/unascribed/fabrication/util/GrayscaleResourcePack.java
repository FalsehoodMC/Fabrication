package com.unascribed.fabrication.util;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

public class GrayscaleResourcePack implements ResourcePack {
	@Override
	public InputStream openRoot(String fileName) throws IOException {
		if (fileName.contains("/") || fileName.contains("\\")) {
			throw new IllegalArgumentException("Root resources can only be filenames, not paths (no / allowed!)");
		}
		if ("pack.png".equals(fileName)) {
			return getClass().getClassLoader().getResourceAsStream("assets/fabrication/icon.png");
		}
		return getIS(fileName);
	}

	public static InputStream getIS(String name) throws IOException {
		return new Grayscale(MinecraftClient.getInstance().getResourceManager().getResource(new Identifier(name)).getInputStream());
	}

	@Override
	public InputStream open(ResourceType type, Identifier id) throws IOException {
		if (type!= ResourceType.CLIENT_RESOURCES) throw new FileNotFoundException(id.toString());
		return getIS(id.getPath());
	}

	@Override
	public Collection<Identifier> findResources(ResourceType type, String namespace, String prefix, int maxDepth, Predicate<String> pathFilter) {
		return Collections.emptySet();
	}

	@Override
	public boolean contains(ResourceType type, Identifier id) {
		if (!"fabrication_grayscale".equals(id.getNamespace())) return false;
		try {
			InputStream is = getIS(id.getPath());
			if (is instanceof Grayscale && ((Grayscale) is).err != null) return false;
		} catch (IOException e) {
			return false;
		}
		return true;
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
