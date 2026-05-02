package com.unascribed.fabrication.loaders;

import com.google.common.collect.Sets;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.QDIni;
import com.unascribed.fabrication.support.ConfigLoader;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.Set;

public class LoaderYeetRecipes implements ConfigLoader {

	public static Set<Identifier> recipesToYeet = Sets.newHashSet();
	public static final LoaderYeetRecipes instance = new LoaderYeetRecipes();

	@Override
	public void load(Path configDir, QDIni config, boolean loadError) {
		Set<Identifier> recipesToYeetTmp = Sets.newHashSet();
		for (String key : config.keySet()) {
			Identifier id = Identifier.tryParse(key);
			if (id == null) {
				FabLog.warn(key+" is not a valid identifier at "+config.getBlame(key, 0));
			} else {
				recipesToYeetTmp.add(id);
			}
		}
		recipesToYeet = recipesToYeetTmp;
	}

	@Override
	public String getConfigName() {
		return "yeet_recipes";
	}

}
