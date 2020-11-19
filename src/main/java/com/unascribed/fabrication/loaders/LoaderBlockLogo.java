package com.unascribed.fabrication.loaders;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;

import com.ibm.icu.impl.locale.XCldrStub.Splitter;
import com.unascribed.fabrication.Resolvable;
import com.unascribed.fabrication.support.ConfigLoader;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.MoreFiles;
import com.google.common.io.Resources;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class LoaderBlockLogo implements ConfigLoader {

	public static boolean invalidated = true;
	public static NativeImage image;
	public static BooleanSupplier getReverse = () -> false;
	public static final Map<Integer, Supplier<BlockState>> colorToState = Maps.newHashMap();
	public static boolean sound = false;
	
	@Override
	public void load(Path configDir, Map<String, String> config) {
		colorToState.clear();
		sound = false;
		invalidated = true;
		if (image != null) image.close();
		Path imageFile = configDir.resolve("block_logo.png");
		if (!Files.exists(imageFile)) {
			try {
				Resources.asByteSource(MixinConfigPlugin.class.getClassLoader().getResource("default_block_logo.png")).copyTo(MoreFiles.asByteSink(imageFile));
			} catch (IOException e) {
				throw new RuntimeException("Failed to write default block logo", e);
			}
		}
		try (InputStream is = Files.newInputStream(imageFile)) {
			image = NativeImage.read(is);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load block logo", e);
		}
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				int color = image.getPixelColor(x, y);
				int alpha = color>>24;
				if (alpha > 0 && alpha < 255) {
					throw new IllegalArgumentException("At "+x+", "+y+" in block_logo.png: Found a pixel that is not fully transparent or fully opaque. This is not allowed");
				}
			}
		}
		for (Map.Entry<String, String> en : config.entrySet()) {
			if (en.getKey().startsWith("general.")) {
				if (en.getKey().equals("general.reverse")) {
					switch (en.getValue().toLowerCase(Locale.ROOT)) {
						case "false":
							getReverse = () -> false;
							break;
						case "true":
							getReverse = () -> true;
							break;
						case "random":
							getReverse = () -> ThreadLocalRandom.current().nextBoolean();
							break;
						default:
							throw new IllegalArgumentException(en.getKey()+" must be false, true, or random; got "+en.getValue());
					}
				} else if (en.getKey().equals("general.sound")) {
					sound = Boolean.parseBoolean(en.getValue());
				} else {
					throw new IllegalArgumentException(en.getKey()+" is invalid: Unknown general key");
				}
			} else if (en.getKey().startsWith("pixels.")) {
				String color = en.getKey().substring(7);
				if (color.length() != 6)
					throw new IllegalArgumentException(en.getKey()+" is invalid: Keys within pixels must be 6-digit (24-bit) hex colors");
				int colorInt = Integer.parseInt(color, 16);
				int swapped = colorInt&0x0000FF00;
				swapped |= (colorInt&0x00FF0000) >> 16;
				swapped |= (colorInt&0x000000FF) << 16;
				List<Resolvable<Block>> blocks = Lists.newArrayList();
				for (String s : Splitter.on(' ').split(en.getValue())) {
					blocks.add(Resolvable.of(new Identifier(s), Registry.BLOCK));
				}
				colorToState.put(swapped, () -> {
					Resolvable<Block> res = blocks.get(ThreadLocalRandom.current().nextInt(blocks.size()));
					Optional<Block> opt = res.get();
					if (!opt.isPresent()) {
						LogManager.getLogger("Fabrication").warn("Couldn't find a block with ID {} when rendering block logo", res.getId());
						return Blocks.AIR.getDefaultState();
					}
					return opt.get().getDefaultState();
				});
			} else {
				throw new IllegalArgumentException(en.getKey()+" is invalid: Unknown section");
			}
		}
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				int color = image.getPixelColor(x, y);
				int alpha = color>>24;
				color &= 0x00FFFFFF;
				if (alpha == 255 && !colorToState.containsKey(color)) {
					throw new IllegalArgumentException("At "+x+", "+y+" in block_logo.png: Found a pixel with a color that isn't in the config: "+Integer.toHexString(color|0xFF000000).substring(2).toUpperCase(Locale.ROOT));
				}
			}
		}
	}

	@Override
	public String getConfigName() {
		return "block_logo";
	}

}
