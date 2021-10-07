package com.unascribed.fabrication.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class FlatItems {

	public static boolean hasGeneratedModel(ItemStack item) {
		MinecraftClient mc = MinecraftClient.getInstance();
		BakedModel bm = mc.getItemRenderer().getHeldItemModel(item, mc.world, null, 1);
		return !bm.hasDepth() || item.isOf(Items.TRIDENT) || item.isOf(Items.SPYGLASS);
	}
	
}
