package com.unascribed.fabrication.support;

import com.unascribed.fabrication.Agnos;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FabricationEventsClient {
	private static final Set<Agnos.TooltipRenderCallback> tooltipRender = new HashSet<>();
	private static final Set<Agnos.HudRenderCallback> hudRender = new HashSet<>();
	private static KeyBinding[] keybindings = null;
	public static void addTooltip(Agnos.TooltipRenderCallback c) {
		tooltipRender.add(c);
	}
	public static void addHud(Agnos.HudRenderCallback c) {
		hudRender.add(c);
	}
	public static void addKeybind(KeyBinding c) {
		if (keybindings == null) {
			keybindings = new KeyBinding[]{c};
			return;
		}
		for (KeyBinding e : keybindings) {
			if (e == c) return;
		}
		KeyBinding[] nk = new KeyBinding[keybindings.length + 1];
		System.arraycopy(keybindings, 0, nk, 0, keybindings.length);
		nk[keybindings.length] = c;
		keybindings = nk;
	}

	public static void tooltip(ItemStack stack, List<Text> lines) {
		for (Agnos.TooltipRenderCallback c : tooltipRender) {
			c.render(stack, lines);
		}
	}
	public static void hud(DrawContext drawContext, float tickDelta) {
		for (Agnos.HudRenderCallback c : hudRender) {
			c.render(drawContext, tickDelta);
		}
	}

	public static KeyBinding[] keys(KeyBinding[] in) {
		if (in == null) return null;
		if (keybindings == null) return in;
		byte[] cnew = new byte[keybindings.length];
		int cursor = 0;
		main:
		for (byte i=0; i<keybindings.length; i++) {
			KeyBinding ke = keybindings[i];
			for (KeyBinding kin : in) {
				if (ke == kin) continue main;
			}
			cnew[cursor++] = i;
		}
		if (cursor == 0) return in;
		KeyBinding[] nin = new KeyBinding[in.length + cursor];
		System.arraycopy(in, 0, nin, 0, in.length);
		for (byte i : cnew) {
			nin[in.length+--cursor] = keybindings[i];
		}
		return nin;
	}

}
