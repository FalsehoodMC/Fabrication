package com.unascribed.fabrication.features;

import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.Feature;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.world.World;

@EligibleIf(configAvailable="*.show_bee_count_tooltip", envMatches=Env.CLIENT)
public class FeatureShowBeeCountTooltip implements Feature {

	private boolean applied = false;
	private boolean active = false;

	@Override
	public void apply(MinecraftServer minecraftServer, World world) {
		active = true;
		if (!applied) {
			applied = true;
			Agnos.runForTooltipRender((stack, lines) -> {
				if (active && !stack.isEmpty() && stack.contains(DataComponentTypes.BLOCK_ENTITY_DATA)) {
					NbtCompound tag = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA).getNbt();
					if (tag == null || !tag.contains("Bees", NbtElement.LIST_TYPE)) return;

					lines.add(Text.literal("Bees: " + ((NbtList) tag.get("Bees")).size()));
				}
			});
		}
	}

	@Override
	public boolean undo(MinecraftServer minecraftServer, World world) {
		active = false;
		return true;
	}

	@Override
	public String getConfigKey() {
		return "*.show_bee_count_tooltip";
	}

}
