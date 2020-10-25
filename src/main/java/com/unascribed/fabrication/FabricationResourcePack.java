package com.unascribed.fabrication;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;

public class FabricationResourcePack implements ResourcePack {

	private final String path;
	
	private final JsonObject meta;
	
	public FabricationResourcePack(String path) {
		this.path = path;
		try {
			meta = new Gson().fromJson(Resources.toString(url("pack.mcmeta"), Charsets.UTF_8), JsonObject.class);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public InputStream openRoot(String fileName) throws IOException {
		if (fileName.contains("/") || fileName.contains("\\")) {
			throw new IllegalArgumentException("Root resources can only be filenames, not paths (no / allowed!)");
		}
		return getClass().getClassLoader().getResourceAsStream(path+"/"+fileName);
	}

	private URL url(ResourceType type, Identifier id) {
		return url(type.getDirectory()+"/"+id.getNamespace()+"/"+id.getPath());
	}
	
	private URL url(String path) {
		return getClass().getClassLoader().getResource("packs/"+this.path+"/"+path);
	}
	
	@Override
	public InputStream open(ResourceType type, Identifier id) throws IOException {
		return url(type, id).openStream();
	}

	@Override
	public Collection<Identifier> findResources(ResourceType type, String namespace, String prefix, int maxDepth, Predicate<String> pathFilter) {
		// SORRY NO BONUS
		return Collections.emptySet();
	}

	@Override
	public boolean contains(ResourceType type, Identifier id) {
		return url(type, id) != null;
	}

	@Override
	public Set<String> getNamespaces(ResourceType type) {
		return ImmutableSet.copyOf(Iterables.transform(meta.getAsJsonObject("fabrication").getAsJsonArray("namespaces"), JsonElement::getAsString));
	}

	@Override
	public <T> T parseMetadata(ResourceMetadataReader<T> metaReader) throws IOException {
		if (!meta.has(metaReader.getKey())) return null;
		return metaReader.fromJson(meta.getAsJsonObject(metaReader.getKey()));
	}

	@Override
	public String getName() {
		return "Fabrication Internal Resource Pack: "+path;
	}

	@Override
	public void close() {
		
	}

}
