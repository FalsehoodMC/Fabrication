package com.unascribed.fabrication.loaders;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.unascribed.fabrication.QDIni;
import com.unascribed.fabrication.support.ConfigLoader;
import com.unascribed.fabrication.support.ConfigValues;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

@EligibleIf(envMatches=Env.CLIENT)
public class LoaderClassicBlockDrops implements ConfigLoader {

	public static final List<Function<Identifier, ConfigValues.Feature>> rules = Lists.newArrayList();
	public static final Map<String, Optional<Boolean>> literals = new HashMap<>();
	public static final Map<String, Optional<Boolean>> heuristics = new HashMap<>();
	private static final Map<Block, Boolean> cache = new WeakHashMap<>();
	public static final LoaderClassicBlockDrops instance = new LoaderClassicBlockDrops();

	public static boolean isSafe(Block b) {
		if (cache.containsKey(b)) return cache.get(b);
		Identifier id = Registries.BLOCK.getId(b);
		if (id == null) return false;
		for (Function<Identifier, ConfigValues.Feature> rule : rules) {
			ConfigValues.Feature t = rule.apply(id);
			if (t != ConfigValues.Feature.UNSET) {
				boolean r = t.resolve(true);
				cache.put(b, r);
				return r;
			}
		}
		cache.put(b, false);
		return false;
	}

	public void reload() {
		rules.clear();
		cache.clear();
		Map<String, Boolean> validLiterals = new HashMap<>();
		for (Map.Entry<String, Optional<Boolean>> entry : literals.entrySet()) {
			if (entry.getValue().isPresent()) {
				validLiterals.put(entry.getKey(), entry.getValue().get());
			}
		}
		rules.add(id -> {
			Boolean k = validLiterals.get(id.toString());
			if (k == null) return ConfigValues.Feature.UNSET;
			return k ? ConfigValues.Feature.TRUE : ConfigValues.Feature.FALSE;
		});
		for (Map.Entry<String, Optional<Boolean>> entry : heuristics.entrySet()) {
			String k = entry.getKey();
			if (k.contains("\\E") || k.contains("\\Q"))
				throw new IllegalArgumentException("No.");
			StringBuffer buf = new StringBuffer("^\\Q");
			Matcher m = Pattern.compile("*", Pattern.LITERAL).matcher(k);
			while (m.find()) {
				m.appendReplacement(buf, "\\\\E.*\\\\Q");
			}
			m.appendTail(buf);
			buf.append("\\E$");
			Pattern p = Pattern.compile(buf.toString());
			Optional<Boolean> valueOpt = entry.getValue();
			if (valueOpt.isPresent()) {
				boolean value = valueOpt.get();
				rules.add(id -> {
					if (p.matcher(id.getPath()).matches()) return value ? ConfigValues.Feature.TRUE : ConfigValues.Feature.FALSE;
					return ConfigValues.Feature.UNSET;
				});
			}
		}
	}

	@Override
	public void load(Path configDir, QDIni config, boolean loadError) {
		heuristics.clear();
		literals.clear();
		for (String k : config.keySet()) {
			if (k.startsWith("@heuristics.")) {
				heuristics.put(k.substring(12), config.getBoolean(k));
			} else {
				literals.put(k, config.getBoolean(k));
			}
		}
		reload();
	}

	@Override
	public String getConfigName() {
		return "classic_block_drops";
	}

}
