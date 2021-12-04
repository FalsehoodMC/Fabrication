package com.unascribed.fabrication.x;

import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class FabricationX {

	public static final PursurverBlock PURSURVER = new PursurverBlock(FabricBlockSettings.copyOf(Blocks.OBSERVER));
	
	public static void init() {
		if (MixinConfigPlugin.isEnabled("x.pursurver_block")) {
			Registry.register(Registry.BLOCK, "fabricationx:pursurver", PURSURVER);
			Registry.register(Registry.ITEM, "fabricationx:pursurver", new BlockItem(PURSURVER, new Item.Settings()));
			addResources("pursurver");
		}
	}

	private static void addResources(String id) {
		ResourceManagerHelper.registerBuiltinResourcePack(new Identifier("fabricationx", "x/"+id), FabricLoader.getInstance().getModContainer("fabrication").get(), ResourcePackActivationType.ALWAYS_ENABLED);
	}
	
}
