package com.unascribed.fabrication.loaders;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.unascribed.fabrication.support.ConfigLoader;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.Trilean;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@EligibleIf(envMatches=Env.CLIENT)
public class LoaderClassicBlockDrops implements ConfigLoader {

	public static final List<Function<Identifier, Trilean>> rules = Lists.newArrayList();
	private static final Map<Block, Boolean> cache = new WeakHashMap<>();
	
	public static boolean isSafe(Block b) {
		if (cache.containsKey(b)) return cache.get(b);
		Identifier id = Registry.BLOCK.getId(b);
		if (id == null) return false;
		for (Function<Identifier, Trilean> rule : rules) {
			Trilean t = rule.apply(id);
			if (t != Trilean.UNSET) {
				boolean r = t.resolve(true);
				cache.put(b, r);
				return r;
			}
		}
		cache.put(b, false);
		return false;
	}
	
	@Override
	public void load(Path configDir, Map<String, String> config) {
		rules.clear();
		cache.clear();
		for (Map.Entry<String, String> en : config.entrySet()) {
			if (en.getKey().startsWith("@heuristics.")) {
				if (en.getKey().contains("\\E") || en.getKey().contains("\\Q"))
					throw new IllegalArgumentException("No.");
				StringBuffer buf = new StringBuffer("^\\Q");
				Matcher m = Pattern.compile("*", Pattern.LITERAL).matcher(en.getKey().substring(12));
				while (m.find()) {
					m.appendReplacement(buf, "\\\\E.*\\\\Q");
				}
				m.appendTail(buf);
				buf.append("\\E$");
				Pattern p = Pattern.compile(buf.toString());
				boolean value = Boolean.parseBoolean(en.getValue());
				rules.add(id -> {
					if (p.matcher(id.getPath()).matches()) return value ? Trilean.TRUE : Trilean.FALSE;
					return Trilean.UNSET;
				});
			} else {
				String k = en.getKey();
				boolean value = Boolean.parseBoolean(en.getValue());
				rules.add(id -> {
					if (id.toString().equals(k)) return value ? Trilean.TRUE : Trilean.FALSE;
					return Trilean.UNSET;
				});
			}
		}
	}

	@Override
	public String getConfigName() {
		return "classic_block_drops";
	}

}
