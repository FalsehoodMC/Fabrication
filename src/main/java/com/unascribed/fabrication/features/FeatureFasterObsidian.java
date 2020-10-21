package com.unascribed.fabrication.features;

import java.lang.reflect.Field;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.unascribed.fabrication.support.Feature;
import com.unascribed.fabrication.support.OnlyIf;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.AbstractBlock.AbstractBlockState;

@OnlyIf(config="tweaks.faster_obsidian")
public class FeatureFasterObsidian implements Feature {

	@Override
	public void apply() {
		amendHardness(1/3f);
	}
	
	@Override
	public boolean undo() {
		amendHardness(3);
		return true;
	}

	private void amendHardness(float m) {
		Field f;
		try {
			f = AbstractBlockState.class.getDeclaredField("field_23172");
		} catch (NoSuchFieldException e) {
			try {
				f = AbstractBlockState.class.getDeclaredField("hardness");
			} catch (NoSuchFieldException e1) {
				throw new RuntimeException("Can't find hardness field", e1);
			}
		}
		FieldUtils.removeFinalModifier(f, true);
		for (BlockState bs : Blocks.OBSIDIAN.getStateManager().getStates()) {
			try {
				f.set(bs, ((float)f.get(bs))*m);
			} catch (Exception e) {
				throw new RuntimeException("Can't update hardness", e);
			}
		}
	}

	@Override
	public String getConfigKey() {
		return "tweaks.faster_obsidian";
	}

}
