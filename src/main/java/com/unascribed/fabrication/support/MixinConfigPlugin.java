package com.unascribed.fabrication.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import com.unascribed.fabrication.QDIni;
import com.unascribed.fabrication.QDIni.IniTransformer;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.MoreFiles;
import com.google.common.io.Resources;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public class MixinConfigPlugin implements IMixinConfigPlugin {

	private static final Logger log = LogManager.getLogger("Fabrication");
	
	private static final ImmutableSet<String> VIENNA_EXCEPTIONS = ImmutableSet.of(
			"balance.infinity_mending"
	);
	private static final ImmutableSet<String> NON_TRILEANS = ImmutableSet.of(
			"general.profile"
	);
	private static final ImmutableSet<String> RUNTIME_CONFIGURABLE = ImmutableSet.of(
			"minor_mechanics.feather_falling_five_damages_boots"
	);
	
	
	public enum Profile {
		GREEN(),
		BLONDE("fixes", "utility"),
		LIGHT("fixes", "utility", "tweaks"),
		MEDIUM("fixes", "utility", "tweaks", "minor_mechanics"),
		DARK("fixes", "utility", "tweaks", "minor_mechanics", "mechanics"),
		VIENNA("fixes", "utility", "tweaks", "minor_mechanics", "mechanics", "balance", "weird_tweaks"),
		BURNT("*")
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
	
	public static final class RuntimeChecks {
		public static final boolean ENABLED = MixinConfigPlugin.isEnabled("general.runtime_checks");
		public static boolean check(String cfg) {
			return !ENABLED || MixinConfigPlugin.isEnabled(cfg);
		}
	}
	
	private static final ImmutableMap<Profile, ImmutableMap<String, Boolean>> defaultsByProfile;
	private static final ImmutableSet<String> validKeys;
	private static final ImmutableMap<String, String> starMap;
	private static ImmutableMap<String, Boolean> defaults;
	static {
		try (InputStream is = MixinConfigPlugin.class.getClassLoader().getResourceAsStream("default_config.ini")) {
			Set<String> keys = QDIni.load(is).keySet();
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
					boolean enabled = p.sections.contains("*") || p.sections.contains(section)
							&& (p != Profile.VIENNA || !VIENNA_EXCEPTIONS.contains(key));
					defaultsBuilder.put(key, enabled);
				}
				profilesBuilder.put(p, defaultsBuilder.build());
			}
			validKeys = ImmutableSet.copyOf(keys);
			defaultsByProfile = profilesBuilder.build();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static Profile profile;
	private static Map<String, String> rawConfig;
	private static Map<String, Trilean> config;
	
	private static String remap(String configKey) {
		return starMap.getOrDefault(configKey, configKey);
	}
	
	public static boolean isEnabled(String configKey) {
		configKey = remap(configKey);
		if (!validKeys.contains(configKey)) throw new IllegalArgumentException("Cannot look up status of config key "+configKey+" with no default");
		if (!config.containsKey(configKey))
			return defaults == null ? false : defaults.get(configKey);
		return config.get(configKey).resolve(defaults == null ? false : defaults.get(configKey));
	}
	
	public static Trilean getValue(String configKey) {
		return config.getOrDefault(remap(configKey), Trilean.UNSET);
	}
	
	public static boolean isTrilean(String s) {
		return !NON_TRILEANS.contains(s);
	}
	
	public static boolean isRuntimeConfigurable(String s) {
		return RUNTIME_CONFIGURABLE.contains(s);
	}
	
	public static String getRawValue(String configKey) {
		configKey = remap(remap(configKey));
		return rawConfig.getOrDefault(configKey, configKey.equals("general.profile") ? "light" : "");
	}
	
	public static boolean isValid(String configKey) {
		return validKeys.contains(remap(configKey));
	}
	
	public static boolean getDefault(String configKey) {
		return defaults == null ? false : defaults.get(remap(configKey));
	}
	
	public static ImmutableSet<String> getAllKeys() {
		return validKeys;
	}
	
	public static void set(String configKey, String newValue) {
		rawConfig.put(configKey, newValue);
		Path configFile = FabricLoader.getInstance().getConfigDir().resolve("fabrication.ini");
		StringWriter sw = new StringWriter();
		try (InputStream is = Files.newInputStream(configFile)) {
			 QDIni.loadAndTransform(new InputStreamReader(is, Charsets.UTF_8), new IniTransformer() {
				 
				boolean found = false;
				
				@Override
				public String transformLine(String path, String line) {
					if (line == null || line.equals("; Notices: (Do not edit anything past this line; it will be overwritten)")) {
						if (!found) {
							found = true;
							return "; Added by /fabrication config as a last resort as this key could\r\n"
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
		} catch (IOException e) {
			log.warn("Failed to update configuration file", e);
		}
		try {
			Files.write(configFile, sw.toString().getBytes(Charsets.UTF_8));
		} catch (IOException e) {
			log.warn("Failed to update configuration file", e);
		}
	}
	
	public static void reload() {
		Path configFile = FabricLoader.getInstance().getConfigDir().resolve("fabrication.ini");
		if (!Files.exists(configFile)) {
			Path configFileOld = FabricLoader.getInstance().getConfigDir().resolve("fabrication.ini.old");
			if (Files.exists(configFileOld)) {
				try {
					Map<String, String> currentValues;
					try (InputStream is = Files.newInputStream(configFileOld)) {
						currentValues = QDIni.load(is);
					}
					try (InputStream is = MixinConfigPlugin.class.getClassLoader().getResourceAsStream("default_config.ini");
							OutputStreamWriter osw = new OutputStreamWriter(Files.newOutputStream(configFile), Charsets.UTF_8)) {
						QDIni.loadAndTransform(new InputStreamReader(is, Charsets.UTF_8), new IniTransformer() {
	
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
								return currentValues.getOrDefault(key, value);
							}
							
						}, osw);
					}
					Files.delete(configFileOld);
				} catch (IOException e) {
					throw new RuntimeException("Failed to upgrade config", e);
				}
			} else {
				try {
					Resources.asByteSource(MixinConfigPlugin.class.getClassLoader().getResource("default_config.ini")).copyTo(MoreFiles.asByteSink(configFile));
				} catch (IOException e) {
					throw new RuntimeException("Failed to write default config", e);
				}
			}
		}
		StringWriter sw = new StringWriter();
		try (InputStream is = Files.newInputStream(configFile)) {
			rawConfig = QDIni.loadAndTransform(new InputStreamReader(is, Charsets.UTF_8), new IniTransformer() {

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
			profile = Profile.valueOf(rawConfig.getOrDefault("general.profile", "light").toUpperCase(Locale.ROOT));
			defaults = defaultsByProfile.get(profile);
			config = Maps.transformValues(Maps.filterKeys(rawConfig, MixinConfigPlugin::isTrilean), Trilean::parseTrilean);
		} catch (IOException e) {
			log.warn("Failed to load configuration file; will assume defaults", e);
			config = Maps.transformValues(defaults, v -> Trilean.UNSET);
		}
		try {
			Files.write(configFile, sw.toString().getBytes(Charsets.UTF_8));
		} catch (IOException e) {
			log.warn("Failed to transform configuration file", e);
		}
	}
	
	@Override
	public void onLoad(String mixinPackage) {
		reload();
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
		log.info("☕ Profile: "+profile.name().toLowerCase(Locale.ROOT));
		return discoverClassesInPackage("com.unascribed.fabrication.mixin", true);
	}

	public static List<String> discoverClassesInPackage(String pkg, boolean truncate) {
		log.info("Starting discovery pass...");
		try {
			int count = 0;
			int enabled = 0;
			int skipped = 0;
			List<String> rtrn = Lists.newArrayList();
			for (ClassInfo ci : ClassPath.from(MixinConfigPlugin.class.getClassLoader()).getTopLevelClassesRecursive(pkg)) {
				count++;
				String truncName = ci.getName().substring(pkg.length()+1);
				log.info("--");
				log.info((Math.random() < 0.01 ? "👅" : "👀")+" Considering "+truncName);
				ClassReader cr = new ClassReader(ci.asByteSource().read());
				ClassNode cn = new ClassNode();
				cr.accept(cn, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
				boolean eligible = true;
				List<String> eligibilityFailures = Lists.newArrayList();
				List<String> eligibilityNotes = Lists.newArrayList();
				List<String> eligibilitySuccesses = Lists.newArrayList();
				boolean anyRestrictions = false;
				if (cn.visibleAnnotations != null) {
					out: for (AnnotationNode an : cn.visibleAnnotations) {
						if (an.desc.equals("Lcom/unascribed/fabrication/support/EligibleIf;")) {
							if (an.values == null) continue;
							for (int i = 0; i < an.values.size(); i += 2) {
								anyRestrictions = true;
								String k = (String)an.values.get(i);
								Object v = an.values.get(i+1);
								if (k.equals("configEnabled")) {
									if (RuntimeChecks.ENABLED) {
										eligibilityNotes.add("Runtime checks is enabled, ignoring required config key "+remap((String)v));
									} else if (!isEnabled((String)v)) {
										eligibilityFailures.add("Required config setting "+remap((String)v)+" is disabled "+(config.get(v) == Trilean.FALSE ? "explicitly" : "by profile"));
										eligible = false;
									} else {
										eligibilitySuccesses.add("Required config setting "+remap((String)v)+" is enabled "+(config.get(v) == Trilean.TRUE ? "explicitly" : "by profile"));
									}
								} else if (k.equals("configDisabled")) {
									if (RuntimeChecks.ENABLED) {
										eligibilityNotes.add("Runtime checks is enabled, ignoring required config key "+remap((String)v));
									} else if (!isEnabled((String)v)) {
										eligibilitySuccesses.add("Conflicting config setting "+remap((String)v)+" is disabled "+(config.get(v) == Trilean.FALSE ? "explicitly" : "by profile"));
									} else {
										eligibilityFailures.add("Conflicting config setting "+remap((String)v)+" is enabled "+(config.get(v) == Trilean.TRUE ? "explicitly" : "by profile"));
										eligible = false;
									}
								} else if (k.equals("envMatches")) {
									String[] arr = (String[])v;
									if (arr[0].equals("Lcom/unascribed/fabrication/support/Env;")) {
										Env e = Env.valueOf(arr[1]);
										EnvType et = e.fabric;
										EnvType curEnv = FabricLoader.getInstance().getEnvironmentType();
										if (et != null && et != curEnv) {
											eligibilityFailures.add("Environment is incorrect (want "+e.name().toLowerCase(Locale.ROOT)+", got "+curEnv.name().toLowerCase(Locale.ROOT)+")");
											eligible = false;
										} else {
											eligibilitySuccesses.add("Environment is correct ("+e.name().toLowerCase(Locale.ROOT)+")");
										}
									}
								} else if (k.equals("modLoaded")) {
									for (String s : (List<String>)v) {
										if (!FabricLoader.getInstance().isModLoaded(s)) {
											eligibilityFailures.add("Required mod "+s+" is not loaded");
											eligible = false;
										} else {
											eligibilitySuccesses.add("Required mod "+s+" is loaded");
										}
									}
								} else if (k.equals("modNotLoaded")) {
									for (String s : (List<String>)v) {
										if (FabricLoader.getInstance().isModLoaded(s)) {
											eligibilityFailures.add("Conflicting mod "+s+" is loaded");
											eligible = false;
										} else {
											eligibilitySuccesses.add("Conflicting mod "+s+" is not loaded");
										}
									}
								} else if (k.equals("classPresent")) {
									for (String s : (List<String>)v) {
										try {
											Class.forName(s);
											eligibilitySuccesses.add("Required class "+s+" is present");
										} catch (ClassNotFoundException e) {
											eligibilityFailures.add("Required class "+s+" is not present");
											eligible = false;
										}
									}
								} else if (k.equals("classNotPresent")) {
									for (String s : (List<String>)v) {
										try {
											Class.forName(s);
											eligibilityFailures.add("Conflicting class "+s+" is present");
											eligible = false;
										} catch (ClassNotFoundException e) {
											eligibilitySuccesses.add("Conflicting class "+s+" is not present");
										}
									}
								} else if (k.equals("anyConfigEnabled")) {
									if (RuntimeChecks.ENABLED) {
										eligibilityNotes.add("Runtime checks is enabled, ignoring desired config keys");
									} else {
										boolean foundAny = false;
										for (String s : (List<String>)v) {
											if (isEnabled(s)) {
												foundAny = true;
												eligibilitySuccesses.add("Relevant config setting "+remap(s)+" is enabled "+(config.get(s) == Trilean.TRUE ? "explicitly" : "by profile"));
											} else {
												eligibilityNotes.add("Relevant config setting "+remap(s)+" is disabled "+(config.get(s) == Trilean.FALSE ? "explicitly" : "by profile"));
											}
										}
										if (!foundAny) {
											eligibilitySuccesses.add("None of the relevant config settings are enabled");
											eligible = false;
										}
									}
								} else {
									log.warn("Unknown annotation setting "+k);
								}
							}
						}
					}
				}
				if (!anyRestrictions) {
					eligibilityNotes.add("No restrictions on eligibility");
				}
				for (String s : eligibilityNotes) {
					log.info("  ℹ️ "+s);
				}
				for (String s : eligibilitySuccesses) {
					log.info("  ✅ "+s);
				}
				for (String s : eligibilityFailures) {
					log.info("  ❌ "+s);
				}
				if (eligible) {
					enabled++;
					log.info("👍 Eligibility requirements met. Applying "+truncName);
					rtrn.add(truncate ? truncName : ci.getName());
				} else {
					skipped++;
					log.info("✋ Eligibility requirements not met. Skipping "+truncName);
				}
			}
			log.info("Discovery pass complete. Found "+count+" candidates, enabled "+enabled+", skipped "+skipped+".");
			return rtrn;
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

}
