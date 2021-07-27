package com.unascribed.fabrication.features;

import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.interfaces.SetCrawling;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.Feature;
import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;

@EligibleIf(configEnabled="*.crawling", envMatches=Env.CLIENT)
public class FeatureCrawling implements Feature {

	public static KeyBinding keybind;
	public static boolean forced = false;
	
	@Override
	public void apply() {
		keybind = new KeyBinding("[Fabrication] Crawl", InputUtil.UNKNOWN_KEY.getCode(), "key.categories.movement") {
			@Override
			public void setPressed(boolean pressed) {
				boolean send = !forced && isPressed() != pressed && MinecraftClient.getInstance().getNetworkHandler() != null;
				boolean state = pressed;
				boolean toggle = MinecraftClient.getInstance().options.sneakToggled;
				if (toggle && !pressed) {
					send = false;
				}
				if (send) {
					if (toggle) {
						state = !((SetCrawling)MinecraftClient.getInstance().player).fabrication$isCrawling();
					}
					setCrawling(state, false);
				}
				super.setPressed(pressed);
			}
		};
		Agnos.registerKeyBinding(keybind);
	}

	public static void setCrawling(boolean state, boolean forced) {
		FeatureCrawling.forced = forced;
		PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
		data.writeBoolean(state);
		MinecraftClient.getInstance().getNetworkHandler().getConnection().send(new CustomPayloadC2SPacket(new Identifier("fabrication", "crawling"), data));
		((SetCrawling)MinecraftClient.getInstance().player).fabrication$setCrawling(state);
	}

	@Override
	public boolean undo() {
		return false;
	}

	@Override
	public String getConfigKey() {
		return "*.crawling";
	}
	
}
