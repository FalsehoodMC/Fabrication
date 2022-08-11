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
import com.google.common.io.BaseEncoding;
import com.google.common.io.MoreFiles;
import com.google.common.io.Resources;
import com.unascribed.fabrication.FeaturesFile.FeatureEntry;
import com.unascribed.fabrication.QDIni.IniTransformer;
import com.unascribed.fabrication.QDIni.SyntaxErrorException;
import com.unascribed.fabrication.support.ConfigLoader;
import com.unascribed.fabrication.support.ConfigValue;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.ResolvedConfigValue;
import com.unascribed.fabrication.support.SpecialEligibility;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

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
import java.util.function.Predicate;
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
	private static final ImmutableMap<String, ImmutableSet<String>> sectionFeatureKeyToFeatures;
	private static final List<ConfigLoader> loaders = Lists.newArrayList();
	private static final Set<SpecialEligibility> metSpecialEligibility = EnumSet.noneOf(SpecialEligibility.class);
	private static final Set<String> failures = Sets.newHashSet();
	private static final Set<String> failuresReadOnly = Collections.unmodifiableSet(failures);
	private static Map<String, ConfigValue> worldConfig = new HashMap<>();
	private static ImmutableMap<String, Boolean> worldDefaults = ImmutableMap.of();
	private static QDIni rawConfig;
	private static Map<String, ConfigValue> config = new HashMap<>();
	private static boolean analyticsSafe = false;
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
		try {
			// net.fabricmc.loader.api.FabricLoader
			// base64-encoded so that Shadow doesn't pick up on it and mess up the check
			Class.forName(new String(BaseEncoding.base64().decode("bmV0LmZhYnJpY21jLmxvYWRlci5hcGkuRmFicmljTG9hZGVy"), Charsets.UTF_8));
			setMet(SpecialEligibility.NOT_FORGE, true);
		} catch (Throwable t) {
			setMet(SpecialEligibility.FORGE, true);
		}
		try {
			Class.forName("optifine.Installer", false, FabConf.class.getClassLoader());
		} catch (Throwable t) {
			setMet(SpecialEligibility.NO_OPTIFINE, true);
		}
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			determineClientEligibility();
		}
		if (FabConf.class.getClassLoader().getResource("default_features_config.ini") == null) {
			throw devError("You must run build-features.sh before running the game.");
		}
		Map<String, String> starMapBldr = Maps.newLinkedHashMap();
		Map<String, Set<String>> equivalanceMapBldr = Maps.newLinkedHashMap();
		Map<String, Set<String>> sectionFeatureKeyToFeaturesBldr = Maps.newLinkedHashMap();
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
				sectionFeatureKeyToFeaturesBldr.put(key.substring(17), Sets.newHashSet());
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
			if (FeaturesFile.get(key).extra) continue;
			int dot = key.indexOf('.');
			if (dot == -1) continue;
			Set<String> set = sectionFeatureKeyToFeaturesBldr.get(key.substring(0, dot));
			if (set != null) {
				set.add(key);
			}
		}

		starMap = ImmutableMap.copyOf(starMapBldr);
		equivalanceMap = ImmutableMap.copyOf(equivalanceMapBldr.entrySet().stream().map(e-> new AbstractMap.SimpleImmutableEntry<>(remap(e.getKey()), ImmutableSet.copyOf(e.getValue()))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
		validKeys = ImmutableSet.copyOf(keys);
		validSections = ImmutableSet.copyOf(sections);
		sectionFeatureKeyToFeatures = ImmutableMap.copyOf(sectionFeatureKeyToFeaturesBldr.entrySet().stream().map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), ImmutableSet.copyOf(e.getValue()))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
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
			worldDefaults = ImmutableMap.of();
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

	public static void submitConfigAnalytics() {
		Analytics.submitConfig();
		analyticsSafe = true;
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
		return config.get("general.limit_runtime_configs") == ConfigValue.TRUE;
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
			ConfigValue worldVal = worldConfig.get(configKey);
			if (worldVal == ConfigValue.UNSET || worldVal == null) {
				Boolean bl = worldDefaults.get(configKey);
				if (bl != null) return bl;
			} else {
				return worldVal == ConfigValue.TRUE;
			}
		}
		if (!config.containsKey(configKey))
			return defaults.contains(configKey);
		return config.get(configKey).resolve(defaults.contains(configKey));
	}

	public static boolean isBanned(String configKey) {
		String k = remap(configKey);
		if (Agnos.getCurrentEnv() == Env.CLIENT && loadComplete) {
			if (clientCheckBanned(k)) {
				return true;
			}
		}
		ConfigValue worldVal = worldConfig.get(configKey);
		if (worldVal == ConfigValue.BANNED) return true;
		return config.get(k) == ConfigValue.BANNED;
	}

	@Environment(EnvType.CLIENT)
	private static boolean clientCheckBanned(String configKey) {
		return FabricationModClient.isBannedByServer(configKey);
	}

	public static boolean isFailed(String configKey) {
		return failures.contains(remap(configKey));
	}

	public static ConfigValue getValue(String configKey) {
		if (isBanned(configKey)) return ConfigValue.BANNED;
		if (isFailed(configKey)) return ConfigValue.FALSE;
		if (hasWorldPath()) {
			ConfigValue worldVal = worldConfig.get(configKey);
			if (worldVal != ConfigValue.UNSET && worldVal != null) return worldVal;
		}
		return config.getOrDefault(remap(configKey), ConfigValue.UNSET);
	}

	public static boolean doesWorldContainValue(String configKey){
		return worldConfig.containsKey(configKey) && worldConfig.get(configKey) != ConfigValue.UNSET || worldDefaults.containsKey(configKey);
	}
	public static boolean doesWorldContainValue(String configKey, String configVal){
		if (!worldConfig.containsKey(configKey)) return false;
		return worldConfig.get(configKey).toString().equals(configVal.toUpperCase(Locale.ROOT));
	}
	public static ResolvedConfigValue getResolvedValue(String configKey) {
		return getResolvedValue(configKey, true);
	}
	public static ResolvedConfigValue getResolvedValue(String configKey, boolean includeWorld) {
		if (isBanned(configKey)) return ResolvedConfigValue.BANNED;
		if (isFailed(configKey)) return ResolvedConfigValue.FALSE;
		if (includeWorld && hasWorldPath()) {
			ConfigValue cv = config.get(remap(configKey));
			Boolean worldDef = worldDefaults.get(configKey);
			return worldConfig.getOrDefault(remap(configKey), ConfigValue.UNSET).resolveSemantically(
					worldDef != null ? worldDef :
					cv == ConfigValue.TRUE ||
					cv == ConfigValue.UNSET && defaults.contains(configKey));
		}
		return config.getOrDefault(remap(configKey), ConfigValue.UNSET).resolveSemantically(defaults.contains(configKey));
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
			ConfigValue worldVal = worldConfig.get(en.getKey());
			return en.getValue() == ConfigValue.BANNED || worldVal == ConfigValue.BANNED;
		}), Map.Entry::getKey);
	}

	public static void addFailure(String configKey) {
		failures.add(remap(configKey));
	}
	private static void getDefaults(Predicate<String> shouldSkipSection, Consumer<String> add) {
		for (Map.Entry<String, ImmutableSet<String>> entry : sectionFeatureKeyToFeatures.entrySet()) {
			if (shouldSkipSection.test("general.category."+entry.getKey())) continue;
			for (String str : entry.getValue()){
				add.accept(str);
			}
		}
	}
	private static ImmutableSet<String> getDefaults(Predicate<String> shouldSkipSection) {
		ImmutableSet.Builder<String> builder = ImmutableSet.builder();
		getDefaults(shouldSkipSection, builder::add);
		return builder.build();
	}
	private static void set(String configKey, String newValue, Path path, boolean isWorld) {
		switch (newValue) {
			case "banned": case "true": case "false": case "unset":
				break;
			default:
				throw new IllegalArgumentException("Standard key " + configKey + " cannot be set to " + newValue);
		}

		if (!isWorld) rawConfig.put(configKey, newValue);
		(isWorld? worldConfig : config).put(configKey, ConfigValue.parseTrilean(newValue));
		if (configKey.startsWith("general.category.")) {
			if (isWorld) {
				ImmutableMap.Builder<String, Boolean> bldr = ImmutableMap.builder();
				getDefaults(s -> worldConfig.get(s) != ConfigValue.TRUE, e -> bldr.put(e, true));
				getDefaults(s -> worldConfig.get(s) != ConfigValue.FALSE, e -> bldr.put(e, false));
				worldDefaults = bldr.build();
			} else {
				defaults = getDefaults(s -> config.get(s) != ConfigValue.TRUE);
			}
		}
		if ("general.data_upload".equals(configKey)) {
			if ("true".equals(newValue)) {
				Analytics.submit("enable_analytics");
				submitConfigAnalytics();
			} else {
				Analytics.deleteId();
			}
		}
		write(configKey, newValue, path);
	}

	public static void worldSet(String configKey, String newValue) {
		if (worldPath == null) {
			FabLog.error("worldSet was called while path was null");
			return;
		}
		set(configKey, newValue, worldPath.resolve("fabrication").resolve("features.ini"), true);
	}

	public static void set(String configKey, String newValue) {
		set(configKey, newValue, Agnos.getConfigDir().resolve("fabrication").resolve("features.ini"), false);
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
		Path dir = Agnos.getConfigDir().resolve("fabrication");
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
				defaults = getDefaults(s -> !rawConfig.get(s).map("true"::equals).orElse(false));
				config = new HashMap<>();
				for (String k : rawConfig.keySet()) {
					config.put(k, rawConfig.getEnum(k, ConfigValue.class).orElseGet(() -> {
						FabLog.warn("Could not parse " + k + " = " + rawConfig.get(k).orElse("") + " - assuming unset");
						return ConfigValue.UNSET;
					}));
				}
			} catch (SyntaxErrorException e) {
				FabLog.warn("Failed to load configuration file: "+e.getMessage()+"; will assume defaults");
				config = validKeys.stream().collect(Collectors.toMap(v -> v, v -> ConfigValue.UNSET));
			} catch (IOException e) {
				FabLog.warn("Failed to load configuration file; will assume defaults", e);
				config = validKeys.stream().collect(Collectors.toMap(v -> v, v -> ConfigValue.UNSET));
			}
			try {
				Files.write(configFile, sw.toString().getBytes(Charsets.UTF_8));
			} catch (IOException e) {
				FabLog.warn("Failed to transform configuration file", e);
			}
		});
		worldReload();
		if (analyticsSafe) {
			submitConfigAnalytics();
		}
		for (ConfigLoader ldr : loaders) {
			load(ldr);
		}
	}

	public static void worldReload() {
		if (worldPath == null) return;
		Path dir = worldPath.resolve("fabrication");
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
				ImmutableMap.Builder<String, Boolean> bldr = ImmutableMap.builder();
				getDefaults(s -> !rawConfig.get(s).map("true"::equals).orElse(false), e -> bldr.put(e, true));
				getDefaults(s -> !rawConfig.get(s).map("false"::equals).orElse(false), e -> bldr.put(e, false));
				worldDefaults = bldr.build();
				worldConfig = new HashMap<>();
				for (String k : rawConfig.keySet()) {
					worldConfig.put(k, rawConfig.getEnum(k, ConfigValue.class).orElseGet(() -> {
						FabLog.warn("Could not parse " + k + " = " + rawConfig.get(k).orElse("") + " - assuming unset");
						return ConfigValue.UNSET;
					}));
				}
			} catch (SyntaxErrorException e) {
				FabLog.warn("Failed to load configuration file: " + e.getMessage() + "; will assume defaults");
				worldConfig = validKeys.stream().collect(Collectors.toMap(v -> v, v -> ConfigValue.UNSET));
			} catch (IOException e) {
				FabLog.warn("Failed to load configuration file; will assume defaults", e);
				worldConfig = validKeys.stream().collect(Collectors.toMap(v -> v, v -> ConfigValue.UNSET));
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
		Path dir = Agnos.getConfigDir().resolve("fabrication");
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
