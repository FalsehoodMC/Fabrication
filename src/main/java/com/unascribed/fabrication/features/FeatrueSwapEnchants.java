package com.unascribed.fabrication.features;

import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.interfaces.SetCrawling;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.Feature;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;

@EligibleIf(configAvailable="*.swap_conflicting_enchants", envMatches=Env.CLIENT)
public class FeatrueSwapEnchants implements Feature {
	public static KeyBinding keybind;

	@Override
	public void apply() {
		keybind = new KeyBinding("["+ MixinConfigPlugin.MOD_NAME+"] Swap Enchant", InputUtil.UNKNOWN_KEY.getCode(), "key.categories.gameplay") {
			@Override
			public void setPressed(boolean pressed) {
				if (pressed) {
					PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
					data.writeBoolean(pressed);
					MinecraftClient.getInstance().getNetworkHandler().getConnection().send(new CustomPayloadC2SPacket(new Identifier("fabrication", "swap_conflicting_enchants"), data));

				}
				super.setPressed(pressed);
			}
		};
		Agnos.registerKeyBinding(keybind);
	}

	@Override
	public boolean undo() {
		return false;
	}

	@Override
	public String getConfigKey() {
		return "*.swap_conflicting_enchants";
	}
}
