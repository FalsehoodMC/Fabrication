package com.unascribed.fabrication.features;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

@EligibleIf(configEnabled="*.gold_tools_useful_in_nether", modLoaded="fabric")
public class FeatureGoldToolsUsefulInNether implements Feature {

	public static final Tag<Block> NETHER_BLOCKS = TagRegistry.block(new Identifier("fabrication", "nether_blocks"));
	public static final Tag<Block> NETHER_BLOCKS_ONLY_IN_NETHER = TagRegistry.block(new Identifier("fabrication", "nether_blocks_only_in_nether"));
	
	public static final Tag<Item> NETHER_TOOLS = TagRegistry.item(new Identifier("fabrication", "nether_tools"));
	public static final Tag<Item> GOLD_TOOLS = TagRegistry.item(new Identifier("fabrication", "gold_tools"));
	public static final Tag<Item> POSSIBLY_FUNGAL_TOOLS = TagRegistry.item(new Identifier("fabrication", "possibly_fungal_tools"));
	
	private boolean applied = false;
	private boolean active = false;
	
	@Override
	public void apply() {
		active = true;
		if (!applied) {
			applied = true;
			if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
				applyClient();
			}
		}
	}

	@Environment(EnvType.CLIENT)
	private void applyClient() {
		ItemTooltipCallback.EVENT.register((stack, ctx, lines) -> {
			if (active && (stack.getItem().isIn(GOLD_TOOLS) || (stack.hasTag() && stack.getTag().getBoolean("fabrication:ActLikeGold")))) {
				for (int i = 0; i < lines.size(); i++) {
					Text t = lines.get(i);
					if (t instanceof TranslatableText) {
						if (((TranslatableText) t).getKey().equals("item.durability")) {
							lines.set(i, new TranslatableText("item.durability", (stack.getMaxDamage() - stack.getDamage())+(1-(stack.getTag().getInt("PartialDamage")/50D)), stack.getMaxDamage()));
						}
					}
				}
			}
		});
	}

	@Override
	public boolean undo() {
		active = false;
		return true;
	}

	@Override
	public String getConfigKey() {
		return "*.gold_tools_useful_in_nether";
	}

}
