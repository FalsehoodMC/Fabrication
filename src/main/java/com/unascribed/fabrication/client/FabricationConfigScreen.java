package com.unascribed.fabrication.client;

import java.util.List;
import java.util.Set;

import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;

public class FabricationConfigScreen extends Screen {

	private static final Identifier BG = new Identifier("fabrication", "bg.png");
	
	private final Screen parent;
	private float timeExisted;
	private boolean leaving = false;
	private float timeLeaving;
	private float sidebarScrollVelocity;
	private float sidebarScroll;
	private float lastSidebarScroll;
	private float sidebarHeight;
	
	private boolean configuringServer;
	private boolean hasClonked;
	private float serverAnimateTime;
	private String whyCantConfigureServer = null;
	private Set<String> serverKnownConfigKeys = Sets.newHashSet();
	
	public FabricationConfigScreen(Screen parent) {
		super(new LiteralText("Fabrication configuration"));
		this.parent = parent;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (timeExisted == 0) {
			client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_WITHER_SHOOT, 2f, 0.1f));
			client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_BARREL_OPEN, 1.2f));
		}
		timeExisted += delta;
		if (leaving) {
			timeLeaving += delta;
		}
		if (parent != null && (leaving || timeExisted < 10)) {
			float a = sCurve5((leaving ? Math.max(0, 10-timeLeaving) : timeExisted)/10);
			GlStateManager.pushMatrix();
			GlStateManager.translatef(width/2f, height, 0);
			GlStateManager.rotatef(a*(leaving ? -180 : 180), 0, 0, 1);
			GlStateManager.translatef(-width/2, -height, 0);
			for (int x = -1; x <= 1; x++) {
				for (int y = -1; y <= 0; y++) {
					GlStateManager.pushMatrix();
					GlStateManager.translatef(width*x, height*y, 0);
					renderBackground(matrices);
					GlStateManager.popMatrix();
				}
			}
			parent.render(matrices, -200, -200, delta);
			GlStateManager.translatef(0, height, 0);
			GlStateManager.translatef(width/2f, height/2f, 0);
			GlStateManager.rotatef(180, 0, 0, 1);
			GlStateManager.translatef(-width/2f, -height/2f, 0);
			fill(matrices, -width, -height, width*2, 0, 0xFF2196F3);
			drawBackground(matrices, -200, -200, delta);
			drawForeground(matrices, -200, -200, delta);
			GlStateManager.popMatrix();
		} else {
			drawBackground(matrices, -200, -200, delta);
			drawForeground(matrices, mouseX, mouseY, delta);
		}
		if (leaving && timeLeaving > 10) {
			client.openScreen(parent);
		}
	}
	
	private void drawBackground(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		fillGradient(matrices, -width, 0, width*2, height, 0xFF2196F3, 0xFF009688);
		client.getTextureManager().bindTexture(BG);
		GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		float ratio = 502/1080f;
		
		float w = height*ratio;
		float brk = Math.min(width-w, (width*2/3f)-(w/3));
		float brk2 = brk+w;
		float border = (float)(20/client.getWindow().getScaleFactor());
		
		Matrix4f mat = matrices.peek().getModel();
		
		GlStateManager.disableCull();
		BufferBuilder bb = Tessellator.getInstance().getBuffer();
		bb.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
		bb.vertex(mat, border, 0, 0).texture(0, 0).next();
		bb.vertex(mat, brk, 0, 0).texture(0, 0).next();
		bb.vertex(mat, brk, height, 0).texture(0, 1).next();
		bb.vertex(mat, border, height, 0).texture(0, 1).next();
		
		bb.vertex(mat, brk, 0, 0).texture(0, 0).next();
		bb.vertex(mat, brk2, 0, 0).texture(1, 0).next();
		bb.vertex(mat, brk2, height, 0).texture(1, 1).next();
		bb.vertex(mat, brk, height, 0).texture(0, 1).next();
		
		bb.vertex(mat, brk2, 0, 0).texture(1, 0).next();
		bb.vertex(mat, width-border, 0, 0).texture(1, 0).next();
		bb.vertex(mat, width-border, height, 0).texture(1, 1).next();
		bb.vertex(mat, brk2, height, 0).texture(1, 1).next();
		
		bb.end();
		RenderSystem.enableAlphaTest();
		BufferRenderer.draw(bb);
	}

	private void drawForeground(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (serverAnimateTime > 0) {
			serverAnimateTime -= delta;
		}
		if (serverAnimateTime < 0) {
			serverAnimateTime = 0;
		}
		float a = sCurve5(serverAnimateTime/10f);
		if (a <= 0.05 && !hasClonked) {
			hasClonked = true;
			client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_BONE_BLOCK_STEP, 1f, 0.5f));
		}
		if (configuringServer) {
			a = 1-a;
		}
		GlStateManager.disableDepthTest();
		fill(matrices, width-120, 0, width, 16, 0x33000000);
		GlStateManager.pushMatrix();
			GlStateManager.translatef(width-60, 8, 0);
			GlStateManager.pushMatrix();
				GlStateManager.rotatef(a*-180, 0, 0, 1);
				float h = (40+(a*-100))/360f;
				if (h < 0) {
					h = 1+h;
				}
				matrices.push();
				matrices.translate(0, 0, 400);
				fill(matrices, -60, -8, 0, 8, MathHelper.hsvToRgb(h, 0.9f, 0.9f)|0xFF000000);
				matrices.pop();
				GlStateManager.pushMatrix();
					GlStateManager.rotatef(45, 0, 0, 1);
					// 8 / sqrt(2)
					float f = 5.6568542f;
					GlStateManager.scalef(f, f, 1);
					fill(matrices, -1, -1, 1, 1, 0xFFFFFFFF);
				GlStateManager.popMatrix();
				fill(matrices, -6, -1, -2, 1, 0xFF000000);
			GlStateManager.popMatrix();
			fill(matrices, -2, -2, 2, 2, 0xFF000000);
		GlStateManager.popMatrix();
		
		textRenderer.draw(matrices, "CLIENT", width-115, 4, 0xFF000000);
		textRenderer.draw(matrices, "SERVER", width-40, 4, whyCantConfigureServer == null ? 0xFF000000 : 0x44000000);
		
		String v = FabricLoader.getInstance().getModContainer("fabrication").get().getMetadata().getVersion().getFriendlyString();
		textRenderer.drawWithShadow(matrices, "§lFabrication v"+v+" §rby unascribed", 142, 42, -1);
		textRenderer.drawWithShadow(matrices, "Click a category on the left to change settings.", 142, 54, -1);
		fill(matrices, 0, -height, 130, height, 0x44000000);
		// TODO don't hardcode this
		ImmutableMap<String, String> sectionDescriptions = ImmutableMap.<String, String>builder()
				.put("General", "Non-features and broad features.")
				.put("Fixes", "Fixes for bugs and weird behavior.")
				.put("Utility", "Useful tidbits that don't modify gameplay.")
				.put("Tweaks", "Minor changes that fit with vanilla.")
				.put("Minor Mechanics", "Small additions to vanilla mechanics.")
				.put("Mechanics", "New mechanics, and powerful additions to vanilla ones.")
				.put("Balance", "Changes to vanilla balance.")
				.put("Weird Tweaks", "Opinionated gameplay changes.")
				.put("Pedantry", "Fixes for non-problems.")
				.put("Situational", "Rarely useful small features.")
				.build();
		// TODO cache this
		List<String> sectionNames = Lists.newArrayList();
		Multimap<String, String> options = Multimaps.newMultimap(Maps.newLinkedHashMap(), Lists::newArrayList);
		for (String key : MixinConfigPlugin.getAllKeys()) {
			int dot = key.indexOf('.');
			String section = key.substring(0, dot);
			String name = key.substring(dot+1);
			String fmt = formatTitleCase(section);
			if (!sectionNames.contains(fmt)) {
				sectionNames.add(fmt);
			}
			options.put(section, name);
		}
		float scroll = sidebarHeight < height ? 0 : lastSidebarScroll+((sidebarScroll-lastSidebarScroll)*client.getTickDelta());
		scroll = (float) (Math.floor((scroll*client.getWindow().getScaleFactor()))/client.getWindow().getScaleFactor());
		float y = 8-scroll;
		int newHeight = 8;
		for (String s : sectionNames) {
			if (y >= -12 && y < height) {
				textRenderer.draw(matrices, "§l"+s, 4, y, -1);
			}
			String desc = sectionDescriptions.getOrDefault(s, "No description available");
			List<OrderedText> lines = textRenderer.wrapLines(new LiteralText(desc), 116);
			y += 12;
			newHeight += 12;
			for (OrderedText ot : lines) {
				if (y >= -12 && y < height) {
					textRenderer.draw(matrices, ot, 8, y, -1);
				}
				y += 12;
				newHeight += 12;
			}
			y += 8;
			newHeight += 8;
		}
		sidebarHeight = newHeight;
		if (sidebarHeight >= height) {
			float knobHeight = (height/sidebarHeight)*height;
			float knobY = (scroll/(sidebarHeight-height))*(height-knobHeight);
			fill(matrices, 128, (int)knobY, 130, (int)(knobY+knobHeight), 0xAAFFFFFF);
		}
		super.render(matrices, mouseX, mouseY, delta);
		
		if (whyCantConfigureServer != null && mouseX > width-120 && mouseY < 16) {
			renderTooltip(matrices, Lists.transform(Lists.newArrayList(whyCantConfigureServer.split("\n")), LiteralText::new), mouseX+10, 20+mouseY);
		}
	}
	
	public static String formatTitleCase(String in) {
		String[] pieces = new String[] { in };
		if (in.contains(" ")) {
			pieces = in.toLowerCase().split(" ");
		} else if (in.contains("_")) {
			pieces = in.toLowerCase().split("_");
		}

		StringBuilder result = new StringBuilder();
		for (String s : pieces) {
			if (s == null)
				continue;
			String t = s.trim().toLowerCase();
			if (t.isEmpty())
				continue;
			result.append(Character.toUpperCase(t.charAt(0)));
			if (t.length() > 1)
				result.append(t.substring(1));
			result.append(" ");
		}
		return result.toString().trim();
	}

	@Override
	protected void init() {
		super.init();
		if (client.world == null) {
			whyCantConfigureServer = "You're not connected to a server.";
		} else if (client.getServer() != null) {
			whyCantConfigureServer = "The singleplayer server shares the client settings.";
		} else {
			CommandDispatcher<?> disp = client.player.networkHandler.getCommandDispatcher();
			if (disp.getRoot().getChild("fabrication") == null) {
				whyCantConfigureServer = "This server doesn't have Fabrication.";
			} else if (disp.getRoot().getChild("fabrication").getChild("config") == null) {
				whyCantConfigureServer = "You don't have permission to configure\nFabrication on this server.";
			} else {
				CommandNode<?> get = disp.getRoot().getChild("fabrication").getChild("config").getChild("get");
				if (get == null) {
					whyCantConfigureServer = "The /fabrication command syntax is unrecognized.";
				} else {
					serverKnownConfigKeys.clear();
					for (CommandNode<?> node : get.getChildren()) {
						serverKnownConfigKeys.add(node.getName());
					}
					whyCantConfigureServer = null;
				}
			}
		}
		addButton(new ColorButtonWidget(width-120, 70, 100, 20, 0xAA000000, new LiteralText("Done"), (w) -> {
			onClose();
		}));
	}
	
	@Override
	public void onClose() {
		leaving = true;
		client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_BARREL_CLOSE, 0.7f));
		client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_SHROOMLIGHT_PLACE, 2f, 1f));
	}
	
	@Override
	public void tick() {
		super.tick();
		if (sidebarHeight < height) return;
		lastSidebarScroll = sidebarScroll;
		sidebarScroll += sidebarScrollVelocity;
		sidebarScrollVelocity *= 0.75f;
		if (sidebarScroll > (sidebarHeight-height)) {
			if (sidebarScrollVelocity > 0) {
				sidebarScrollVelocity *= 0.6f;
			}
			sidebarScrollVelocity -= (sidebarScroll-(sidebarHeight-height))/8;
		} else if (sidebarScroll < 0) {
			if (sidebarScrollVelocity < 0) {
				sidebarScrollVelocity *= 0.6f;
			}
			sidebarScrollVelocity += (-sidebarScroll)/8;
		}
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (mouseX <= 120) {
			boolean dampen;
			if (amount < 0) {
				dampen = sidebarScroll < 0;
			} else {
				dampen = sidebarScroll > sidebarHeight;
			}
			sidebarScrollVelocity -= amount*(dampen ? 2 : 5);
		}
		return super.mouseScrolled(mouseX, mouseY, amount);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (configuringServer) {
			if (mouseX > width-120 && mouseY < 16) {
				hasClonked = false;
				serverAnimateTime = 10-serverAnimateTime;
				configuringServer = false;
				client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_XYLOPHONE, 0.8f, 1));
			}
		} else {
			if (mouseX > width-120 && mouseY < 16) {
				if (whyCantConfigureServer == null) {
					hasClonked = false;
					serverAnimateTime = 10-serverAnimateTime;
					configuringServer = true;
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_XYLOPHONE, 1.2f, 1));
				} else {
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO, 0.8f, 1));
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO, 0.7f, 1));
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	public static float sCurve5(float a) {
		float a3 = a * a * a;
		float a4 = a3 * a;
		float a5 = a4 * a;
		return (6 * a5) - (15 * a4) + (10 * a3);
	}
	
	@Override
	public void renderOrderedTooltip(MatrixStack matrices, List<? extends OrderedText> lines, int x, int y) {
		if (!lines.isEmpty()) {
			int maxWidth = 0;

			for (OrderedText line : lines) {
				int width = textRenderer.getWidth(line);
				if (width > maxWidth) {
					maxWidth = width;
				}
			}

			int innerX = x + 12;
			int innerY = y - 12;
			int totalHeight = 8;
			if (lines.size() > 1) {
				totalHeight += 2 + (lines.size() - 1) * 10;
			}

			if (innerX + maxWidth > width) {
				innerX -= 28 + maxWidth;
			}

			if (innerY + totalHeight + 6 > height) {
				innerY = height - totalHeight - 6;
			}

			matrices.push();
			fill(matrices, innerX-3, innerY-3, innerX+maxWidth+3, innerY+totalHeight+3, 0xAA000000);
			VertexConsumerProvider.Immediate vcp = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
			matrices.translate(0, 0, 400);

			for (int i = 0; i < lines.size(); ++i) {
				OrderedText line = lines.get(i);
				if (line != null) {
					textRenderer.draw(line, innerX, innerY, -1, false, matrices.peek().getModel(), vcp, false, 0, 0xF000F0);
				}
				if (i == 0) {
					innerY += 2;
				}
				innerY += 10;
			}

			vcp.draw();
			matrices.pop();
		}
	}

	
}
