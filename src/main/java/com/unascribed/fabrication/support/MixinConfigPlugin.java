package com.unascribed.fabrication.support;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.Analytics;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.QDIni;
import com.unascribed.fabrication.QDIni.BadValueException;
import com.unascribed.fabrication.QDIni.IniTransformer;
import com.unascribed.fabrication.QDIni.SyntaxErrorException;
import com.unascribed.fabrication.support.injection.FailsoftCallbackInjectionInfo;
import com.unascribed.fabrication.support.injection.FailsoftModifyArgInjectionInfo;
import com.unascribed.fabrication.support.injection.FailsoftModifyArgsInjectionInfo;
import com.unascribed.fabrication.support.injection.FailsoftModifyConstantInjectionInfo;
import com.unascribed.fabrication.support.injection.FailsoftModifyVariableInjectionInfo;
import com.unascribed.fabrication.support.injection.FailsoftRedirectInjectionInfo;

import com.google.common.base.Charsets;
import com.google.common.base.Enums;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.io.BaseEncoding;
import com.google.common.io.MoreFiles;
import com.google.common.io.Resources;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

public class MixinConfigPlugin implements IMixinConfigPlugin {

	private static final ImmutableSet<String> VIENNA_EXCEPTIONS = ImmutableSet.of(
			"balance.infinity_mending",
			"balance.anvil_damage_only_on_fall",
			"balance.food_always_edible",
			"weird_tweaks.photoallergic_creepers",
			"weird_tweaks.photoresistant_mobs",
			"weird_tweaks.underwater_explosions",
			"woina.old_sheep_shear"
	);
	private static final ImmutableSet<String> NEVER_DEFAULT_EXCEPT_BURNT = ImmutableSet.of(
			"minor_mechanics.spiders_cant_climb_while_wet"
	);
	private static final ImmutableSet<String> NEVER_DEFAULT = ImmutableSet.of(
			"tweaks.ghost_chest_woo_woo",
			"woina.janky_arm",
			"woina.flat_items",
			"woina.billboard_drops",
			"woina.oof",
			"woina.no_experience",
			"general.data_upload",
			"balance.loading_furnace_minecart"
	);
	private static final ImmutableSet<String> NON_TRILEANS = ImmutableSet.of(
			"general.profile"
	);
	private static final ImmutableSet<String> RUNTIME_CONFIGURABLE = ImmutableSet.of(
			"general.reduced_motion",
			"general.data_upload",
			"minor_mechanics.feather_falling_five_damages_boots",
			"minor_mechanics.observers_see_entities_living_only"
	);
	
	
	public enum Profile {
		GREEN("general"),
		BLONDE("general", "fixes", "utility"),
		LIGHT("general", "fixes", "utility", "tweaks"),
		MEDIUM("general", "fixes", "utility", "tweaks", "minor_mechanics"),
		DARK("general", "fixes", "utility", "tweaks", "minor_mechanics", "mechanics"),
		VIENNA("general", "fixes", "utility", "tweaks", "minor_mechanics", "mechanics", "balance", "weird_tweaks", "woina"),
		BURNT("*", "!situational", "!experiments")
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
	
	private static final ImmutableMap<Profile, ImmutableMap<String, Boolean>> defaultsByProfile;
	private static final ImmutableSet<String> validSections;
	private static final ImmutableSet<String> validKeys;
	private static final ImmutableMap<String, String> starMap;
	private static ImmutableMap<String, Boolean> defaults;
	
	private static final Set<SpecialEligibility> metSpecialEligibility = EnumSet.noneOf(SpecialEligibility.class);
	
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
	
	private static final List<ConfigLoader> loaders = Lists.newArrayList();
	
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
			Class.forName("optifine.Installer", false, MixinConfigPlugin.class.getClassLoader());
		} catch (Throwable t) {
			setMet(SpecialEligibility.NO_OPTIFINE, true);
		}
		if (MixinConfigPlugin.class.getClassLoader().getResource("default_features_config.ini") == null) {
			throw devError("You must run build-features.sh before running the game.");
		}
		try (InputStream is = MixinConfigPlugin.class.getClassLoader().getResourceAsStream("default_features_config.ini")) {
			Set<String> keys = QDIni.load("default_features_config.ini", is).keySet();
			Set<String> sections = Sets.newHashSet();
			ImmutableMap.Builder<String, String> starMapBldr = ImmutableMap.builder();
			for (String key : keys) {
				int dot = key.indexOf('.');
				if (dot != -1) {
					starMapBldr.put("*"+key.substring(dot), key);
				}
			}
			starMap = starMapBldr.build();
			ImmutableMap.Builder<Profile, ImmutableMap<String, Boolean>> profilesBuilder = ImmutableMap.builder();
			for (Profile p : Profile.values()) {
				ImmutableMap.Builder<String, Boolean> defaultsBuilder = ImmutableMap.builder();
				for (String key : keys) {
					int dot = key.indexOf('.');
					String section = dot != -1 ? key.substring(0, dot) : "";
					if (!section.isEmpty()) sections.add(section);
					boolean enabled;
					if (NEVER_DEFAULT.contains(key) || (NEVER_DEFAULT_EXCEPT_BURNT.contains(key) && p != Profile.BURNT)) {
						enabled = false;
					} else {
						enabled = (p.sections.contains("*") && !p.sections.contains("!"+section)) || p.sections.contains(section)
								&& (p != Profile.VIENNA || !VIENNA_EXCEPTIONS.contains(key));
					}
					defaultsBuilder.put(key, enabled);
				}
				profilesBuilder.put(p, defaultsBuilder.build());
			}
			validKeys = ImmutableSet.copyOf(keys);
			validSections = ImmutableSet.copyOf(sections);
			defaultsByProfile = profilesBuilder.build();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static Profile profile;
	private static QDIni rawConfig;
	private static Map<String, Trilean> config;
	private static final Set<String> failures = Sets.newHashSet();
	private static final Set<String> failuresReadOnly = Collections.unmodifiableSet(failures);
	private static final SetMultimap<String, String> configKeysForDiscoveredClasses = HashMultimap.create();
	private static boolean analyticsSafe = false;
	
	public static void submitConfigAnalytics() {
		Analytics.submitConfig();
		analyticsSafe = true;
	}
	
	public static String remap(String configKey) {
		return starMap.getOrDefault(configKey, configKey);
	}
	
	public static Set<String> getConfigKeysForDiscoveredClass(String clazz) {
		return Collections.unmodifiableSet(configKeysForDiscoveredClasses.get(clazz.replace('/', '.')));
	}
	
	private static RuntimeException devError(String msg) {
		try {
			UIManager.setLookAndFeel(new MetalLookAndFeel());
			JFrame dummyFrame = new JFrame();
			dummyFrame.setIconImage(Toolkit.getDefaultToolkit().createImage(MixinConfigPlugin.class.getClassLoader().getResource("assets/fabrication/icon.png")));
			dummyFrame.setSize(1, 1);
			dummyFrame.setLocationRelativeTo(null);
			JOptionPane.showOptionDialog(dummyFrame, msg, "Fabrication Dev Error",
					JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE,
					null,
					new String[] {"Exit"}, "Exit");
			System.exit(1);
		} catch (Throwable t) {}
		return new RuntimeException(msg);
	}

	public static boolean isEnabled(String configKey) {
		if (isFailed(configKey)) return false;
		configKey = remap(configKey);
		if (!validKeys.contains(configKey)) {
			FabLog.error("Cannot look up value for config key "+configKey+" with no default");
			return false;
		}
		if (!config.containsKey(configKey))
			return defaults != null && defaults.get(configKey);
		return config.get(configKey).resolve(defaults == null ? false : defaults.get(configKey));
	}
	
	public static boolean isFailed(String configKey) {
		return failures.contains(remap(configKey));
	}
	
	public static Trilean getValue(String configKey) {
		if (isFailed(configKey)) return Trilean.FALSE;
		return config.getOrDefault(remap(configKey), Trilean.UNSET);
	}
	
	public static ResolvedTrilean getResolvedValue(String configKey) {
		if (isFailed(configKey)) return ResolvedTrilean.FALSE;
		return config.getOrDefault(remap(configKey), Trilean.UNSET).resolveSemantically(defaults != null && defaults.get(configKey));
	}
	
	public static boolean isTrilean(String s) {
		return !NON_TRILEANS.contains(s);
	}
	
	public static boolean isRuntimeConfigurable(String s) {
		return RUNTIME_CONFIGURABLE.contains(s);
	}
	
	public static String getRawValue(String configKey) {
		configKey = remap(remap(configKey));
		return rawConfig.get(configKey).orElse(configKey.equals("general.profile") ? "light" : "");
	}
	
	public static boolean isValid(String configKey) {
		return validKeys.contains(remap(configKey));
	}
	
	public static boolean getDefault(String configKey) {
		return defaults != null && defaults.get(remap(configKey));
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
	
	public static void addFailure(String configKey) {
		failures.add(remap(configKey));
	}
	
	public static void set(String configKey, String newValue) {
		if (isTrilean(configKey)) {
			switch (newValue) {
				case "true": case "false": case "unset":
					break;
				default: throw new IllegalArgumentException("Trilean key "+configKey+" cannot be set to "+newValue);
			}
		} else if ("general.profile".equals(configKey)) {
			if (!Enums.getIfPresent(Profile.class, newValue.toUpperCase(Locale.ROOT)).isPresent()) {
				throw new IllegalArgumentException("Cannot set profile to "+newValue);
			}
		}
		rawConfig.put(configKey, newValue);
		if ("general.profile".equals(configKey)) {
			profile = rawConfig.getEnum("general.profile", Profile.class).orElse(Profile.LIGHT);
			defaults = defaultsByProfile.get(profile);
		} else {
			config.put(configKey, Trilean.parseTrilean(newValue));
		}
		if ("general.data_upload".equals(configKey)) {
			if ("true".equals(newValue)) {
				Analytics.submit("enable_analytics");
				submitConfigAnalytics();
			} else {
				Analytics.deleteId();
			}
		}
		Path configFile = Agnos.getConfigDir().resolve("fabrication").resolve("features.ini");
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
			FabLog.info("Update of features.ini done in "+watch);
		} catch (IOException e) {
			FabLog.warn("Failed to update configuration file", e);
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
				rawConfig = QDIni.loadAndTransform(configFile, new IniTransformer() {
	
					final String NOTICES_HEADER = "; Notices: (Do not edit anything past this line; it will be overwritten)";
					
					Set<String> encounteredKeys = Sets.newHashSet();
					
					List<String> notices = Lists.newArrayList();
					boolean encounteredNotices = false;
					
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
					
				}, sw);
				profile = rawConfig.getEnum("general.profile", Profile.class).orElse(Profile.LIGHT);
				defaults = defaultsByProfile.get(profile);
				config = new HashMap<>();
				for (String k : rawConfig.keySet()) {
					if (isTrilean(k)) {
						try {
							config.put(k, rawConfig.getEnum(k, Trilean.class).get());
						} catch (BadValueException e) {
							FabLog.warn(e.getMessage()+" - assuming unset");
							config.put(k, Trilean.UNSET);
						}
					}
				}
			} catch (SyntaxErrorException e) {
				FabLog.warn("Failed to load configuration file: "+e.getMessage()+"; will assume defaults");
				config = Maps.transformValues(defaults, v -> Trilean.UNSET);
			} catch (IOException e) {
				FabLog.warn("Failed to load configuration file; will assume defaults", e);
				config = Maps.transformValues(defaults, v -> Trilean.UNSET);
			}
			try {
				Files.write(configFile, sw.toString().getBytes(Charsets.UTF_8));
			} catch (IOException e) {
				FabLog.warn("Failed to transform configuration file", e);
			}
		});
		if (analyticsSafe) {
			submitConfigAnalytics();
		}
		for (ConfigLoader ldr : loaders) {
			load(ldr);
		}
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
			if (Files.exists(configFileOld)) {
				try {
					QDIni currentValues = QDIni.load(configFileOld);
					try (InputStream is = MixinConfigPlugin.class.getClassLoader().getResourceAsStream(defaultName);
							OutputStreamWriter osw = new OutputStreamWriter(Files.newOutputStream(configFile), Charsets.UTF_8)) {
						QDIni.loadAndTransform(defaultName, new InputStreamReader(is, Charsets.UTF_8), new IniTransformer() {
	
							@Override
							public String transformLine(String path, String line) {
								return line;
							}
	
							@Override
							public String transformValueComment(String key, String value, String comment) {
								return comment;
							}
							
							@Override
							public String transformValue(String key, String value) {
								return currentValues.get(key).orElse(value);
							}
							
						}, osw);
					}
					Files.delete(configFileOld);
				} catch (IOException e) {
					throw new RuntimeException("Failed to upgrade config", e);
				}
			} else if (!migrated) {
				try {
					Resources.asByteSource(MixinConfigPlugin.class.getClassLoader().getResource(defaultName)).copyTo(MoreFiles.asByteSink(configFile));
				} catch (IOException e) {
					throw new RuntimeException("Failed to write default config", e);
				}
			}
		}
	}

	@Override
	public void onLoad(String mixinPackage) {
		reload();
		RUNTIME_CHECKS_WAS_ENABLED = isEnabled("general.runtime_checks");
		Mixins.registerErrorHandlerClass("com.unascribed.fabrication.support.MixinErrorHandler_THIS_ERROR_HANDLER_IS_FOR_SOFT_FAILURE_IN_FABRICATION_ITSELF_AND_DOES_NOT_IMPLY_FABRICATION_IS_RESPONSIBLE_FOR_THE_BELOW_ERROR");
		InjectionInfo.register(FailsoftCallbackInjectionInfo.class);
		InjectionInfo.register(FailsoftModifyArgInjectionInfo.class);
		InjectionInfo.register(FailsoftModifyArgsInjectionInfo.class);
		InjectionInfo.register(FailsoftRedirectInjectionInfo.class);
		InjectionInfo.register(FailsoftModifyVariableInjectionInfo.class);
		InjectionInfo.register(FailsoftModifyConstantInjectionInfo.class);
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
		
	}

	@Override
	public List<String> getMixins() {
		FabLog.debug("☕ Profile: "+profile.name().toLowerCase(Locale.ROOT));
		return discoverClassesInPackage("com.unascribed.fabrication.mixin", true);
	}

	public static List<String> discoverClassesInPackage(String pkg, boolean truncate) {
		FabLog.debug("Starting discovery pass...");
		try {
			int count = 0;
			int enabled = 0;
			int skipped = 0;
			List<String> rtrn = Lists.newArrayList();
			for (ClassInfo ci : getClassesInPackage(pkg)) {
				// we want nothing to do with inner classes and the like
				if (ci.getName().contains("$")) continue;
				count++;
				String truncName = ci.getName().substring(pkg.length()+1);
				FabLog.debug("--");
				FabLog.debug((Math.random() < 0.01 ? "👅" : "👀")+" Considering "+truncName);
				ClassReader cr = new ClassReader(ci.asByteSource().read());
				ClassNode cn = new ClassNode();
				cr.accept(cn, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
				boolean eligible = true;
				List<String> eligibilityFailures = Lists.newArrayList();
				List<String> eligibilityNotes = Lists.newArrayList();
				List<String> eligibilitySuccesses = Lists.newArrayList();
				boolean anyRestrictions = false;
				if (cn.visibleAnnotations != null) {
					for (AnnotationNode an : cn.visibleAnnotations) {
						if (an.desc.equals("Lcom/unascribed/fabrication/support/EligibleIf;")) {
							if (an.values == null) continue;
							for (int i = 0; i < an.values.size(); i += 2) {
								anyRestrictions = true;
								String k = (String)an.values.get(i);
								Object v = an.values.get(i+1);
								if (k.equals("configEnabled")) {
									if (!defaults.containsKey(remap((String)v))) {
										FabLog.debug("🙈 Dev error! Exploding.");
										throw devError(cn.name.substring(pkg.length()+1).replace('/', '.')+" references an unknown config key "+v+"\n\nDid you forget to add it to features.txt and run build-features.sh?");
									}
									if (getValue((String)v) == Trilean.UNSET && MixinConfigPlugin.RUNTIME_CHECKS_WAS_ENABLED) {
										eligibilitySuccesses.add("Runtime checks is enabled and required config key "+remap((String)v)+" is unset");
									} else if (!isEnabled((String)v)) {
										eligibilityFailures.add("Required config setting "+remap((String)v)+" is disabled "+(config.get(remap((String)v)) == Trilean.FALSE ? "explicitly" : "by profile"));
										eligible = false;
									} else {
										eligibilitySuccesses.add("Required config setting "+remap((String)v)+" is enabled "+(config.get(remap((String)v)) == Trilean.TRUE ? "explicitly" : "by profile"));
									}
									configKeysForDiscoveredClasses.put(ci.getName(), (String)v);
								} else if (k.equals("configDisabled")) {
									if (getValue((String)v) == Trilean.UNSET && MixinConfigPlugin.RUNTIME_CHECKS_WAS_ENABLED) {
										eligibilitySuccesses.add("Runtime checks is enabled and conflicting config key "+remap((String)v)+" is unset");
									} else if (!isEnabled((String)v)) {
										eligibilitySuccesses.add("Conflicting config setting "+remap((String)v)+" is disabled "+(config.get(remap((String)v)) == Trilean.FALSE ? "explicitly" : "by profile"));
									} else {
										eligibilityFailures.add("Conflicting config setting "+remap((String)v)+" is enabled "+(config.get(remap((String)v)) == Trilean.TRUE ? "explicitly" : "by profile"));
										eligible = false;
									}
								} else if (k.equals("envMatches")) {
									String[] arr = (String[])v;
									if (arr[0].equals("Lcom/unascribed/fabrication/support/Env;")) {
										Env e = Env.valueOf(arr[1]);
										Env curEnv = Agnos.getCurrentEnv();
										if (!curEnv.matches(e)) {
											eligibilityFailures.add("Environment is incorrect (want "+e.name().toLowerCase(Locale.ROOT)+", got "+curEnv.name().toLowerCase(Locale.ROOT)+")");
											eligible = false;
										} else {
											eligibilitySuccesses.add("Environment is correct ("+e.name().toLowerCase(Locale.ROOT)+")");
										}
									}
								} else if (k.equals("modLoaded")) {
									for (String s : (List<String>)v) {
										if (!Agnos.isModLoaded(s)) {
											eligibilityFailures.add("Required mod "+s+" is not loaded");
											eligible = false;
										} else {
											eligibilitySuccesses.add("Required mod "+s+" is loaded");
										}
									}
								} else if (k.equals("modNotLoaded")) {
									for (String s : (List<String>)v) {
										if (Agnos.isModLoaded(s)) {
											eligibilityFailures.add("Conflicting mod "+s+" is loaded");
											eligible = false;
										} else {
											eligibilitySuccesses.add("Conflicting mod "+s+" is not loaded");
										}
									}
								} else if (k.equals("classPresent")) {
									for (String s : (List<String>)v) {
										try {
											Class.forName(s, false, MixinConfigPlugin.class.getClassLoader());
											eligibilitySuccesses.add("Required class "+s+" is present");
										} catch (ClassNotFoundException e) {
											eligibilityFailures.add("Required class "+s+" is not present");
											eligible = false;
										}
									}
								} else if (k.equals("classNotPresent")) {
									for (String s : (List<String>)v) {
										try {
											Class.forName(s, false, MixinConfigPlugin.class.getClassLoader());
											eligibilityFailures.add("Conflicting class "+s+" is present");
											eligible = false;
										} catch (ClassNotFoundException e) {
											eligibilitySuccesses.add("Conflicting class "+s+" is not present");
										}
									}
								} else if (k.equals("anyConfigEnabled")) {
									boolean allDisabled = true;
									boolean foundAny = false;
									for (String s : (List<String>)v) {
										s = remap(s);
										if (isEnabled(s)) {
											foundAny = true;
											allDisabled = false;
											eligibilitySuccesses.add("Relevant config setting "+s+" is enabled "+(config.get(s) == Trilean.TRUE ? "explicitly" : "by profile"));
										} else {
											if (getValue(s) != Trilean.FALSE) {
												allDisabled = false;
											}
											eligibilityNotes.add("Relevant config setting "+s+" is disabled "+(config.get(s) == Trilean.FALSE ? "explicitly" : "by profile"));
										}
										configKeysForDiscoveredClasses.put(ci.getName(), s);
									}
									if (allDisabled) {
										eligibilityFailures.add("All of the relevant config settings are explicitly disabled");
										eligible = false;
									} else if (!foundAny) {
										if (MixinConfigPlugin.RUNTIME_CHECKS_WAS_ENABLED) {
											eligibilityNotes.add("None of the relevant config settings are enabled, but runtime checks is enabled");
										} else {
											eligibilityFailures.add("None of the relevant config settings are enabled");
											eligible = false;
										}
									}
								} else if (k.equals("specialConditions")) {
									List<String[]> li = (List<String[]>)v;
									if (li.isEmpty()) {
										eligibilityNotes.add("Special conditions is present but empty - ignoring");
									} else {
										for (String[] e : li) {
											if (!"Lcom/unascribed/fabrication/support/SpecialEligibility;".equals(e[0])) {
												eligibilityNotes.add("Unknown special condition type "+e[0]+" - ignoring");
											} else {
												try {
													SpecialEligibility se = SpecialEligibility.valueOf(e[1]);
													if (MixinConfigPlugin.RUNTIME_CHECKS_WAS_ENABLED && se.ignorableWithRuntimeChecks) {
														eligibilityNotes.add("Runtime checks is enabled, ignoring special condition "+se);
													} else if (isMet(se)) {
														eligibilitySuccesses.add("Special condition "+se+" is met");
													} else {
														eligibilityFailures.add("Special condition "+se+" is not met");
														eligible = false;
													}
												} catch (IllegalArgumentException ex) {
													eligibilityFailures.add("Unknown special condition "+e[1]);
													eligible = false;
												}
											}
										}
									}
								} else {
									FabLog.warn("Unknown annotation setting "+k);
								}
							}
						}
					}
				}
				if (!anyRestrictions) {
					eligibilityNotes.add("No restrictions on eligibility");
				}
				for (String s : eligibilityNotes) {
					FabLog.debug("  ℹ️ "+s);
				}
				for (String s : eligibilitySuccesses) {
					FabLog.debug("  ✅ "+s);
				}
				for (String s : eligibilityFailures) {
					FabLog.debug("  ❌ "+s);
				}
				if (eligible) {
					enabled++;
					FabLog.debug("👍 Eligibility requirements met. Applying "+truncName);
					rtrn.add(truncate ? truncName : ci.getName());
				} else {
					skipped++;
					FabLog.debug("✋ Eligibility requirements not met. Skipping "+truncName);
				}
			}
			FabLog.debug("Discovery pass complete. Found "+count+" candidates, enabled "+enabled+", skipped "+skipped+".");
			return rtrn;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Iterable<ClassInfo> getClassesInPackage(String pkg) {
		try (InputStream is = MixinConfigPlugin.class.getClassLoader().getResourceAsStream("classes.txt")) {
			if (is != null) {
				Constructor<ClassInfo> cons = ClassInfo.class.getDeclaredConstructor(String.class, ClassLoader.class);
				cons.setAccessible(true);
				List<ClassInfo> rtrn = Lists.newArrayList();
				BufferedReader br = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
				String prefix = pkg.replace('.', '/')+"/";
				while (true) {
					String line = br.readLine();
					if (line == null) break;
					if (line.startsWith(prefix)) {
						rtrn.add(cons.newInstance(line, MixinConfigPlugin.class.getClassLoader()));
					}
				}
				return rtrn;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			return ClassPath.from(MixinConfigPlugin.class.getClassLoader()).getTopLevelClassesRecursive(pkg);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	public static boolean RUNTIME_CHECKS_WAS_ENABLED;
	
}
