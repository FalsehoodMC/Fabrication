package com.unascribed.fabrication.support.optional;

import com.unascribed.fabrication.Agnos;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

@Environment(EnvType.CLIENT)
public class OptionalFabricRender {
	public static void runForHudRender(Agnos.HudRenderCallback r) {
		HudRenderCallback.EVENT.register(r::render);
	}
}
