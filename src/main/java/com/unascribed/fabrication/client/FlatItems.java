package com.unascribed.fabrication.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.item.ItemStack;

public class FlatItems {

	public static boolean hasGeneratedModel(ItemStack item) {
		MinecraftClient mc = MinecraftClient.getInstance();
		BakedModel bm = mc.getItemRenderer().getHeldItemModel(item, mc.world, null, 1);
		return !bm.hasDepth();
	}
	
}
