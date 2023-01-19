package com.unascribed.fabrication;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FabricationResourcePack implements ResourcePack {

	private final String path;

	private final JsonObject meta;

	public FabricationResourcePack(String path) {
		this.path = path;
		JsonObject meta;
		try {
			meta = new Gson().fromJson(Resources.toString(url("pack.mcmeta"), Charsets.UTF_8), JsonObject.class);
		} catch (Throwable t) {
			FabLog.warn("Failed to load meta for internal resource pack "+path);
			meta = new JsonObject();
		}
		this.meta = meta;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public InputSupplier<InputStream> openRoot(String... seg) {
		if (seg.length == 0) return null;
		if ("pack.png".equals(seg[0])) {
			return () -> getClass().getClassLoader().getResourceAsStream("assets/fabrication/icon.png");
		}
		InputStream is = getClass().getClassLoader().getResourceAsStream(path+"/"+String.join("/", seg));
		if (is == null) return null;
		return () -> is;
	}

	private URL url(ResourceType type, Identifier id) {
		return url(type.getDirectory()+"/"+id.getNamespace()+"/"+id.getPath());
	}

	private URL url(String path) {
		return getClass().getClassLoader().getResource("packs/"+this.path+"/"+path);
	}

	@Override
	public InputSupplier<InputStream> open(ResourceType type, Identifier id) {
		URL u = url(type, id);
		if (u == null) return null;
		return u::openStream;
	}

	@Override
	public void findResources(ResourceType type, String namespace, String prefix, ResultConsumer consumer) {
		String prePath = "packs/"+this.path+"/"+type.getDirectory()+"/"+namespace+"/";
		String dirPathStr = prePath+prefix;
		try {
			URL dirUrl = getClass().getClassLoader().getResource(dirPathStr);
			if (dirUrl == null) return;
			if ("jar".equals(dirUrl.getProtocol())) {
				JarFile jar = ((JarURLConnection) dirUrl.openConnection()).getJarFile();
				Enumeration<JarEntry> en = jar.entries();
				while (en.hasMoreElements()) {
					String name = en.nextElement().getName();
					int i = name.indexOf(dirPathStr);
					if (i != -1) {
						Identifier id = new Identifier("fabrication", name.substring(i+prePath.length()));
						consumer.accept(id, open(type, id));
					}
				}
			} else {
				List<File> dirs = new ArrayList<>();
				dirs.add(new File(dirUrl.getFile()));
				for (int i=0; i<dirs.size(); i++) {
					for (File file : dirs.get(i).listFiles()) {
						if (file.isDirectory()) {
							dirs.add(file);
							continue;
						}
						String name = file.getPath();
						int ix = name.indexOf(dirPathStr);
						if (ix != -1) {
							Identifier id = new Identifier("fabrication", name.substring(ix + prePath.length()));
							consumer.accept(id, open(type, id));
						}
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
		return MixinConfigPlugin.MOD_NAME;
	}

	@Override
	public void close() {

	}

}
