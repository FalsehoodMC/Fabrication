package com.unascribed.fabrication.client;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.CommandDispatcher;
import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.interfaces.GetServerConfig;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.MixinConfigPlugin.Profile;
import com.unascribed.fabrication.support.ResolvedTrilean;
import com.unascribed.fabrication.support.Trilean;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import io.netty.buffer.Unpooled;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;

public class FabricationConfigScreen extends Screen {

	private static final ImmutableMap<String, String> SECTION_DESCRIPTIONS = ImmutableMap.<String, String>builder()
			.put("general", "Broad features\nand global settings.")
			.put("fixes", "Fixes for bugs\nand weird behavior.")
			.put("utility", "Useful tidbits that\ndon't modify gameplay.")
			.put("tweaks", "Minor changes that\nfit with vanilla.")
			.put("minor_mechanics", "Small additions to\nvanilla mechanics.")
			.put("mechanics", "New mechanics and\npowerful additions.")
			.put("balance", "Changes to vanilla\nbalance.")
			.put("weird_tweaks", "Opinionated\nchanges.")
			.put("pedantry", "Fixes for\nnon-problems.")
			.put("situational", "Rarely useful\nsmall features.")
			.build();
	
	private static final ImmutableMap<Profile, String> PROFILE_DESCRIPTIONS = ImmutableMap.<Profile, String>builder()
			.put(Profile.GREEN, "Enables nothing. Build your own config.")
			.put(Profile.BLONDE, "Enables Fixes and Utility. The bare minimum.\nYou like vanilla.")
			.put(Profile.LIGHT, "Blonde + Tweaks. Default.")
			.put(Profile.MEDIUM, "Light + Minor Mechanics.")
			.put(Profile.DARK, "Medium + Mechanics. Recommended.")
			.put(Profile.VIENNA, "Dark + Balance + Weird Tweaks.\nYou agree with all of Una's opinions.")
			.put(Profile.BURNT, "Screw it, enable everything.")
			.build();
	
	private static final ImmutableMap<Profile, Integer> PROFILE_COLORS = ImmutableMap.<Profile, Integer>builder()
			.put(Profile.GREEN, 0xFF8BC34A)
			.put(Profile.BLONDE, 0xFFFFCC80)
			.put(Profile.LIGHT, 0xFFA1887F)
			.put(Profile.MEDIUM, 0xFF6D4C41)
			.put(Profile.DARK, 0xFF4E342E)
			.put(Profile.VIENNA, 0xFF2B1B18)
			.put(Profile.BURNT, 0xFF12181B)
			.build();
	
	private static final Identifier BG = new Identifier("fabrication", "bg.png");
	
	private final Screen parent;
	private float timeExisted;
	private boolean leaving = false;
	private float timeLeaving;
	private float sidebarScrollVelocity;
	private float sidebarScroll;
	private float lastSidebarScroll;
	private float sidebarHeight;
	
	private boolean didClick;
	private float lastClickX;
	private float lastClickY;
	private float selectTime;
	private String selectedSection;
	private String prevSelectedSection;
	
	private int tooltipBlinkTicks = 0;
	
	private boolean configuringServer;
	private boolean hasClonked = true;
	private float serverAnimateTime;
	private String whyCantConfigureServer = null;
	private Set<String> serverKnownConfigKeys = Sets.newHashSet();
	
	private final Map<String, Trilean> optionPreviousValues = Maps.newHashMap();
	private final Map<String, Float> optionAnimationTime = Maps.newHashMap();
	
	private boolean bufferTooltips = false;
	private final List<Runnable> bufferedTooltips = Lists.newArrayList();
	
	public FabricationConfigScreen(Screen parent) {
		super(new LiteralText("Fabrication configuration"));
		this.parent = parent;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (timeExisted == 0 && !MixinConfigPlugin.isEnabled("*.reduced_motion")) {
			client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_WITHER_SHOOT, 2f, 0.1f));
			client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_BARREL_OPEN, 1.2f));
		}
		timeExisted += delta;
		if (leaving) {
			timeLeaving += delta;
		}
		if (parent != null && (leaving || timeExisted < 10) && !MixinConfigPlugin.isEnabled("*.reduced_motion")) {
			float a = sCurve5((leaving ? Math.max(0, 10-timeLeaving) : timeExisted)/10);
			GlStateManager.pushMatrix();
			GlStateManager.translatef(width/2f, height, 0);
			GlStateManager.rotatef(a*(leaving ? -180 : 180), 0, 0, 1);
			GlStateManager.translatef(-width/2, -height, 0);
			GlStateManager.pushMatrix();
			GlStateManager.translatef(0, height, 0);
			GlStateManager.translatef(width/2f, height/2f, 0);
			GlStateManager.rotatef(180, 0, 0, 1);
			GlStateManager.translatef(-width/2f, -height/2f, 0);
			fill(matrices, -width, -height, width*2, 0, 0xFF2196F3);
			drawBackground(matrices, -200, -200, delta);
			drawForeground(matrices, -200, -200, delta);
			GlStateManager.popMatrix();
			for (int x = -1; x <= 1; x++) {
				for (int y = -1; y <= 0; y++) {
					GlStateManager.pushMatrix();
					GlStateManager.translatef(width*x, height*y, 0);
					renderBackgroundTexture(0);
					GlStateManager.popMatrix();
				}
			}
			parent.render(matrices, -200, -200, delta);
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
		
		GlStateManager.enableBlend();
		RenderSystem.defaultBlendFunc();
		GlStateManager.color4f(1, 1, 1, 0.5f);
		
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
		if (selectTime > 0) {
			selectTime -= delta;
		}
		if (selectTime < 0) {
			selectTime = 0;
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
		fill(matrices, width-120, 0, width*2, 16, 0x33000000);
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
		
		fill(matrices, -width, -height, 130, height, 0x44000000);
		// TODO cache this
		Multimap<String, String> options = Multimaps.newMultimap(Maps.newLinkedHashMap(), Lists::newArrayList);
		for (String key : MixinConfigPlugin.getAllKeys()) {
			int dot = key.indexOf('.');
			String section = key.substring(0, dot);
			String name = key.substring(dot+1);
			options.put(section, name);
		}
		float scroll = sidebarHeight < height ? 0 : lastSidebarScroll+((sidebarScroll-lastSidebarScroll)*client.getTickDelta());
		scroll = (float) (Math.floor((scroll*client.getWindow().getScaleFactor()))/client.getWindow().getScaleFactor());
		float y = 8-scroll;
		int newHeight = 8;
		int i = 0;
		float selectedChoiceY = 0;
		float prevSelectedChoiceY = 0;
		for (String s : options.keySet()) {
			float selectA;
			if (s.equals(selectedSection)) {
				selectA = sCurve5((10-selectTime)/10f);
				selectedChoiceY = y;
			} else if (s.equals(prevSelectedSection)) {
				selectA = sCurve5(selectTime/10f);
				prevSelectedChoiceY = y;
			} else {
				selectA = 0;
			}
			if (selectA > 0) {
				GlStateManager.disableCull();
				GlStateManager.enableBlend();
				RenderSystem.defaultBlendFunc();
				GlStateManager.disableTexture();
				GlStateManager.disableAlphaTest();
				Matrix4f mat = matrices.peek().getModel();
				GlStateManager.shadeModel(GL11.GL_SMOOTH);
				BufferBuilder bb = Tessellator.getInstance().getBuffer();
				bb.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
				bb.vertex(mat, 0, y-4, 0).color(1f, 1f, 1f, 0.2f).next();
				bb.vertex(mat, 130*selectA, y-4, 0).color(1f, 1f, 1f, 0.2f+((1-selectA)*0.8f)).next();
				bb.vertex(mat, 130*selectA, y+36, 0).color(1f, 1f, 1f, 0.2f+((1-selectA)*0.8f)).next();
				bb.vertex(mat, 0, y+36, 0).color(1f, 1f, 1f, 0.2f).next();
				bb.end();
				BufferRenderer.draw(bb);
				GlStateManager.shadeModel(GL11.GL_FLAT);
				GlStateManager.enableTexture();
			}
			float startY = y;
			if (y >= -24 && y < height) {
				GlStateManager.enableBlend();
				RenderSystem.defaultBlendFunc();
				client.getTextureManager().bindTexture(new Identifier("fabrication", "category/"+s+".png"));
				GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
				GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
				GlStateManager.color4f(1, 1, 1, 0.4f);
				GlStateManager.pushMatrix();
				GlStateManager.translatef(0, y, 0);
				drawTexture(matrices, (130-26), 0, 0, 0, 0, 24, Math.min(24, (int)Math.ceil(height-y)), 24, 24);
				GlStateManager.popMatrix();
			}
			if (y >= -12 && y < height) {
				textRenderer.draw(matrices, "§l"+formatTitleCase(s), 4, y, -1);
			}
			String desc = SECTION_DESCRIPTIONS.getOrDefault(s, "No description available");
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
			if (didClick) {
				if (lastClickX >= 0 && lastClickX <= 130 && lastClickY > startY-4 && lastClickY < y) {
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_BELL, 0.6f+(i*0.1f), 1f));
					if (!s.equals(selectedSection)) {
						prevSelectedSection = selectedSection;
						selectedSection = s;
						selectTime = 10-selectTime;
					}
				}
			}
			y += 8;
			newHeight += 8;
			i++;
		}
		sidebarHeight = newHeight;
		if (sidebarHeight >= height) {
			float knobHeight = (height/sidebarHeight)*height;
			float knobY = (scroll/(sidebarHeight-height))*(height-knobHeight);
			fill(matrices, 128, (int)knobY, 130, (int)(knobY+knobHeight), 0xAAFFFFFF);
		}
		
		bufferTooltips = true;
		drawSection(matrices, selectedSection, mouseX, mouseY, selectedChoiceY, sCurve5((10-selectTime)/10f));
		if (prevSelectedSection != null) {
			drawSection(matrices, prevSelectedSection, -200, -200, prevSelectedChoiceY, sCurve5(selectTime/10f));
		}
		
		if (didClick) didClick = false;
		
		textRenderer.draw(matrices, "Config changes are applied in real", 136, height-20, -1);
		textRenderer.draw(matrices, "time and do not need to be saved.", 136, height-10, -1);
		
		super.render(matrices, mouseX, mouseY, delta);
		
		bufferTooltips = false;
		for (Runnable r : bufferedTooltips) {
			r.run();
		}
		bufferedTooltips.clear();
		
		if (mouseX > width-120 && mouseY < 16) {
			String msg;
			if (whyCantConfigureServer != null) {
				msg = ((tooltipBlinkTicks/5)%2 == 1 ? "§c" : "§e")+"§l"+whyCantConfigureServer;
			} else {
				int srv = serverKnownConfigKeys.size();
				int cli = MixinConfigPlugin.getAllKeys().size();
				msg = "§d§lServer has Fabrication and is recognized.";
				if (srv != cli) {
					msg += "\n§oMismatch: Server has "+srv+" options. Client has "+cli+".";
					if (srv > cli) {
						msg += "\n§cOptions unknown to the client will not appear.";
					} else if (cli > srv) {
						msg += "\n§eOptions unknown to the server will be disabled.";
					}
				}
			}
			msg += "\n§fChanges will apply to the "+(configuringServer ? "§dSERVER" : "§6CLIENT")+"§f.";
			renderTooltip(matrices, Lists.transform(Lists.newArrayList(msg.split("\n")),
					s -> new LiteralText(s)), mouseX+10, 20+mouseY);
		}
	}
	
	private void drawSection(MatrixStack matrices, String section, float mouseX, float mouseY, float choiceY, float a) {
		if (a <= 0) return;
		// jesus fucking christ
		GlStateManager.pushMatrix();
		GlStateManager.translatef(60, choiceY+16, 0);
		GlStateManager.scalef(a, a, 1);
		GlStateManager.translatef(-60, -(choiceY+16), 0);
		int y = 16;
		if (section == null) {
			String v = FabricLoader.getInstance().getModContainer("fabrication").get().getMetadata().getVersion().getFriendlyString();
			textRenderer.drawWithShadow(matrices, "§lFabrication v"+v+" §rby unascribed", 142, 42, -1);
			textRenderer.drawWithShadow(matrices, "Click a category on the left to change settings.", 142, 54, -1);
		} else {
			GlStateManager.enableBlend();
			RenderSystem.defaultBlendFunc();
			client.getTextureManager().bindTexture(new Identifier("fabrication", "category/"+section+".png"));
			GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			GlStateManager.color4f(1, 1, 1, 0.1f);
			GlStateManager.pushMatrix();
			GlStateManager.translatef(130+((width-130)/2f), height/2f, 0);
			// Desyncing the state manager for fun and profit
			GlStateManager.enableAlphaTest();
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			drawTexture(matrices, -80, -80, 0, 0, 0, 160, 160, 160, 160);
			GlStateManager.disableAlphaTest();
			GlStateManager.popMatrix();
			if ("general".equals(section)) {
				GlStateManager.enableBlend();
				RenderSystem.defaultBlendFunc();
				client.getTextureManager().bindTexture(new Identifier("fabrication", "coffee_bean.png"));
				GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
				GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
				int x = 0;
				Profile hovered = null;
				for (Profile p : Profile.values()) {
					boolean selected = getRawValue("general.profile").toUpperCase(Locale.ROOT).equals(p.name());
					if (mouseX >= 134+x && mouseX <= 134+x+16 && mouseY >= 18 && mouseY <= 18+16) {
						hovered = p;
					}
					if (didClick && lastClickX >= 134+x && lastClickX <= 134+x+16 && lastClickY >= 18 && lastClickY <= 18+16) {
						if (p == Profile.BURNT) {
							client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ITEM_FLINTANDSTEEL_USE, 1f));
							client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_FIRE_AMBIENT, 1f, 1f));
						} else {
							client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_BASS, 0.7f+(p.ordinal()*0.22f), 1f));
						}
						setValue("general.profile", p.name().toLowerCase(Locale.ROOT));
					}
					color(PROFILE_COLORS.get(p), selected ? 1f : hovered == p ? 0.6f : 0.3f);
					drawTexture(matrices, 134+x, 18, 0, 0, 0, 16, 16, 16, 16);
					x += 18;
				}
				int textRight = textRenderer.draw(matrices, "Profile", 136, 6, -1);
				if (mouseX >= 136 && mouseX <= textRight && mouseY >= 6 && mouseY <= 18) {
					renderTooltip(matrices, new LiteralText("Choose your defaults."), (int)mouseX, (int)mouseY);
				}
				if (hovered != null) {
					List<String> li = Lists.newArrayList();
					li.add("§l"+formatTitleCase(hovered.name()));
					for (String s : PROFILE_DESCRIPTIONS.get(hovered).split("\n")) {
						li.add(s);
					}
					renderTooltip(matrices, Lists.transform(li, s -> new LiteralText(s)), (int)mouseX, (int)mouseY);
				}
				y = 40;
				y = drawTrilean(matrices, "general.runtime_checks", "Runtime Checks",
						"Allows changing settings arbitrarily on-the-fly, but is very slightly\n" +
						"slower and a fair bit less compatible, as it applies all mixins regardless\n" +
						"of if the feature is enabled or not.\n" +
						"§cIf this is disabled, you must restart the game for changes made here to apply.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "general.reduced_motion", "Reduced Motion",
						"Disable high-motion GUI animations.", y, mouseX, mouseY, true);
				y = drawTrilean(matrices, "general.taggable_players", "Taggable Players",
						"Allows players to be tagged by ops with /fabrication tag.", y, mouseX, mouseY, false);
			} else if ("fixes".equals(section)) {
				y = drawTrilean(matrices, "fixes.sync_attacker_yaw", "Sync Attacker Yaw",
						"Makes the last attacker yaw field sync properly when the player is damaged, instead "
						+ "of always being zero. Causes the camera shake animation when being hurt to tilt "
						+ "away from the source of damage.\n"
						+ "Fixes MC-26678, which is closed as Won't Fix.\n"
						+ "Needed on both client and server, but doesn't break vanilla clients.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "fixes.furnace_minecart_pushing", "Furnace Minecart Pushing",
						"Right-clicking a furnace minecart with a non-fuel while it's out of " +
						"fuel gives it a little bit of fuel, allowing you to \"push\" it. " +
						"Removed some time after 17w46a (1.13 pre releases)\n" +
						"Unnecessary on client.\n" +
						"Note: All furnace minecart tweaks enable a mixin that overrides " +
						"multiple methods in the furnace minecart entity. If you have another " +
						"mod that changes furnace minecarts (wow!) then you'll need to disable\n" +
						"this.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "fixes.use_player_list_name_in_tag", "Use Player List Name In Tag",
						"Changes player name tags to match names in the player list. "
						+ "Good in combination with nickname mods like Drogtor.", y, mouseX, mouseY, true);
				y = drawTrilean(matrices, "fixes.better_pause_freezing", "Better Pause Freezing",
						"Makes textures not tick while the game is paused.\n" +
						"May do more in the future.", y, mouseX, mouseY, true);
				y = drawTrilean(matrices, "fixes.inanimates_can_be_invisible", "Inanimates Can Be Invisible",
						"Prevents inanimate entities from rendering at all if their \"invisible\" " +
						"flag is set to true.", y, mouseX, mouseY, true);
				y = drawTrilean(matrices, "fixes.omniscent_player", "Omniscent Player",
						"The player render in the inventory follows your cursor, even if it's not" +
						"inside the game window.", y, mouseX, mouseY, true);
				y = drawTrilean(matrices, "fixes.uncap_menu_fps", "Uncap Menu FPS",
						"Allow the menu to render at your set frame limit instead of being " +
						"locked to 60. (30 in older versions)", y, mouseX, mouseY, true);
			} else if ("utility".equals(section)) {
				y = drawTrilean(matrices, "utility.mods_command", "/mods command",
						"Adds a /mods command that anyone can run that lists installed mods. Lets " +
						"players see what changes are present on a server at a glance.\n" +
						"Requires Fabric API. Force disabled if Fabric API is not present.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "utility.legacy_command_syntax", "Legacy Command Syntax",
						"Re-adds /toggledownfall and numeric arguments to /difficulty and /gamemode, as "
						+ "well as capitalized arguments to /summon.\n" +
						"Requires Fabric API. Force disabled if Fabric API is not present.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "utility.books_show_enchants", "Books Show Enchants",
						"Makes enchanted books show the first letter of their enchants in the" +
						"bottom left, cycling through enchants every second if they have multiple.", y, mouseX, mouseY, true);
				y = drawTrilean(matrices, "utility.tools_show_important_enchant", "Tools Show Important Enchant",
						"Makes tools enchanted with Silk Touch, Fortune, or Riptide show " +
						"the first letter of that enchant in the top left.\n" +
						"Never break an Ender Chest with the wrong tool again.", y, mouseX, mouseY, true);
				y = drawTrilean(matrices, "utility.despawning_items_blink", "Despawning Items Blink",
						"Items that are about to despawn blink.\n" +
						"Needed on both sides. Server sends packets, client actually does the blinking.\n" +
						"Will not break vanilla clients.", y, mouseX, mouseY, false);
			} else if ("tweaks".equals(section)) {
				y = drawTrilean(matrices, "tweaks.creepers_explode_when_on_fire", "Creepers Explode When On Fire",
						"Causes creepers to light their fuses when lit on fire. Just because.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "tweaks.tridents_in_void_return", "Tridents In Void Return",
						"Makes Loyalty tridents immune to void damage, and causes them to start " +
						"their return timer upon falling into the void.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "tweaks.less_annoying_fire", "Less Annoying Fire",
						"Makes the \"on fire\" overlay half as tall, and removes it completely if " +
						"you have Fire Resistance.", y, mouseX, mouseY, true);
				y = drawTrilean(matrices, "tweaks.less_restrictive_note_blocks", "Less Restrictive Note Blocks",
						"Allows note blocks to play if any block next to them has a nonsolid " +
						"face, instead of only if the block above is air.\n" +
						"On the client, just adjusts the note particle to fly the right direction.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "tweaks.cactus_walk_doesnt_hurt_with_boots", "Cactus Walk Doesn't Hurt With Boots",
						"Makes walking on top of a cactus (not touching the side of one) with " +
						"boots equipped not deal damage.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "tweaks.cactus_brush_doesnt_hurt_with_chest", "Cactus Brush Doesn't Hurt With Chest",
						"Makes touching the side of a cactus (not walking on top of one) with " +
						"a chestplate equipped not deal damage.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "tweaks.bush_walk_doesnt_hurt_with_armor", "Bush Walk Doesn't Hurt With Armor",
						"Makes walking through berry bushes with both leggings and boots\n" +
						"equipped not deal damage.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "tweaks.shulker_bullets_despawn_on_death", "Shulker Bullets Despawn On Death",
						"Makes shulker bullets despawn when the shulker that shot them is killed.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "tweaks.arrows_work_in_water", "Arrows Work In Water",
						"Makes arrows viable in water by reducing their drag. Nowhere near as\n" +
						"good as a trident, but usable.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "tweaks.reverse_note_block_tuning", "Reverse Note Block Tuning",
						"Sneaking while tuning a note block reduces its pitch rather than increases.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "tweaks.campfires_place_unlit", "Campfires Place Unlit",
						"Campfires are unlit when placed and must be lit.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "tweaks.ghost_chest_woo_woo", "Ghost Chest Woo Woo",
						"?", y, mouseX, mouseY, true);
			} else if ("minor_mechanics".equals(section)) {
				y = drawTrilean(matrices, "minor_mechanics.feather_falling_five", "Feather Falling V",
						"Makes Feather Falling V a valid enchant that completely negates fall damage.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "minor_mechanics.feather_falling_five_damages_boots", "Feather Falling V Damages Boots",
						"Absorbing fall damage with Feather Falling V causes damage to the boots.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "minor_mechanics.furnace_minecart_any_fuel", "Furnace Minecart Any Fuel",
						"Allows furnace minecarts to accept any furnace fuel, rather than just " +
						"coal and charcoal.\n" +
						"Note: All furnace minecart tweaks enable a mixin that overrides " +
						"multiple methods in the furnace minecart entity. If you have another " +
						"mod that changes furnace minecarts (wow!) then you'll need to disable " +
						"this.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "minor_mechanics.infibows", "InfiBows (aka Bow Infinity Fix)",
						"Makes Infinity bows not require an arrow in your inventory to fire.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "minor_mechanics.note_blocks_play_on_landing", "Note Blocks Play On Landing",
						"Makes note blocks play their note when landed on. Also triggers observers.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "minor_mechanics.fire_protection_on_any_item", "Fire Protection On Any Item",
						"Fire Protection can be applied to any enchantable item rather than just " +
						"armor, and makes items enchanted with it immune to fire and lava damage.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "minor_mechanics.observers_see_entities", "Observers See Entities",
						"Observers detect when entities move in front of them if they have\n" +
						"no block in front of them. Not as laggy as it sounds.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "minor_mechanics.observers_see_entities_living_only", "Observers See Entities - Living Only",
						"Observers only detect living entities, and not e.g. item entities. " +
						"Safety option to prevent breaking a variety of vanilla contraptions.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "minor_mechanics.exact_note_block_tuning", "Exact Note Block Tuning",
						"Right-clicking a note block with a stack of sticks sets its pitch to the " +
						"size of the stack minus one.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "minor_mechanics.note_block_notes", "Note Block Notes",
						"Tells you the note the note block has been tuned to when right-clicking " +
						"it above your hotbar.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "minor_mechanics.spiders_cant_climb_glazed_terracotta", "Spiders Can't Climb Glazed Terracotta",
						"Spiders can't climb Glazed Terracotta. Slime (the stickiest substance " +
						"known to Stevekind) can't stick to it, so why should spiders?", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "minor_mechanics.water_fills_on_break", "Water Fills On Break",
						"Water source blocks fill in broken blocks instead of air if there is " +
						"more water on its north, east, south, west, and top faces than there is " +
						"air on its north, east, south, and west faces. In case of a tie, air " +
						"wins. Makes terraforming lakes and building canals, etc much less " +
						"frustrating.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "minor_mechanics.invisibility_splash_on_inanimates", "Invisibility Splash On Inanimates",
						"Invisibility splash potions affect inanimates (minecarts, arrows, etc) " +
						"making them invisible. They will become visible again if they become wet.\n" +
						"See Fixes > Inanimates Can Be Invisible.\n" +
						"Interacts with Mechanics > Enhanced Moistness.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "minor_mechanics.fire_aspect_is_flint_and_steel", "Fire Aspect Is Flint And Steel",
						"Right-clicking a block with no action with a Fire Aspect tool " +
						"emulates a click with flint and steel, allowing you to light fires " +
						"and such with a Fire Aspect tool instead of having to carry around " +
						"flint and steel. ", y, mouseX, mouseY, false);
			} else if ("mechanics".equals(section)) {
				y = drawTrilean(matrices, "mechanics.enhanced_moistness", "Enhanced Moistness",
						"Entities are considered \"wet\" for 5 seconds after leaving a source of " +
						"wetness. Additionally, lingering or splash water bottles inflict " +
						"wetness. Also makes wet entities drip to show they're wet. Affects " +
						"various vanilla mechanics including fire and undead burning.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "mechanics.slowfall_splash_on_inanimates", "Slow Fall Splash On Inanimates",
						"Slow fall splash potions affect inanimates (minecarts, arrows, etc) " +
						"making them unaffected by gravity. They will become normally affected " +
						"again if they become wet. This is kind of overpowered.\n" +
						"Interacts with Enhanced Moistness.", y, mouseX, mouseY, false);
			} else if ("balance".equals(section)) {
				y = drawTrilean(matrices, "balance.faster_obsidian", "Faster Obsidian",
						"Makes obsidian break 3x faster. Needed on both sides to work properly."
						+ "Does not break vanilla clients when on the server, but when on the client, "
						+ "vanilla servers will think you're cheating. (And they won't be wrong.)", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "balance.disable_prior_work_penalty", "Disable Prior Work Penalty",
						"Disables the anvil prior work penalty when an item has been worked " +
						"multiple times. Makes non-Mending tools relevant.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "balance.soul_speed_doesnt_damage_boots", "Soul Speed Doesn't Damage Boots",
						"Makes running on soul blocks with Soul Speed not deal damage to your boots.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "balance.infinity_mending", "Infinity & Mending",
						"Makes Mending and Infinity compatible enchantments.\n"
						+ "§4Not enabled in the \"vienna\" profile.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "balance.hyperspeed_furnace_minecart", "Hyperspeed Furnace Minecart",
						"Make furnace minecarts very fast.\n" +
						"An attempt to make rail transport relevant again, as well as furnace " +
						"carts, in a world with ice roads, swimming, elytra, etc.\n" +
						"Warning: These carts are so fast that they sometimes fall off of track " +
						"corners. Make sure to surround track corners with blocks.\n" +
						"Note: All furnace minecart tweaks enable a mixin that overrides " +
						"multiple methods in the furnace minecart entity. If you have another " +
						"mod that changes furnace minecarts (wow!) then you'll need to disable " +
						"this.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "balance.tridents_accept_power", "Tridents Accept Power",
						"Allows tridents to accept the Power enchantment, increasing their ranged " +
						"damage. It's pitiful that tridents only deal as much damage as an " +
						"unenchanted bow and this cannot be improved at all other than via " +
						"Impaling, which is exclusive to aquatic mobs (not including Drowned).\n" +
						"Power is considered incompatible with Sharpness and Impaling.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "balance.tridents_accept_sharpness", "Tridents Accept Sharpness",
						"Allows tridents to accept the Sharpness enchantment, increasing their " +
						"melee damage. See Tridents Accept Power for justification.\n" +
						"Sharpness is considered incompatible with Power and Impaling.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "balance.bedrock_impaling", "Bedrock-Like Impaling",
						"Makes the Impaling enchantment act like it does in Bedrock Edition and " +
						"Combat Test 4. Namely, it deals bonus damage to anything that is in " +
						"water or rain (i.e. is wet), instead of only aquatic mobs.\n" +
						"Interacts with Mechanics > Enhanced Moistness.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "balance.environmentally_friendly_creepers", "Environmentally Friendly Creepers",
						"Creeper explosions deal entity damage, but not block damage, even " +
						"if mobGriefing is true.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "balance.anvil_damage_only_on_fall", "Anvil Damage Only On Fall",
						"Anvils only take damage when falling from a height rather than randomly " +
						"after being used.\n"
						+ "§4Not enabled in the \"vienna\" profile.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "balance.broken_tools_drop_components", "Broken Gear Drops Components",
						"Makes tools and armor drop 75% minus one of their original materials when " +
						"broken. Assumes vanilla material costs. Doesn't use loot tables because I " +
						"have never written a JSON file in my life. Broken Netherite gear " +
						"drops all 4 of its constituent scrap.", y, mouseX, mouseY, false);
			} else if ("weird_tweaks".equals(section)) {
				y = drawTrilean(matrices, "weird_tweaks.endermen_dont_squeal", "Endermen Don't Squeal",
						"Makes Endermen not make their growling or screeching sounds when angry.\n" +
						"On client, mutes the sounds for just you. This means angry endermen don't " +
						"make ambient sounds.\n" +
						"On server, replaces the angry ambient sound with the normal ambient sound " +
						"for everyone. The stare sound is client-sided, unfortunately.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "weird_tweaks.disable_equip_sound", "Disable Equip Sound",
						"Disables the unnecessary \"Gear equips\" sound that plays when your hands " +
						"change, and is often glitchily played every tick. Armor equip sounds and " +
						"other custom equip sounds remain unchanged. You won't even notice it's " +
						"gone.\n" +
						"On client, mutes it just for you.\n" +
						"On server, prevents the sound from playing at all for everyone.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "weird_tweaks.repelling_void", "Déjà Void (aka Repelling Void)",
						"Players falling into the void teleports them back to the last place they " +
						"were on the ground and deals 6 hearts of damage.", y, mouseX, mouseY, false);
			} else if ("pedantry".equals(section)) {
				y = drawTrilean(matrices, "pedantry.tnt_is_dynamite", "TNT Is Dynamite",
						"TNT is renamed to Dynamite and doesn't say TNT on it. TNT is more stable " +
						"than Minecraft's representation of it, and the texture is clearly " +
						"dynamite.\n" +
						"(Technically dynamite is made from nitroglycerin, but nitro is so " +
						"incredibly unstable that you would need to change a dozen different " +
						"mechanics to make it \"correct\".)\n" +
						"Gunpowder is renamed to Creeper Dust, because gunpowder is not that " +
						"explosive.\n"+
						"§6Reloads resource packs.", y, mouseX, mouseY, true);
				y = drawTrilean(matrices, "pedantry.oak_is_apple", "Oak Is Apple",
						"Oak trees become apple trees. Because oak trees do not grow apples.\n"+
						"§6Reloads resource packs.", y, mouseX, mouseY, true);
			} else if ("situational".equals(section)) {
				y = drawTrilean(matrices, "situational.all_damage_is_fatal", "All Damage Is Fatal",
						"Any amount of damage done to an entity is unconditionally fatal.", y, mouseX, mouseY, false);
				y = drawTrilean(matrices, "situational.weapons_accept_silk", "Weapons Accept Silk Touch",
						"Weapons can accept Silk Touch. Does nothing on its own, but datapacks " +
						"can use this for special drops. Also makes Silk Touch incompatible with " +
						"Looting.", y, mouseX, mouseY, false);
			}
		}
		GlStateManager.popMatrix();
	}

	private int drawTrilean(MatrixStack matrices, String key, String title, String desc, int y, float mouseX, float mouseY, boolean clientOnly) {
		boolean disabled = (configuringServer && clientOnly) || !isValid(key);
		boolean noUnset = key.startsWith("general.") || key.startsWith("situational.") || key.startsWith("pedantry.") || "tweaks.ghost_chest_woo_woo".equals(key);
		Trilean currentValue = noUnset ? (isEnabled(key) ? Trilean.TRUE : Trilean.FALSE) : getValue(key);
		boolean keyEnabled = isEnabled(key);
		Trilean prevValue = optionPreviousValues.getOrDefault(key, currentValue);
		int prevX = prevValue == Trilean.FALSE ? 0 : prevValue == Trilean.TRUE ? noUnset ? 23 : 30 : 15;
		int prevHue = prevValue == Trilean.FALSE ? 0 : prevValue == Trilean.TRUE ? 120 : 55;
		int curX = currentValue == Trilean.FALSE ? 0 : currentValue == Trilean.TRUE ? noUnset ? 23 : 30 : 15;
		int curHue = currentValue == Trilean.FALSE ? 0 : currentValue == Trilean.TRUE ? 120 : 55;
		float time = optionAnimationTime.getOrDefault(key, 0f);
		if (time > 0) {
			time -= client.getLastFrameDuration();
			if (time <= 0) {
				optionAnimationTime.remove(key);
				time = 0;
			} else {
				optionAnimationTime.put(key, time);
			}
		}
		float a = sCurve5((5-time)/5f);
		if (clientOnly) {
			fill(matrices, 133, y, 134+46, y+11, 0xFFFFAA00);
		} else {
			fill(matrices, 133, y, 134+46, y+11, 0xFFFFFFFF);
		}
		fill(matrices, 134, y+1, 134+45, y+10, 0x66000000);
		if (!noUnset) fill(matrices, 134+15, y+1, 134+15+15, y+10, 0x33000000);
		if (!disabled) {
			GlStateManager.pushMatrix();
			GlStateManager.translatef(134+(prevX+((curX-prevX)*a)), 0, 0);
			fill(matrices, 0, y+1, noUnset ? 22 : 15, y+10, MathHelper.hsvToRgb((prevHue+((curHue-prevHue)*a))/360f, 0.9f, 0.8f)|0xFF000000);
			if (!noUnset && a >= 1 && currentValue == Trilean.UNSET) {
				fill(matrices, keyEnabled ? 15 : -1, y+1, keyEnabled ? 16 : 0, y+10, MathHelper.hsvToRgb((keyEnabled ? 120 : 0)/360f, 0.9f, 0.8f)|0xFF000000);
			}
			GlStateManager.popMatrix();
		}
		GlStateManager.enableBlend();
		RenderSystem.defaultBlendFunc();
		client.getTextureManager().bindTexture(new Identifier("fabrication", "trilean.png"));
		GlStateManager.color4f(1, 1, 1, disabled ? 0.3f : 1);
		GlStateManager.enableTexture();
		if (noUnset) {
			drawTexture(matrices, 134+3, y+1, 0, 0, 15, 9, 45, 9);
			drawTexture(matrices, 134+4+22, y+1, 30, 0, 15, 9, 45, 9);
		} else {
			drawTexture(matrices, 134, y+1, 0, 0, 45, 9, 45, 9);
		}
		GlStateManager.disableTexture();
		if (didClick) {
			if (lastClickX >= 134 && lastClickX <= 134+45 && lastClickY >= y+1 && lastClickY <= y+10) {
				if (disabled) {
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO, 0.8f, 1));
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO, 0.7f, 1));
					tooltipBlinkTicks = 20;
				} else {
					int clickedIndex = (int)((lastClickX-134)/(noUnset ? 22 : 15));
					Trilean newValue = clickedIndex == 0 ? Trilean.FALSE : clickedIndex == 1 && !noUnset ? Trilean.UNSET : Trilean.TRUE;
					if (newValue != currentValue) {
						client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE, 0.7f+((clickedIndex*(noUnset?2:1))*0.27f), 1f));
						optionPreviousValues.put(key, currentValue);
						optionAnimationTime.compute(key, (k, f) -> 5 - (f == null ? 0 : f));
						setValue(key, newValue.toString().toLowerCase(Locale.ROOT));
					}
				}
			}
		}
		int startX = 136+50;
		int endX = textRenderer.draw(matrices, title, startX, y+2, disabled ? 0x88FFFFFF : 0xFFFFFFFF);
		if (mouseX >= startX && mouseX <= endX && mouseY >= y && mouseY <= y+10) {
			renderOrderedTooltip(matrices, textRenderer.wrapLines(new LiteralText((clientOnly ? "§6Client Only§r\n" : "")+desc), width/3), (int)(mouseX+10), (int)(20+mouseY));
		} else if (mouseX >= 134 && mouseX <= 134+45 && mouseY >= y && mouseY <= y+10) {
			if (disabled) {
				renderTooltip(matrices, new LiteralText(((tooltipBlinkTicks/5)%2 == 1 ? "§c" : "")+"The server does not recognize this option"), (int)mouseX, (int)mouseY);
			} else {
				int index = (int)((mouseX-134)/(noUnset ? 22 : 15));
				if (index == 0) {
					renderTooltip(matrices, new LiteralText("§cDisable"), (int)mouseX, (int)mouseY);
				} else if (index == 1 && !noUnset) {
					if (currentValue == Trilean.UNSET) {
						renderTooltip(matrices, Lists.newArrayList(
								new LiteralText("§eUse default value §f(see General > Profile)"),
								new LiteralText("§rCurrent default: "+(keyEnabled ? "§aEnabled" : "§cDisabled"))
						), (int)mouseX, (int)mouseY);
					} else {
						renderTooltip(matrices, new LiteralText("§eUse default value §f(see General > Profile)"), (int)mouseX, (int)mouseY);
					}
				} else {
					renderTooltip(matrices, new LiteralText("§aEnable"), (int)mouseX, (int)mouseY);
				}
			}
		}
		return y+14;
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
	
	private void color(int packed) {
		color(packed, ((packed>>24)&0xFF)/255f);
	}
	
	private void color(int packed, float alpha) {
		GlStateManager.color4f(((packed>>16)&0xFF)/255f, ((packed>>8)&0xFF)/255f, ((packed>>0)&0xFF)/255f, alpha);
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
				whyCantConfigureServer = "You don't have permission to configure Fabrication.";
			} else {
				ClientPlayNetworkHandler cpnh = client.getNetworkHandler();
				if (cpnh instanceof GetServerConfig) {
					GetServerConfig gsc = (GetServerConfig)cpnh;
					if (!gsc.fabrication$hasHandshook()) {
						whyCantConfigureServer = "This server's version of Fabrication is too old.";
					} else {
						serverKnownConfigKeys.clear();
						serverKnownConfigKeys.addAll(gsc.fabrication$getServerTrileanConfig().keySet());
						serverKnownConfigKeys.addAll(gsc.fabrication$getServerStringConfig().keySet());
					}
				} else {
					whyCantConfigureServer = "An internal error prevented initialization of the syncer.";
				}
			}
		}
		addButton(new ColorButtonWidget(width-100, height-20, 100, 20, 0x66000000, new LiteralText("Done"), (w) -> {
			onClose();
		}));
	}
	
	@Override
	public void onClose() {
		if (!MixinConfigPlugin.isEnabled("*.reduced_motion")) {
			leaving = true;
			client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_BARREL_CLOSE, 0.7f));
			client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_SHROOMLIGHT_PLACE, 2f, 1f));
		} else {
			client.openScreen(parent);
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		if (sidebarHeight < height) return;
		lastSidebarScroll = sidebarScroll;
		sidebarScroll += sidebarScrollVelocity;
		sidebarScrollVelocity *= MixinConfigPlugin.isEnabled("*.reduced_motion") ? 0.45f : 0.75f;
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
		if (tooltipBlinkTicks > 0) {
			tooltipBlinkTicks--;
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
		if (button == 0) {
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
						tooltipBlinkTicks = 20;
					}
				}
			}
			didClick = true;
			lastClickX = (float)mouseX;
			lastClickY = (float)mouseY;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	private boolean isValid(String key) {
		if (configuringServer) {
			return ((GetServerConfig)client.getNetworkHandler()).fabrication$getServerTrileanConfig().containsKey(key) ||
					((GetServerConfig)client.getNetworkHandler()).fabrication$getServerStringConfig().containsKey(key);
		} else {
			return MixinConfigPlugin.isValid(key);
		}
	}
	
	private boolean isTrilean(String key) {
		if (configuringServer) {
			return ((GetServerConfig)client.getNetworkHandler()).fabrication$getServerTrileanConfig().containsKey(key);
		} else {
			return MixinConfigPlugin.isTrilean(key);
		}
	}
	
	private ResolvedTrilean getResolvedValue(String key) {
		if (configuringServer) {
			return ((GetServerConfig)client.getNetworkHandler()).fabrication$getServerTrileanConfig().get(key);
		} else {
			return MixinConfigPlugin.getResolvedValue(key);
		}
	}
	
	private Trilean getValue(String key) {
		return getResolvedValue(key).trilean;
	}
	
	private boolean isEnabled(String key) {
		return getResolvedValue(key).value;
	}
	
	private String getRawValue(String key) {
		if (configuringServer) {
			if (isTrilean(key)) {
				return getValue(key).toString().toLowerCase(Locale.ROOT);
			} else {
				return ((GetServerConfig)client.getNetworkHandler()).fabrication$getServerStringConfig().get(key);
			}
		} else {
			return MixinConfigPlugin.getRawValue(key);
		}
	}
	
	private void setValue(String key, String value) {
		if (configuringServer) {
			PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
			data.writeVarInt(1);
			data.writeString(key);
			data.writeString(value);
			client.getNetworkHandler().sendPacket(new CustomPayloadC2SPacket(new Identifier("fabrication", "config"), data));
		} else {
			MixinConfigPlugin.set(key, value);
			if (FabricationMod.isAvailableFeature(key)) {
				FabricationMod.updateFeature(key);
			}
		}
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
			if (bufferTooltips) {
				final int yf = y;
				bufferedTooltips.add(() -> renderOrderedTooltip(matrices, lines, x, yf));
				return;
			}
			if (y < 20) {
				y += 20;
			}
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
				totalHeight += /*2 +*/ (lines.size() - 1) * 10;
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
//				if (i == 0) {
//					innerY += 2;
//				}
				innerY += 10;
			}

			vcp.draw();
			matrices.pop();
		}
	}

	
}
