package com.unascribed.fabrication.features;

import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.Feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

@EligibleIf(configEnabled="*.gold_tools_useful_in_nether", modLoaded="fabric")
public class FeatureGoldToolsUsefulInNether implements Feature {

	public static final Tag<Block> NETHER_BLOCKS = Agnos.INST.registerBlockTag("fabrication:nether_blocks");
	public static final Tag<Block> NETHER_BLOCKS_ONLY_IN_NETHER = Agnos.INST.registerBlockTag("fabrication:nether_blocks_only_in_nether");
	
	public static final Tag<Item> NETHER_TOOLS = Agnos.INST.registerItemTag("fabrication:nether_tools");
	public static final Tag<Item> GOLD_TOOLS = Agnos.INST.registerItemTag("fabrication:gold_tools");
	public static final Tag<Item> POSSIBLY_FUNGAL_TOOLS = Agnos.INST.registerItemTag("fabrication:possibly_fungal_tools");
	
	private boolean applied = false;
	private boolean active = false;
	
	@Override
	public void apply() {
		active = true;
		if (!applied) {
			applied = true;
			if (Agnos.INST.getCurrentEnv() == Env.CLIENT) {
				applyClient();
			}
		}
	}

	@Environment(EnvType.CLIENT)
	private void applyClient() {
		Agnos.INST.runForTooltipRender((stack, lines) -> {
			if (active && (stack.getItem().isIn(GOLD_TOOLS) || (stack.hasTag() && stack.getTag().getBoolean("fabrication:ActLikeGold")))) {
				for (int i = 0; i < lines.size(); i++) {
					Object t = lines.get(i);
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
