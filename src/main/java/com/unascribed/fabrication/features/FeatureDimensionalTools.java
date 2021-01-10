package com.unascribed.fabrication.features;

import java.text.NumberFormat;

import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.Feature;
import com.unascribed.fabrication.support.SpecialEligibility;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.TranslatableText;

@EligibleIf(configEnabled="*.dimensional_tools", specialConditions=SpecialEligibility.EVENTS_AVAILABLE)
public class FeatureDimensionalTools implements Feature {

	private static final NumberFormat format = NumberFormat.getNumberInstance();
	static {
		format.setGroupingUsed(false);
		format.setMaximumFractionDigits(2);
	}
	
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
			if (active && !stack.isEmpty() && (stack.hasTag() && stack.getTag().contains("fabrication:PartialDamage"))) {
				for (int i = 0; i < lines.size(); i++) {
					Object t = lines.get(i);
					double part = stack.getTag().getDouble("fabrication:PartialDamage");
					if (t instanceof TranslatableText) {
						if (((TranslatableText) t).getKey().equals("item.durability")) {
							lines.set(i, new TranslatableText("item.durability",
									format.format((stack.getMaxDamage() - stack.getDamage())+(1-part)), stack.getMaxDamage()));
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
		return "*.dimensional_tools";
	}

}
