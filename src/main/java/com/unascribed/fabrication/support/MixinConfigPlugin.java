package com.unascribed.fabrication.support;

import java.io.IOException;
import java.io.InputStream;
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
import org.jline.utils.InputStreamReader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import com.unascribed.fabrication.QDIni;
import com.unascribed.fabrication.QDIni.IniTransformer;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
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
	
	private static final Map<String, Boolean> defaults;
	static {
		try (InputStream is = MixinConfigPlugin.class.getClassLoader().getResourceAsStream("defaults.ini")) {
			defaults = Maps.transformValues(QDIni.load(is), Boolean::parseBoolean);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static Map<String, Trilean> config;
	
	public static boolean isEnabled(String configKey) {
		if (!defaults.containsKey(configKey)) throw new IllegalArgumentException("Cannot look up status of config key "+configKey+" with no default");
		if (!config.containsKey(configKey))
			return defaults.get(configKey);
		return config.get(configKey).resolve(defaults.get(configKey));
	}
	
	@Override
	public void onLoad(String mixinPackage) {
		Path configFile = FabricLoader.getInstance().getConfigDir().resolve("fabrication.ini");
		if (!Files.exists(configFile)) {
			Path configFileOld = FabricLoader.getInstance().getConfigDir().resolve("fabrication.ini.old");
			if (Files.exists(configFileOld)) {
				try {
					Map<String, String> currentValues;
					try (InputStream is = Files.newInputStream(configFileOld)) {
						currentValues = QDIni.load(is);
					}
					try (InputStream is = getClass().getClassLoader().getResourceAsStream("default_config.ini");
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
					Resources.asByteSource(getClass().getClassLoader().getResource("default_config.ini")).copyTo(MoreFiles.asByteSink(configFile));
				} catch (IOException e) {
					throw new RuntimeException("Failed to write default config", e);
				}
			}
		}
		StringWriter sw = new StringWriter();
		try (InputStream is = Files.newInputStream(configFile)) {
			config = Maps.transformValues(QDIni.loadAndTransform(new InputStreamReader(is, Charsets.UTF_8), new IniTransformer() {

				final String NOTICES_HEADER = "; Notices: (Do not edit anything past this line; it will be overwritten)";
				
				Set<String> encounteredKeys = Sets.newHashSet();
				boolean encounteredUnset = false;
				boolean encounteredTrue = false;
				
				List<String> notices = Lists.newArrayList();
				boolean encounteredNotices = false;
				
				@Override
				public String transformLine(String path, String line) {
					if ((!encounteredNotices && line == null) || (line != null && line.trim().equals(NOTICES_HEADER))) {
						encounteredNotices = true;
						if (!encounteredUnset && !encounteredTrue) {
							notices.add("All options are set to false; why even have the mod installed?");
						}
						for (Map.Entry<String, Boolean> en : defaults.entrySet()) {
							String s = en.getKey();
							if (!encounteredKeys.contains(s)) {
								notices.add(s+" was not found; it defaulted to "+en.getValue());
							}
						}
						for (String s : encounteredKeys) {
							if (!defaults.containsKey(s)) {
								notices.add(s+" is not recognized");
							}
						}
						if (notices.isEmpty()) {
							return NOTICES_HEADER+"\r\n; - No notices. You're in the clear!";
						}
						return NOTICES_HEADER+"\r\n; - "+Joiner.on("\r\n; - ").join(notices);
					}
					return encounteredNotices ? null : line;
				}

				@Override
				public String transformValueComment(String key, String value, String comment) {
					return defaults.containsKey(key) ? "default: "+defaults.get(key) : comment;
				}
				
				@Override
				public String transformValue(String key, String value) {
					encounteredKeys.add(key);
					if (value.toUpperCase(Locale.ROOT).equals("UNSET")) {
						encounteredUnset = true;
					}
					if (value.toUpperCase(Locale.ROOT).equals("TRUE")) {
						encounteredTrue = true;
					}
					return value;
				}
				
			}, sw), Trilean::parseTrilean);
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
		return discoverClassesInPackage("com.unascribed.fabrication.mixin", true);
	}

	public static List<String> discoverClassesInPackage(String pkg, boolean truncate) {
		try {
			List<String> rtrn = Lists.newArrayList();
			for (ClassInfo ci : ClassPath.from(MixinConfigPlugin.class.getClassLoader()).getTopLevelClassesRecursive(pkg)) {
				String truncName = ci.getName().substring(pkg.length()+1);
				log.debug("--");
				log.debug("Considering "+truncName);
				ClassReader cr = new ClassReader(ci.asByteSource().read());
				ClassNode cn = new ClassNode();
				cr.accept(cn, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
				boolean eligible = true;
				boolean anyRestrictions = false;
				if (cn.visibleAnnotations != null) {
					out: for (AnnotationNode an : cn.visibleAnnotations) {
						if (an.desc.equals("Lcom/unascribed/fabrication/support/OnlyIf;")) {
							if (an.values == null) continue;
							for (int i = 0; i < an.values.size(); i += 2) {
								anyRestrictions = true;
								String k = (String)an.values.get(i);
								Object v = an.values.get(i+1);
								if (k.equals("config")) {
									if (!isEnabled((String)v)) {
										log.debug("  ❌ Required config setting "+v+" is disabled "+(config.get(k) == Trilean.FALSE ? "explicitly" : "by default"));
										eligible = false;
									} else {
										log.debug("  ✅ Required config setting "+v+" is enabled "+(config.get(k) == Trilean.TRUE ? "explicitly" : "by default"));
									}
								} else if (k.equals("env")) {
									String[] arr = (String[])v;
									if (arr[0].equals("Lcom/unascribed/fabrication/support/Env;")) {
										Env e = Env.valueOf(arr[1]);
										EnvType et = e.fabric;
										EnvType curEnv = FabricLoader.getInstance().getEnvironmentType();
										if (et != null && et != curEnv) {
											log.debug("  ❌ Environment is incorrect (want "+e.name().toLowerCase(Locale.ROOT)+", got "+curEnv.name().toLowerCase(Locale.ROOT)+")");
											eligible = false;
										} else {
											log.debug("  ✅ Environment is correct ("+e.name().toLowerCase(Locale.ROOT)+")");
										}
									}
								} else if (k.equals("dependencies")) {
									for (String s : (List<String>)v) {
										if (!FabricLoader.getInstance().isModLoaded(s)) {
											log.debug("  ❌ Required mod "+s+" is not loaded");
											eligible = false;
										} else {
											log.debug("  ✅ Required mod "+s+" is loaded");
										}
									}
								} else {
									log.warn("Unknown annotation setting "+k);
								}
							}
						}
					}
				}
				if (eligible) {
					if (!anyRestrictions) {
						log.debug("  ⭕​ No restrictions on eligibility");
					}
					log.debug("Eligibility requirements met. Injecting "+truncName);
					rtrn.add(truncate ? truncName : ci.getName());
				} else {
					log.debug("Eligibility requirements not met. Skipping "+truncName);
				}
			}
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
