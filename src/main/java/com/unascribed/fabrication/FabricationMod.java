package com.unascribed.fabrication;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.unascribed.fabrication.support.Feature;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.fabricmc.api.ModInitializer;

public class FabricationMod implements ModInitializer {
	
	private static final Map<String, Feature> features = Maps.newHashMap();
	private static final List<Feature> unconfigurableFeatures = Lists.newArrayList();
	private static final Set<String> enabledFeatures = Sets.newHashSet();
	
	@Override
	public void onInitialize() {
		for (String s : MixinConfigPlugin.discoverClassesInPackage("com.unascribed.fabrication.features", false)) {
			try {
				Feature r = (Feature)Class.forName(s).newInstance();
				if (r.getConfigKey() == null || RuntimeChecks.check(r.getConfigKey())) {
					r.apply();
					if (r.getConfigKey() != null) {
						enabledFeatures.add(r.getConfigKey());
					}
				}
				if (r.getConfigKey() != null) {
					features.put(r.getConfigKey(), r);
				} else {
					unconfigurableFeatures.add(r);
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed to initialize feature "+s, e);
			}
		}
	}
	
	public static boolean isAvailableFeature(String configKey) {
		return features.containsKey(configKey);
	}
	
	public static boolean updateFeature(String configKey) {
		boolean enabled = MixinConfigPlugin.isEnabled(configKey);
		if (enabledFeatures.contains(configKey) == enabled) return true;
		if (enabled) {
			features.get(configKey).apply();
			enabledFeatures.add(configKey);
			return true;
		} else {
			boolean b = features.get(configKey).undo();
			if (b) {
				enabledFeatures.remove(configKey);
			}
			return b;
		}
	}
	
	public static <T> T snag(Class<?> clazz, Object inst, String intermediateName, String yarnName) {
		try {
			Field f;
			try {
				f = clazz.getDeclaredField(intermediateName);
			} catch (NoSuchFieldException e) {
				f = clazz.getDeclaredField(yarnName);
			}
			f.setAccessible(true);
			return (T)f.get(inst);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T twiddle(Class<?> clazz, Object inst, String intermediateName, String yarnName, Class<?>[] argTypes, Object... args) {
		try {
			Method m;
			try {
				m = clazz.getDeclaredMethod(intermediateName, argTypes);
			} catch (NoSuchMethodException e) {
				m = clazz.getDeclaredMethod(yarnName, argTypes);
			}
			m.setAccessible(true);
			return (T)m.invoke(inst, args);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
}
