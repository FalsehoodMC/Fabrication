package com.unascribed.fabrication.features;

import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.Feature;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.LiteralText;

@EligibleIf(configAvailable="*.show_bee_count_tooltip", envMatches=Env.CLIENT)
public class FeatureShowBeeCountTooltip implements Feature {

	private boolean applied = false;
	private boolean active = false;

	@Override
	public void apply() {
		active = true;
		if (!applied) {
			applied = true;
			Agnos.runForTooltipRender((stack, lines) -> {
				if (active && !stack.isEmpty() && stack.hasTag()) {
					NbtCompound tag = stack.getTag().getCompound("BlockEntityTag");
					if (tag == null || !tag.contains("Bees", NbtType.LIST)) return;

					lines.add(new LiteralText("Bees: " + ((NbtList) tag.get("Bees")).size()));
				}
			});
		}
	}

	@Override
	public boolean undo() {
		active = false;
		return true;
	}

	@Override
	public String getConfigKey() {
		return "*.show_bee_count_tooltip";
	}

}
