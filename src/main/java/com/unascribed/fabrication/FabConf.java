package com.unascribed.fabrication;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.MoreFiles;
import com.google.common.io.Resources;
import com.unascribed.fabrication.FeaturesFile.FeatureEntry;
import com.unascribed.fabrication.QDIni.IniTransformer;
import com.unascribed.fabrication.QDIni.SyntaxErrorException;
import com.unascribed.fabrication.support.ConfigLoader;
import com.unascribed.fabrication.support.ConfigValues;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.SpecialEligibility;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

import net.minecraft.util.Pair;
import org.lwjgl.system.Platform;

import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FabConf {
	private static final ImmutableSet<String> RUNTIME_CONFIGURABLE = ImmutableSet.of(
			"general.reduced_motion",
			"general.data_upload",
			"minor_mechanics.feather_falling_five_damages_boots",
			"minor_mechanics.observers_see_entities_living_only"
			);

	private static final ImmutableSet<String> validSections;
	private static final ImmutableSet<String> validKeys;
	private static final ImmutableMap<String, String> starMap;
	private static final ImmutableMap<String, ImmutableSet<String>> equivalanceMap;
	//Pair, left - Non Extra, right - Extra
	private static final ImmutableMap<String, Pair<ImmutableSet<String>, ImmutableSet<String>>> sectionFeatureKeyToFeatures;
	private static final List<ConfigLoader> loaders = Lists.newArrayList();
	private static final Set<SpecialEligibility> metSpecialEligibility = EnumSet.noneOf(SpecialEligibility.class);
	private static final Set<String> failures = Sets.newHashSet();
	private static final Set<String> failuresReadOnly = Collections.unmodifiableSet(failures);
	private static Map<String, ConfigValues.Feature> worldConfig = new HashMap<>();
	private static QDIni rawConfig;
	public static Map<String, ConfigValues.Category> categoryConfig = new HashMap<>();
	private static Map<String, ConfigValues.Feature> config = new HashMap<>();
	private static ImmutableSet<String> defaults = ImmutableSet.of();
	private static Path worldPath = null;
	public static boolean loadComplete = false;

	private static class FeaturesIniTransformer implements IniTransformer {
		final static String NOTICES_HEADER = "; Notices: (Do not edit anything past this line; it will be overwritten)";

		Set<String> encounteredKeys = Sets.newHashSet();
		List<String> notices = Lists.newArrayList();
		boolean encounteredNotices = false;

		public FeaturesIniTransformer reset(){
			encounteredKeys = Sets.newHashSet();
			notices = Lists.newArrayList();
			encounteredNotices = false;
			return this;
		}

		@Override
		public String transformLine(String path, String line) {
			if ((!encounteredNotices && line == null) || (line != null && line.trim().equals(NOTICES_HEADER))) {
				encounteredNotices = true;
				boolean badKeys = false;
				for (String s : validKeys) {
					if (!encounteredKeys.contains(s)) {
						notices.add("- "+s+" was not found");
						badKeys = true;
					}
				}
				for (String s : encounteredKeys) {
					if (!validKeys.contains(s)) {
						notices.add("- "+s+" is not recognized");
						badKeys = true;
					}
				}
				if (badKeys) {
					notices.add("Consider updating this config file by renaming it to fabrication.ini.old");
				}
				if (notices.isEmpty()) {
					return NOTICES_HEADER+"\r\n; - No notices. You're in the clear!";
				}
				return NOTICES_HEADER+"\r\n; "+Joiner.on("\r\n; ").join(notices);
			}
			return encounteredNotices ? null : line;
		}

		@Override
		public String transformValueComment(String key, String value, String comment) {
			return comment;
		}

		@Override
		public String transformValue(String key, String value) {
			encounteredKeys.add(key);
			return value;
		}
	}
	private static final FeaturesIniTransformer featuresIniTransformer = new FeaturesIniTransformer();

	static {
		if (EarlyAgnos.isForge()) {
			setMet(SpecialEligibility.FORGE, true);
		} else {
			setMet(SpecialEligibility.NOT_FORGE, true);
		}
		try {
			Class.forName("optifine.Installer", false, FabConf.class.getClassLoader());
		} catch (Throwable t) {
			setMet(SpecialEligibility.NO_OPTIFINE, true);
		}
		if (EarlyAgnos.getCurrentEnv() == Env.CLIENT) {
			determineClientEligibility();
		}
		if (FabConf.class.getClassLoader().getResource("default_features_config.ini") == null) {
			throw devError("You must run build-features.sh before running the game.");
		}
		Map<String, String> starMapBldr = Maps.newLinkedHashMap();
		Map<String, Set<String>> equivalanceMapBldr = Maps.newLinkedHashMap();
		Map<String, Pair<Set<String>, Set<String>>> sectionFeatureKeyToFeaturesBldr = Maps.newLinkedHashMap();
		Set<String> keys = Sets.newLinkedHashSet();
		Set<String> sections = Sets.newLinkedHashSet();
		for (Map.Entry<String, FeatureEntry> en : FeaturesFile.getAll().entrySet()) {
			if (en.getValue().meta) {
				continue;
			}
			if (en.getValue().section) {
				sections.add(en.getKey());
				continue;
			}
			String key = en.getKey();
			if (key.startsWith("general.category.")) {
				sectionFeatureKeyToFeaturesBldr.put(key, new Pair<>(Sets.newHashSet(), Sets.newHashSet()));
			}
			String extend = en.getValue().extend;
			if (extend != null){
				if (!equivalanceMapBldr.containsKey(extend)) equivalanceMapBldr.put(extend, new HashSet<>());
				equivalanceMapBldr.get(extend).add(key);
			}
			keys.add(key);
			int dot = key.indexOf('.');
			if (dot != -1) {
				starMapBldr.put("*"+key.substring(dot), key);
				int lastDot = key.lastIndexOf('.');
				if (lastDot != dot) {
					starMapBldr.put("*"+key.substring(lastDot), key);
				}
			}
		}
		for (String key : keys) {
			int dot = key.indexOf('.');
			if (dot == -1) continue;
			Pair<Set<String>, Set<String>> sets = sectionFeatureKeyToFeaturesBldr.get("general.category."+key.substring(0, dot));
			if (sets == null) continue;
			Set<String> set = FeaturesFile.get(key).extra ? sets.getRight() : sets.getLeft();
			if (set == null) continue;
			set.add(key);
		}

		starMap = ImmutableMap.copyOf(starMapBldr);
		equivalanceMap = ImmutableMap.copyOf(equivalanceMapBldr.entrySet().stream().map(e-> new AbstractMap.SimpleImmutableEntry<>(remap(e.getKey()), ImmutableSet.copyOf(e.getValue()))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
		validKeys = ImmutableSet.copyOf(keys);
		validSections = ImmutableSet.copyOf(sections);
		sectionFeatureKeyToFeatures = ImmutableMap.copyOf(sectionFeatureKeyToFeaturesBldr.entrySet().stream().map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), new Pair<>(ImmutableSet.copyOf(e.getValue().getLeft()), ImmutableSet.copyOf(e.getValue().getRight())))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
	}

	public static boolean hasWorldPath(){
		return worldPath != null;
	}

	@Environment(EnvType.CLIENT)
	private static void determineClientEligibility() {
		setMet(SpecialEligibility.NOT_MACOS, Platform.get() != Platform.MACOSX);
	}

	public static void setWorldPath(Path path) {
		setWorldPath(path, false);
	}

	public static void setWorldPath(Path path, boolean onLoad) {
		worldPath = path;
		if (path == null){
			worldConfig.clear();
		} else if (onLoad) {
			Path target = path.resolve(MixinConfigPlugin.MOD_NAME_LOWER);
			Path source = path.resolve(EarlyAgnos.isForge() ? "fabrication" : "forgery");
			if (!Files.exists(target) && Files.exists(source)) {
				try {
					Files.move(source, target);
				} catch (IOException ignore) {}
			}
		}
		worldReload();
	}

	public static void setMet(SpecialEligibility se, boolean met) {
		if (met) {
			metSpecialEligibility.add(se);
		} else {
			metSpecialEligibility.remove(se);
		}
	}

	public static boolean isMet(SpecialEligibility se) {
		return metSpecialEligibility.contains(se);
	}

	public static String remap(String configKey) {
		return starMap.getOrDefault(configKey, configKey);
	}

	public static ImmutableSet<String> getEquivalent(String configKey) {
		return equivalanceMap.getOrDefault(remap(configKey), ImmutableSet.of());
	}

	public static RuntimeException devError(String msg) {
		try {
			UIManager.setLookAndFeel(new MetalLookAndFeel());
			JFrame dummyFrame = new JFrame();
			dummyFrame.setIconImage(Toolkit.getDefaultToolkit().createImage(FabConf.class.getClassLoader().getResource("assets/fabrication/icon.png")));
			dummyFrame.setSize(1, 1);
			dummyFrame.setLocationRelativeTo(null);
			JOptionPane.showOptionDialog(dummyFrame, msg, "Fabrication Dev Error",
					JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE,
					null,
					new String[] {"Exit"}, "Exit");
			System.exit(1);
		} catch (Throwable ignore) {}
		return new RuntimeException(msg);
	}

	public static boolean limitRuntimeConfigs() {
		return config.get("general.limit_runtime_configs") == ConfigValues.Feature.TRUE;
	}

	public static boolean isAnyEnabled(String configKey) {
		if (isEnabled(configKey)) return true;
		return getEquivalent(configKey).stream().anyMatch(FabConf::isEnabled);
	}

	public static boolean isEnabled(String configKey) {
		if (isFailed(configKey) || isBanned(configKey)) return false;
		configKey = remap(configKey);
		if (!validKeys.contains(configKey)) {
			FabLog.error("Cannot look up value for config key "+configKey+" with no default");
			return false;
		}
		if (hasWorldPath()) {
			ConfigValues.Feature worldVal = worldConfig.get(configKey);
			if (worldVal != ConfigValues.Feature.UNSET && worldVal != null) {
				return worldVal == ConfigValues.Feature.TRUE;
			}
		}
		if (!config.containsKey(configKey))
			return defaults.contains(configKey);
		return config.get(configKey).resolve(defaults.contains(configKey));
	}

	public static boolean isBanned(String configKey) {
		String k = remap(configKey);
		if (EarlyAgnos.getCurrentEnv() == Env.CLIENT && loadComplete) {
			if (clientCheckBanned(k)) {
				return true;
			}
		}
		ConfigValues.Feature worldVal = worldConfig.get(configKey);
		if (worldVal == ConfigValues.Feature.BANNED) return true;
		return config.get(k) == ConfigValues.Feature.BANNED;
	}

	@Environment(EnvType.CLIENT)
	private static boolean clientCheckBanned(String configKey) {
		return FabricationModClient.isBannedByServer(configKey);
	}

	public static boolean isFailed(String configKey) {
		return failures.contains(remap(configKey));
	}

	public static ConfigValues.Feature getValue(String configKey) {
		if (isBanned(configKey)) return ConfigValues.Feature.BANNED;
		if (isFailed(configKey)) return ConfigValues.Feature.FALSE;
		if (hasWorldPath()) {
			ConfigValues.Feature worldVal = worldConfig.get(configKey);
			if (worldVal != ConfigValues.Feature.UNSET && worldVal != null) return worldVal;
		}
		return config.getOrDefault(remap(configKey), ConfigValues.Feature.UNSET);
	}

	public static boolean doesWorldContainValue(String configKey){
		ConfigValues.Feature val = worldConfig.get(configKey);
		return val != null && val != ConfigValues.Feature.UNSET;
	}
	public static boolean doesWorldContainValue(String configKey, String configVal){
		if (!worldConfig.containsKey(configKey)) return false;
		return worldConfig.get(configKey).toString().equals(configVal.toUpperCase(Locale.ROOT));
	}
	public static ConfigValues.ResolvedFeature getResolvedValue(String configKey) {
		return getResolvedValue(configKey, true);
	}
	public static ConfigValues.ResolvedFeature getResolvedValue(String configKey, boolean includeWorld) {
		if (isBanned(configKey)) return ConfigValues.ResolvedFeature.BANNED;
		if (isFailed(configKey)) return ConfigValues.ResolvedFeature.FALSE;
		if (includeWorld && hasWorldPath()) {
			ConfigValues.Feature cv = config.get(remap(configKey));
			return worldConfig.getOrDefault(remap(configKey), ConfigValues.Feature.UNSET).resolveSemantically(
					cv == ConfigValues.Feature.TRUE ||
					cv == ConfigValues.Feature.UNSET && defaults.contains(configKey));
		}
		return config.getOrDefault(remap(configKey), ConfigValues.Feature.UNSET).resolveSemantically(defaults.contains(configKey));
	}

	public static boolean isRuntimeConfigurable(String s) {
		return RUNTIME_CONFIGURABLE.contains(s);
	}

	public static String getRawValue(String configKey) {
		configKey = remap(configKey);
		return rawConfig.get(configKey).orElse("");
	}

	public static boolean isValid(String configKey) {
		return validKeys.contains(remap(configKey));
	}

	public static boolean getDefault(String configKey) {
		return defaults.contains(remap(configKey));
	}

	public static ImmutableSet<String> getAllKeys() {
		return validKeys;
	}

	public static ImmutableSet<String> getAllSections() {
		return validSections;
	}

	public static Set<String> getAllFailures() {
		return failuresReadOnly;
	}

	public static Collection<String> getAllBanned() {
		return Collections2.transform(Collections2.filter(config.entrySet(), en ->{
			ConfigValues.Feature worldVal = worldConfig.get(en.getKey());
			return en.getValue() == ConfigValues.Feature.BANNED || worldVal == ConfigValues.Feature.BANNED;
		}), Map.Entry::getKey);
	}

	public static void addFailure(String configKey) {
		failures.add(remap(configKey));

	}
	private static void getDefaults(Function<String, ConfigValues.Category> getCategory, Consumer<String> add) {
		for (Map.Entry<String, Pair<ImmutableSet<String>, ImmutableSet<String>>> entry : sectionFeatureKeyToFeatures.entrySet()) {
			ConfigValues.Category cat = getCategory.apply(entry.getKey());
			if (cat == null) continue;
			switch (cat) {
				case ASH:
					for (String str : entry.getValue().getRight()){
						add.accept(str);
					}
				case DARK:
					for (String str : entry.getValue().getLeft()){
						add.accept(str);
					}
			}
		}
	}
	private static ImmutableSet<String> getDefaults(Function<String, ConfigValues.Category> getCategory) {
		ImmutableSet.Builder<String> builder = ImmutableSet.builder();
		getDefaults(getCategory, builder::add);
		return builder.build();
	}
	private static void set(String configKey, String newValue, Path path, boolean isWorld) {
		boolean isCategory = configKey.startsWith("general.category.");
		if (isCategory) {
			if (!ConfigValues.Category.isCategory(newValue)) {
				throw new IllegalArgumentException("Category key " + configKey + " cannot be set to " + newValue);
			}
		} else {
			switch (newValue) {
				case "banned": case "true": case "false": case "unset":
					break;
				default:
				throw new IllegalArgumentException("Standard key " + configKey + " cannot be set to " + newValue);
			}
		}
		if (isWorld) {
			if (!isCategory) {
				worldConfig.put(configKey, ConfigValues.Feature.parse(newValue));
			}
		} else {
			rawConfig.put(configKey, newValue);
			if (isCategory) {
				ConfigValues.Category category = ConfigValues.Category.parse(newValue);
				categoryConfig.put(configKey, category);
				defaults = getDefaults(categoryConfig::get);
			} else {
				config.put(configKey, ConfigValues.Feature.parse(newValue));
			}
		}
		write(configKey, newValue, path);
	}

	public static void worldSet(String configKey, String newValue) {
		if (worldPath == null) {
			FabLog.error("worldSet was called while path was null");
			return;
		}
		set(configKey, newValue, worldPath.resolve(MixinConfigPlugin.MOD_NAME_LOWER).resolve("features.ini"), true);
	}

	public static void set(String configKey, String newValue) {
		set(configKey, newValue, EarlyAgnos.getConfigDir().resolve(MixinConfigPlugin.MOD_NAME_LOWER).resolve("features.ini"), false);
	}

	private static void write(String configKey, String newValue, Path configFile){
		Stopwatch watch = Stopwatch.createStarted();
		StringWriter sw = new StringWriter();
		try {
			QDIni.loadAndTransform(configFile, new IniTransformer() {

				boolean found = false;
				boolean foundEmptySection = false;

				@Override
				public String transformLine(String path, String line) {
					if (line != null && line.startsWith("[]")) {
						foundEmptySection = true;
					}
					if (line == null || line.equals("; Notices: (Do not edit anything past this line; it will be overwritten)")) {
						if (!found) {
							found = true;
							return (foundEmptySection ? "" : "[]\r\n")+"; Added by runtime reconfiguration as a last resort as this key could\r\n"
									+ "; not be found elsewhere in the file.\r\n"
									+ configKey+"="+newValue+"\r\n"+(line == null ? "" : "\r\n"+line);
						}
					}
					return line;
				}

				@Override
				public String transformValueComment(String key, String value, String comment) {
					return comment;
				}

				@Override
				public String transformValue(String key, String value) {
					if (configKey.equals(key)) {
						found = true;
						return newValue;
					}
					return value;
				}

			}, sw);
			Files.write(configFile, sw.toString().getBytes(Charsets.UTF_8));
			FabLog.info("Update of "+configFile+" done in "+watch);
		} catch (IOException e) {
			FabLog.warn("Failed to update "+configFile+" file", e);
		}
	}

	public static void introduce(ConfigLoader ldr) {
		load(ldr);
		loaders.add(ldr);
	}
	public static void reload() {
		FabLog.info("Reloading configs...");
		Path dir = EarlyAgnos.getConfigDir().resolve(MixinConfigPlugin.MOD_NAME_LOWER);
		try {
			Files.createDirectories(dir);
		} catch (IOException e1) {
			throw new RuntimeException("Failed to create fabrication config directory", e1);
		}
		Path configFile = dir.resolve("features.ini");
		checkForAndSaveDefaultsOrUpgrade(configFile, "default_features_config.ini");
		FabLog.timeAndCountWarnings("Loading of features.ini", () -> {
			StringWriter sw = new StringWriter();
			try {
				rawConfig = QDIni.loadAndTransform(configFile, featuresIniTransformer.reset(), sw);
				defaults = getDefaults(s -> {
					try {
						return rawConfig.get(s).map(ConfigValues.Category::parse).orElse(ConfigValues.Category.GREEN);
					} catch (IllegalArgumentException badVal) {
						return ConfigValues.Category.GREEN;
					}
				});
				config = new HashMap<>();
				for (String k : rawConfig.keySet()) {
					if (k.startsWith("general.category")) {
						try {
							categoryConfig.put(k, rawConfig.get(k).map(ConfigValues.Category::parse).orElse(ConfigValues.Category.GREEN));
						} catch (IllegalArgumentException badVal) {
							FabLog.warn("Could not parse " + k + " = " + rawConfig.get(k).orElse("") + " - assuming green");
							categoryConfig.put(k, ConfigValues.Category.GREEN);
						}
					} else {
						config.put(k, rawConfig.getEnum(k, ConfigValues.Feature.class).orElseGet(() -> {
							FabLog.warn("Could not parse " + k + " = " + rawConfig.get(k).orElse("") + " - assuming unset");
							return ConfigValues.Feature.UNSET;
						}));
					}
				}
			} catch (SyntaxErrorException e) {
				FabLog.warn("Failed to load configuration file: "+e.getMessage()+"; will assume defaults");
				config = validKeys.stream().collect(Collectors.toMap(v -> v, v -> ConfigValues.Feature.UNSET));
			} catch (IOException e) {
				FabLog.warn("Failed to load configuration file; will assume defaults", e);
				config = validKeys.stream().collect(Collectors.toMap(v -> v, v -> ConfigValues.Feature.UNSET));
			}
			try {
				Files.write(configFile, sw.toString().getBytes(Charsets.UTF_8));
			} catch (IOException e) {
				FabLog.warn("Failed to transform configuration file", e);
			}
		});
		worldReload();
		for (ConfigLoader ldr : loaders) {
			load(ldr);
		}
	}

	public static void worldReload() {
		if (worldPath == null) return;
		Path dir = worldPath.resolve(MixinConfigPlugin.MOD_NAME_LOWER);
		try {
			Files.createDirectories(dir);
		} catch (IOException e1) {
			throw new RuntimeException("Failed to create fabrication world config directory", e1);
		}
		Path configFile = dir.resolve("features.ini");
		checkForAndSaveDefaultsOrUpgrade(configFile, "default_features_config.ini", IniTransformer.simpleValueIniTransformer((k,v) -> "unset"));
		FabLog.timeAndCountWarnings("Loading of features.ini", () -> {
			StringWriter sw = new StringWriter();
			try {
				QDIni rawConfig = QDIni.loadAndTransform(configFile, featuresIniTransformer.reset(), sw);
				worldConfig = new HashMap<>();
				for (String k : rawConfig.keySet()) {
					worldConfig.put(k, rawConfig.getEnum(k, ConfigValues.Feature.class).orElseGet(() -> {
						FabLog.warn("Could not parse " + k + " = " + rawConfig.get(k).orElse("") + " - assuming unset");
						return ConfigValues.Feature.UNSET;
					}));
				}
			} catch (SyntaxErrorException e) {
				FabLog.warn("Failed to load configuration file: " + e.getMessage() + "; will assume defaults");
				worldConfig = validKeys.stream().collect(Collectors.toMap(v -> v, v -> ConfigValues.Feature.UNSET));
			} catch (IOException e) {
				FabLog.warn("Failed to load configuration file; will assume defaults", e);
				worldConfig = validKeys.stream().collect(Collectors.toMap(v -> v, v -> ConfigValues.Feature.UNSET));
			}
			try {
				Files.write(configFile, sw.toString().getBytes(Charsets.UTF_8));
			} catch (IOException e) {
				FabLog.warn("Failed to transform configuration file", e);
			}
		});
	}

	private static void load(ConfigLoader ldr) {
		String name = ldr.getConfigName();
		Path dir = EarlyAgnos.getConfigDir().resolve(MixinConfigPlugin.MOD_NAME_LOWER);
		Path file = dir.resolve(name+".ini");
		checkForAndSaveDefaultsOrUpgrade(file, "default_"+name+"_config.ini");
		FabLog.timeAndCountWarnings("Loading of "+name+".ini", () -> {
			try {
				ldr.load(dir, QDIni.load(file), false);
			} catch (SyntaxErrorException e) {
				FabLog.warn("Failed to load "+name+": "+e.getMessage());
				ldr.load(dir, QDIni.load("<empty>", ""), true);
			} catch (IOException e) {
				FabLog.warn("Failed to load "+name+" configuration file", e);
			}
		});
	}
	private static void checkForAndSaveDefaultsOrUpgrade(Path configFile, String defaultName) {
		checkForAndSaveDefaultsOrUpgrade(configFile, defaultName, null);
	}
	private static void checkForAndSaveDefaultsOrUpgrade(Path configFile, String defaultName, IniTransformer transformer) {
		if (!Files.exists(configFile)) {
			Path configFileLegacy = configFile.getParent().getParent().resolve(defaultName.equals("default_features_config.ini") ? "fabrication.ini" : "fabrication_"+configFile.getFileName().toString());
			boolean migrated = false;
			if (Files.exists(configFileLegacy)) {
				try {
					Files.move(configFileLegacy, configFile);
					migrated = true;
				} catch (IOException e) {
					throw new RuntimeException("Failed to move legacy config file into directory");
				}
			}
			Path configFileLegacyOld = configFileLegacy.resolveSibling(configFileLegacy.getFileName()+".old");
			Path configFileOld = configFile.resolveSibling(configFile.getFileName()+".old");
			if (Files.exists(configFileLegacyOld)) {
				try {
					Files.move(configFileLegacyOld, configFileOld);
				} catch (IOException e) {
					throw new RuntimeException("Failed to move legacy old config file into directory");
				}
			}
			boolean loadedLegacy = false;
			if (Files.exists(configFileOld)){
				try {
					QDIni currentValues = QDIni.load(configFileOld);
					transformer = IniTransformer.simpleValueIniTransformer((key, value) -> currentValues.get(key).orElse(value));
					loadedLegacy = true;
				} catch (IOException e) {
					throw new RuntimeException("Failed to upgrade config", e);
				}
			}
			if (transformer != null) {
				try {
					QDIni.loadAndTransform(defaultName, new InputStreamReader(FabConf.class.getClassLoader().getResourceAsStream(defaultName), Charsets.UTF_8), transformer, new OutputStreamWriter(Files.newOutputStream(configFile), Charsets.UTF_8));
					if (loadedLegacy) Files.delete(configFileOld);
				} catch (IOException e) {
					throw new RuntimeException("Failed to upgrade config", e);
				}
			} else if (!migrated) {
				try {
					Resources.asByteSource(FabConf.class.getClassLoader().getResource(defaultName)).copyTo(MoreFiles.asByteSink(configFile));
				} catch (IOException e) {
					throw new RuntimeException("Failed to write default config", e);
				}
			}
		}
	}

}
