package com.unascribed.fabrication.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class ScrollBar {
	public float target;
	public float scroll;
	public float height;
	public float displayHeight;

	public ScrollBar(float displayHeight) {
		this.displayHeight = displayHeight;
	}

	public float getScaledScroll(MinecraftClient client) {
		return  (float) (Math.floor(((height < displayHeight ? 0 : scroll) * client.getWindow().getScaleFactor())) / client.getWindow().getScaleFactor());
	}

	public void scroll(double amount) {
		target-=amount;
	}

	public void tick() {
		if (height > displayHeight) {
			scroll += (target-scroll)/2;
			if (target < 0) target /= 2;
			float h = height- displayHeight;
			if (target > h) target = h+((target-h)/2);
		}
	}

}
