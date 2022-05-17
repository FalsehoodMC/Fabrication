package com.unascribed.fabrication;

import com.google.common.base.Charsets;
import com.google.common.base.Enums;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.google.common.io.BaseEncoding;
import com.google.common.io.MoreFiles;
import com.google.common.io.Resources;
import com.unascribed.fabrication.FeaturesFile.FeatureEntry;
import com.unascribed.fabrication.QDIni.BadValueException;
import com.unascribed.fabrication.QDIni.IniTransformer;
import com.unascribed.fabrication.QDIni.SyntaxErrorException;
import com.unascribed.fabrication.support.ConfigLoader;
import com.unascribed.fabrication.support.ConfigValue;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.ResolvedConfigValue;
import com.unascribed.fabrication.support.SpecialEligibility;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
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
import java.util.stream.Collectors;

public class FabConf {

	private static final ImmutableSet<String> NON_STANDARD = ImmutableSet.of(
			"general.profile"
			);
	private static final ImmutableSet<String> RUNTIME_CONFIGURABLE = ImmutableSet.of(
			"general.reduced_motion",
			"general.data_upload",
			"minor_mechanics.feather_falling_five_damages_boots",
			"minor_mechanics.observers_see_entities_living_only"
			);

	public enum Profile {
		GREEN,
		BLONDE,
		LIGHT,
		MEDIUM,
		DARK,
		VIENNA,
		BURNT,
		;
		public final ImmutableSet<String> sections;
		Profile(String... sections) {
			this.sections = ImmutableSet.copyOf(sections);
		}
		public static String[] stringValues() {
			Profile[] values = values();
			String[] out = new String[values.length];
			for (int i = 0; i < values.length; i++) {
				out[i] = values[i].name().toLowerCase(Locale.ROOT);
			}
			return out;
		}
	}

	private static final ImmutableTable<Profile, String, Boolean> defaultsByProfile;
	private static final ImmutableSet<String> validSections;
	private static final ImmutableSet<String> validKeys;
	private static final ImmutableMap<String, String> starMap;
	private static final ImmutableMap<String, Set<String>> equivalanceMap;
	private static final List<ConfigLoader> loaders = Lists.newArrayList();
	private static final Set<SpecialEligibility> metSpecialEligibility = EnumSet.noneOf(SpecialEligibility.class);
	private static final Set<String> failures = Sets.newHashSet();
	private static final Set<String> failuresReadOnly = Collections.unmodifiableSet(failures);
	private static final SetMultimap<String, String> configKeysForDiscoveredClasses = HashMultimap.create();
	private static Map<String, ConfigValue> worldConfig = new HashMap<>();
	private static Map<String, Boolean> worldDefaults = new HashMap<>();
	private static Profile worldProfile;
	private static Profile profile;
	private static QDIni rawConfig;
	private static Map<String, ConfigValue> config = new HashMap<>();
	private static boolean analyticsSafe = false;
	private static ImmutableMap<String, Boolean> defaults;
	private static Path worldPath = null;
	private static Path lastWorldPath = null;
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
		if (FabConf.class.getClassLoader().getResource("default_features_config.ini") == null) {
			throw devError("You must run build-features.sh before running the game.");
		}
		Map<String, String> starMapBldr = Maps.newLinkedHashMap();
		Map<String, Set<String>> equivalanceMapBldr = Maps.newLinkedHashMap();
		Table<Profile, String, Boolean> profilesBldr = Tables.newCustomTable(Maps.newLinkedHashMap(), Maps::newLinkedHashMap);
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
			String extend = en.getValue().extend;
			if (extend != null){
				if (!equivalanceMapBldr.containsKey(extend)) equivalanceMapBldr.put(extend, new HashSet<>());
				equivalanceMapBldr.get(extend).add(key);
			}
			keys.add(key);
			int dot = key.indexOf('.');
			String parent = null;
			if (dot != -1) {
				starMapBldr.put("*"+key.substring(dot), key);
				int lastDot = key.lastIndexOf('.');
				if (lastDot != dot) {
					starMapBldr.put("*"+key.substring(lastDot), key);
				}
				parent = key.substring(0, dot);
			}
			Profile defAt = null;
			String def = en.getValue().def;
			if ("inherit".equals(def)) {
				if (parent != null) {
					FeatureEntry pfe = FeaturesFile.get(parent);
					defAt = Enums.getIfPresent(Profile.class, pfe.def.toUpperCase(Locale.ROOT)).orNull();
				}
			} else {
				defAt = Enums.getIfPresent(Profile.class, def.toUpperCase(Locale.ROOT)).orNull();
			}
			for (Profile p : Profile.values()) {
				boolean enabled = defAt != null && p.ordinal() >= defAt.ordinal();
				profilesBldr.put(p, en.getKey(), enabled);
			}
		}
		starMap = ImmutableMap.copyOf(starMapBldr);
		equivalanceMap = ImmutableMap.copyOf(equivalanceMapBldr.entrySet().stream().map(e-> new AbstractMap.SimpleImmutableEntry<>(remap(e.getKey()), e.getValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
		validKeys = ImmutableSet.copyOf(keys);
		validSections = ImmutableSet.copyOf(sections);
		defaultsByProfile = ImmutableTable.copyOf(profilesBldr);
	}

	public static boolean hasWorldPath(){
		return worldPath != null;
	}

	public static void resetWorldPath(){
		setWorldPath(lastWorldPath, false);
	}

	public static void setWorldPath(Path path) {
		setWorldPath(path, false);
	}

	public static void setWorldPath(Path path, boolean onLoad) {
		worldPath = path;
		if (path == null){
			worldConfig.clear();
			worldProfile = Profile.GREEN;
			worldDefaults = defaultsByProfile.row(worldProfile);
		}else if (onLoad) {
			lastWorldPath = path;
		}
		worldReload();
	}

	public static String getProfileName() {
		return profile.name();
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

	public static Set<String> getEquivalent(String configKey) {
		return equivalanceMap.getOrDefault(remap(configKey), new HashSet<>());
	}

	public static Set<String> getConfigKeysForDiscoveredClass(String clazz) {
		return Collections.unmodifiableSet(configKeysForDiscoveredClasses.get(clazz.replace('/', '.')));
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
			if (worldVal == ConfigValue.UNSET) {
				if (worldDefaults != null && worldDefaults.get(configKey) == Boolean.TRUE) return true;
			} else {
				return worldVal == ConfigValue.TRUE;
			}
		}
		if (!config.containsKey(configKey))
			return defaults != null && defaults.get(configKey);
		return config.get(configKey).resolve(defaults != null && defaults.get(configKey));
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
			if (worldVal != ConfigValue.UNSET) return worldVal;
		}
		return config.getOrDefault(remap(configKey), ConfigValue.UNSET);
	}

	public static boolean doesWorldContainValue(String configKey){
		return worldConfig.get(configKey) != ConfigValue.UNSET || worldDefaults != null && worldDefaults.get(configKey) == Boolean.TRUE;
	}
	public static boolean doesWorldContainValue(String configKey, String configVal){
		if (!worldConfig.containsKey(configKey)) return false;
		return worldConfig.get(configKey).toString().equals(configVal.toUpperCase(Locale.ROOT));
	}

	public static ResolvedConfigValue getResolvedValue(String configKey) {
		if (isBanned(configKey)) return ResolvedConfigValue.BANNED;
		if (isFailed(configKey)) return ResolvedConfigValue.FALSE;
		if (hasWorldPath()) {
			ConfigValue cv = config.get(remap(configKey));
			return worldConfig.getOrDefault(remap(configKey), ConfigValue.UNSET).resolveSemantically(
					worldDefaults != null && worldDefaults.getOrDefault(configKey, false) ||
					 cv == ConfigValue.TRUE ||
					cv == ConfigValue.UNSET && defaults != null && defaults.getOrDefault(configKey, false));
		}
		return config.getOrDefault(remap(configKey), ConfigValue.UNSET).resolveSemantically(defaults != null && defaults.getOrDefault(configKey, false));
	}

	public static boolean isStandardValue(String s) {
		return !NON_STANDARD.contains(s);
	}

	public static boolean isRuntimeConfigurable(String s) {
		return RUNTIME_CONFIGURABLE.contains(s);
	}

	public static String getRawValue(String configKey) {
		configKey = remap(configKey);
		return rawConfig.get(configKey).orElse(configKey.equals("general.profile") ? "light" : "");
	}

	public static Profile getWorldProfile(){
		return worldProfile;
	}

	public static boolean isValid(String configKey) {
		return validKeys.contains(remap(configKey));
	}

	public static boolean getDefault(String configKey) {
		return defaults != null && defaults.get(remap(configKey));
	}

	public static boolean defaultContains(String configKey) {
		return defaults != null && defaults.containsKey(remap(configKey));
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

	private static void set(String configKey, String newValue, Path path, boolean isWorld) {
		if (isStandardValue(configKey)) {
			switch (newValue) {
				case "banned": case "true": case "false": case "unset":
					break;
				default: throw new IllegalArgumentException("Standard key "+configKey+" cannot be set to "+newValue);
			}
		} else if ("general.profile".equals(configKey)) {
			if (!Enums.getIfPresent(Profile.class, newValue.toUpperCase(Locale.ROOT)).isPresent()) {
				throw new IllegalArgumentException("Cannot set profile to "+newValue);
			}
		}
		if (!isWorld) rawConfig.put(configKey, newValue);
		if ("general.profile".equals(configKey)) {
			if (isWorld) {
				try {
					worldProfile = Profile.valueOf(newValue.toUpperCase(Locale.ROOT));
				} catch (Exception e){
					worldProfile = Profile.GREEN;
				}
				worldDefaults = defaultsByProfile.row(profile);
			} else {
				profile = rawConfig.getEnum("general.profile", Profile.class).orElse(Profile.LIGHT);
				defaults = defaultsByProfile.row(profile);
			}
		} else {
			(isWorld? worldConfig : config).put(configKey, ConfigValue.parseTrilean(newValue));
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
				profile = rawConfig.getEnum("general.profile", Profile.class).orElse(Profile.LIGHT);
				defaults = defaultsByProfile.row(profile);
				config = new HashMap<>();
				for (String k : rawConfig.keySet()) {
					if (isStandardValue(k)) {
						try {
							config.put(k, rawConfig.getEnum(k, ConfigValue.class).get());
						} catch (BadValueException e) {
							FabLog.warn(e.getMessage()+" - assuming unset");
							config.put(k, ConfigValue.UNSET);
						}
					}
				}
			} catch (SyntaxErrorException e) {
				FabLog.warn("Failed to load configuration file: "+e.getMessage()+"; will assume defaults");
				config = Maps.transformValues(defaults, v -> ConfigValue.UNSET);
			} catch (IOException e) {
				FabLog.warn("Failed to load configuration file; will assume defaults", e);
				config = Maps.transformValues(defaults, v -> ConfigValue.UNSET);
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
		checkForAndSaveDefaultsOrUpgrade(configFile, "default_features_config.ini", IniTransformer.simpleValueIniTransformer(((key, value) -> "general.profile".equals(key)? "green" : value)));
		FabLog.timeAndCountWarnings("Loading of features.ini", () -> {
			StringWriter sw = new StringWriter();
			try {
				QDIni rawConfig = QDIni.loadAndTransform(configFile, featuresIniTransformer.reset(), sw);
				worldProfile = rawConfig.getEnum("general.profile", Profile.class).orElse(Profile.GREEN);
				worldDefaults = defaultsByProfile.row(worldProfile);
				worldConfig = new HashMap<>();
				for (String k : rawConfig.keySet()) {
					if (isStandardValue(k)) {
						try {
							worldConfig.put(k, rawConfig.getEnum(k, ConfigValue.class).get());
						} catch (BadValueException e) {
							FabLog.warn(e.getMessage() + " - assuming unset");
							worldConfig.put(k, ConfigValue.UNSET);
						}
					}
				}
			} catch (SyntaxErrorException e) {
				FabLog.warn("Failed to load configuration file: " + e.getMessage() + "; will assume defaults");
				worldConfig = Maps.transformValues(worldDefaults, v -> ConfigValue.UNSET);
			} catch (IOException e) {
				FabLog.warn("Failed to load configuration file; will assume defaults", e);
				worldConfig = Maps.transformValues(worldDefaults, v -> ConfigValue.UNSET);
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
				QDIni qd = QDIni.load(file);
				qd.setYapLog(FabLog::warn);
				ldr.load(dir, qd, false);
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
			if (transformer == null && Files.exists(configFileOld)){
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
