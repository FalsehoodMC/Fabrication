package com.unascribed.fabrication.loaders;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.QDIni;
import com.unascribed.fabrication.support.ConfigLoader;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import com.google.common.collect.Maps;
import com.google.common.io.MoreFiles;
import com.google.common.io.Resources;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.command.argument.BlockArgumentParser;

@EligibleIf(envMatches=Env.CLIENT)
public class LoaderBlockLogo implements ConfigLoader {

	public static boolean invalidated = true;
	public static boolean unrecoverableLoadError = false;
	public static NativeImage image;
	public static BooleanSupplier getReverse = () -> false;
	public static Reverse rawReverse = Reverse.FALSE;
	public static final Map<Integer, Supplier<BlockState>> colorToState = Maps.newHashMap();
	public static boolean sound = false;
	public static float shadowRed = 0.f;
	public static float shadowGreen = 0.f;
	public static float shadowBlue = 0.f;
	public static float shadowAlpha = 0.f;

	public static int rawShadowRed = 0;
	public static int rawShadowGreen = 0;
	public static int rawShadowBlue = 0;
	public static int rawShadowAlpha = 0;
	public static final Map<Integer, List<String>> fullColorToState = new HashMap<>();
	public static final Set<Integer> validColors = new HashSet<>();

	static Path imageFile;

	public enum Reverse {
		FALSE(() -> false),
		TRUE(() -> true),
		RANDOM(() -> ThreadLocalRandom.current().nextBoolean()),
		;
		public final BooleanSupplier sup;
		Reverse(BooleanSupplier sup) {
			this.sup = sup;
		}
	}

	public static void remove(String key) {
		Stopwatch watch = Stopwatch.createStarted();
		StringWriter sw = new StringWriter();
		Path configFile = Agnos.getConfigDir().resolve("fabrication").resolve("block_logo.ini");
		try {
			QDIni.loadAndTransform(configFile, QDIni.simpleLineIniTransformer(((path, line) -> {
				if (line == null) return null;
				int i = line.indexOf('=');
				if (i != -1 && key.equals(path+line.substring(0, i))) {
					return "";
				}
				return line;
			})), sw);
			Files.write(configFile, sw.toString().getBytes(Charsets.UTF_8));
			FabLog.info("Update of "+configFile+" done in "+watch);
		} catch (IOException e) {
			FabLog.warn("Failed to update "+configFile+" file", e);
		}
	}
	public static void set(String key, String val) {
		Stopwatch watch = Stopwatch.createStarted();
		StringWriter sw = new StringWriter();
		Path configFile = Agnos.getConfigDir().resolve("fabrication").resolve("block_logo.ini");
		try {
			AtomicBoolean found = new AtomicBoolean(false);
			QDIni.loadAndTransform(configFile, QDIni.simpleValueIniTransformer(((key1, value) -> {
				if (key1.equals(key)){
					found.set(true);
					return val;
				}
				return value;
			})), sw);
			if (!found.get()){
				StringWriter sw2 = new StringWriter();
				QDIni.loadAndTransform("LoaderBlockLogo internal append value", new ByteArrayInputStream(sw.toString().getBytes()),
						QDIni.simpleLineIniTransformer(((path, line) -> {
							if (line != null && !found.get() && path != null && !path.isEmpty() && key.startsWith(path)) {
								found.set(true);
								return line + "\n" + key.substring(path.length()) + "=" + val;
							}
							return line;
						})), sw2);
				Files.write(configFile, sw2.toString().getBytes(Charsets.UTF_8));
			} else {
				Files.write(configFile, sw.toString().getBytes(Charsets.UTF_8));
			}
			FabLog.info("Update of "+configFile+" done in "+watch);
		} catch (IOException e) {
			FabLog.warn("Failed to update "+configFile+" file", e);
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

		rawReverse = config.getEnum("general.reverse", Reverse.class).orElse(Reverse.FALSE);
		getReverse = rawReverse.sup;
		sound = config.getBoolean("general.sound").orElse(false);
		rawShadowRed = config.getInt("shadow.red").orElse(0);
		rawShadowGreen = config.getInt("shadow.green").orElse(0);
		rawShadowBlue = config.getInt("shadow.blue").orElse(0);
		rawShadowAlpha = config.getInt("shadow.alpha").orElse(0);
		shadowRed =  rawShadowRed / 255.f;
		shadowGreen = rawShadowGreen / 255.f;
		shadowBlue = rawShadowBlue / 255.f;
		shadowAlpha = rawShadowAlpha / 255.f;

		if (loadError) {
			unrecoverableLoadError = true;
			return;
		}
		FabLog.timeAndCountWarnings("Loading of block_logo.png", () -> {
			imageFile = configDir.resolve("block_logo.png");
			if (!Files.exists(imageFile)) {
				try {
					Resources.asByteSource(MixinConfigPlugin.class.getClassLoader().getResource("default_block_logo.png")).copyTo(MoreFiles.asByteSink(imageFile));
				} catch (IOException e) {
					FabLog.warn("Failed to write default block logo", e);
					unrecoverableLoadError = true;
					return;
				}
			}
			reloadImage();
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
					String[] blocks = config.get(key).orElse("").split(" ");
					fullColorToState.put(swapped, new ArrayList<>(Arrays.asList(blocks)));
					colorToState.put(swapped, () -> {
						// Since this Loader (like all loaders) is called on the mod's initialization,
						// the block registry might not have been populated by other mods yet.
						// Therefore late BlockState resolution is performed here; colorToState suppliers
						// are called by MixinTitleScreen, which runs after game init is done.
						String block = blocks[ThreadLocalRandom.current().nextInt(blocks.length)];
						try {
							BlockArgumentParser parser = new BlockArgumentParser(new StringReader(block), false);
							parser.parse(false);
							return parser.getBlockState();
						} catch (CommandSyntaxException e) {
							FabLog.warn(block+" is not a valid identifier at "+config.getBlame(key));
							return Blocks.AIR.getDefaultState();
						}
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

	public static void reloadImage() {
		try (InputStream is = Files.newInputStream(imageFile)) {
			image = NativeImage.read(is);
		} catch (IOException e) {
			FabLog.warn("Failed to load block logo", e);
			unrecoverableLoadError = true;
			return;
		}
		validColors.clear();
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				int color = image.getPixelColor(x, y);
				int alpha = (color>>24)&0xFF;
				if (alpha == 0) validColors.add(color);
				if (alpha > 0 && alpha < 255) {
					FabLog.warn("At "+x+", "+y+" in block_logo.png: Found a pixel that is not fully transparent or fully opaque; ignoring it");
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
