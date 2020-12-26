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

import com.ibm.icu.impl.locale.XCldrStub.Splitter;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.QDIni;
import com.unascribed.fabrication.Resolvable;
import com.unascribed.fabrication.support.ConfigLoader;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
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

@EligibleIf(envMatches=Env.CLIENT)
public class LoaderBlockLogo implements ConfigLoader {

	public static boolean invalidated = true;
	public static boolean unrecoverableLoadError = false;
	public static NativeImage image;
	public static BooleanSupplier getReverse = () -> false;
	public static final Map<Integer, Supplier<BlockState>> colorToState = Maps.newHashMap();
	public static boolean sound = false;
	
	public enum Reverse {
		FALSE(() -> false),
		TRUE(() -> true),
		RANDOM(() -> ThreadLocalRandom.current().nextBoolean()),
		;
		public final BooleanSupplier sup;
		private Reverse(BooleanSupplier sup) {
			this.sup = sup;
		}
	}
	
	@Override
	public void load(Path configDir, QDIni config, boolean loadError) {
		colorToState.clear();
		invalidated = true;
		unrecoverableLoadError = false;
		if (image != null) {
			image.close();
			image = null;
		}
		
		getReverse = config.getEnum("general.reverse", Reverse.class).orElse(Reverse.FALSE).sup;
		sound = config.getBoolean("general.sound").orElse(false);
		
		if (loadError) {
			unrecoverableLoadError = true;
			return;
		}
		FabLog.timeAndCountWarnings("Loading of block_logo.png", () -> {
			Path imageFile = configDir.resolve("block_logo.png");
			if (!Files.exists(imageFile)) {
				try {
					Resources.asByteSource(MixinConfigPlugin.class.getClassLoader().getResource("default_block_logo.png")).copyTo(MoreFiles.asByteSink(imageFile));
				} catch (IOException e) {
					FabLog.warn("Failed to write default block logo", e);
					unrecoverableLoadError = true;
					return;
				}
			}
			try (InputStream is = Files.newInputStream(imageFile)) {
				image = NativeImage.read(is);
			} catch (IOException e) {
				FabLog.warn("Failed to load block logo", e);
				unrecoverableLoadError = true;
				return;
			}
			for (int x = 0; x < image.getWidth(); x++) {
				for (int y = 0; y < image.getHeight(); y++) {
					int color = image.getPixelColor(x, y);
					int alpha = (color>>24)&0xFF;
					if (alpha > 0 && alpha < 255) {
						FabLog.warn("At "+x+", "+y+" in block_logo.png: Found a pixel that is not fully transparent or fully opaque; ignoring it");
						image.setPixelColor(x, y, 0);
					}
				}
			}
		});
		
		for (String key : config.keySet()) {
			if (key.startsWith("pixels.")) {
				String color = key.substring(7);
				if (color.length() != 6) {
					FabLog.warn(key+" must be a 24-bit hex color like FF0000 (got "+color+") at "+config.getBlame(key));
					continue;
				}
				int colorInt = Integer.parseInt(color, 16);
				int swapped = colorInt&0x0000FF00;
				swapped |= (colorInt&0x00FF0000) >> 16;
				swapped |= (colorInt&0x000000FF) << 16;
				List<Resolvable<Block>> blocks = Lists.newArrayList();
				for (String s : Splitter.on(' ').split(config.get(key).orElse(""))) {
					blocks.add(Resolvable.of(new Identifier(s), Registry.BLOCK));
				}
				colorToState.put(swapped, () -> {
					Resolvable<Block> res = blocks.get(ThreadLocalRandom.current().nextInt(blocks.size()));
					Optional<Block> opt = res.get();
					if (!opt.isPresent()) {
						FabLog.warn("Couldn't find a block with ID "+res.getId()+" when rendering block logo");
						return Blocks.AIR.getDefaultState();
					}
					return opt.get().getDefaultState();
				});
			}
		}
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				int color = image.getPixelColor(x, y);
				int alpha = (color>>24)&0xFF;
				color &= 0x00FFFFFF;
				if (alpha == 255 && !colorToState.containsKey(color)) {
					FabLog.warn("At "+x+", "+y+" in block_logo.png: Found a pixel with a color that isn't in the config: "+Integer.toHexString(color|0xFF000000).substring(2).toUpperCase(Locale.ROOT)+"; ignoring it");
					image.setPixelColor(x, y, 0);
				}
			}
		}
	}

	@Override
	public String getConfigName() {
		return "block_logo";
	}

}
