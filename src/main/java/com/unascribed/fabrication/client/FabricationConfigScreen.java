package com.unascribed.fabrication.client;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.CommandDispatcher;
import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.interfaces.GetServerConfig;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.MixinConfigPlugin.Profile;
import com.unascribed.fabrication.support.ResolvedTrilean;
import com.unascribed.fabrication.support.Trilean;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
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
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;

import static com.unascribed.fabrication.client.FabricationConfigScreen.TrileanFlag.*;

public class FabricationConfigScreen extends Screen {

	public enum TrileanFlag {
		CLIENT_ONLY, REQUIRES_FABRIC_API
	}

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
			.put("woina", "Forward ports of\nforgotten tidbits.")
			.put("experiments", "Bad ideas given\nform.")
			.build();
	
	private static final ImmutableMap<Profile, String> PROFILE_DESCRIPTIONS = ImmutableMap.<Profile, String>builder()
			.put(Profile.GREEN, "Enables nothing. Build your own config.")
			.put(Profile.BLONDE, "Enables Fixes and Utility. The bare minimum.\nYou like vanilla.")
			.put(Profile.LIGHT, "Blonde + Tweaks. Default.")
			.put(Profile.MEDIUM, "Light + Minor Mechanics.")
			.put(Profile.DARK, "Medium + Mechanics. Recommended.")
			.put(Profile.VIENNA, "Dark + Balance + Weird Tweaks + W.O.I.N.A.\nYou agree with all of Una's opinions.")
			.put(Profile.BURNT, "Screw it, enable everything.\n(Except Situational and Experiments.)")
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
	private static final Identifier PRIDETEX = new Identifier("fabrication", "pride.png");
	
	private static final boolean PRIDE = Calendar.getInstance().get(Calendar.MONTH) == Calendar.JUNE || Boolean.getBoolean("com.unascribed.fabrication.everyMonthIsPrideMonth") || Boolean.getBoolean("fabrication.everyMonthIsPrideMonth");
	
	private static long serverLaunchId = -1;
	
	private static final Set<String> newlyFalseKeysClient = Sets.newHashSet();
	private static final Set<String> newlyFalseKeysServer = Sets.newHashSet();
	
	private static final Set<String> newlyNotFalseKeysClient = Sets.newHashSet();
	private static final Set<String> newlyNotFalseKeysServer = Sets.newHashSet();
	
	private static final Map<String, String> changedKeysWithoutRuntimeChecksClient = Maps.newHashMap();
	private static final Map<String, String> changedKeysWithoutRuntimeChecksServer = Maps.newHashMap();
	
	private static boolean runtimeChecksToggledClient;
	private static boolean runtimeChecksToggledServer;
	
	private final int random = ThreadLocalRandom.current().nextInt();
	
	private final Screen parent;
	
	private int realWidth;
	private int realHeight;
	private double scaleCompensation = 1;
	
	private float timeExisted;
	private boolean leaving = false;
	private float timeLeaving;
	private float sidebarScrollVelocity;
	private float sidebarScroll;
	private float lastSidebarScroll;
	private float sidebarHeight;
	
	private boolean didClick;
	private float selectTime;
	private String selectedSection;
	private String prevSelectedSection;
	
	private int tooltipBlinkTicks = 0;
	
	private boolean configuringServer;
	private boolean hasClonked = true;
	private boolean isSingleplayer;
	private float serverAnimateTime;
	private String whyCantConfigureServer = null;
	private Set<String> serverKnownConfigKeys = Sets.newHashSet();
	private boolean serverReadOnly;
	
	private final Map<String, Trilean> optionPreviousValues = Maps.newHashMap();
	private final Map<String, Float> optionAnimationTime = Maps.newHashMap();
	private final Map<String, Float> disabledAnimationTime = Maps.newHashMap();
	private final Set<String> knownDisabled = Sets.newHashSet();
	
	private boolean bufferTooltips = false;
	private final List<Runnable> bufferedTooltips = Lists.newArrayList();
	
	private int noteIndex = 0;
	
	public FabricationConfigScreen(Screen parent) {
		super(new LiteralText("Fabrication configuration"));
		this.parent = parent;
	}
	
	@Override
	public void init(MinecraftClient client, int width, int height) {
		super.init(client, width, height);
		realWidth = width;
		realHeight = height;
		float ratio = width/(float)height;
		if (ratio < 13.5/9f) {
			double scaleFactor = client.getWindow().getScaleFactor();
			if (scaleFactor <= 1) {
				// nothing we can do...
				scaleCompensation = 1;
				return;
			}
			double newScaleFactor = client.getWindow().getScaleFactor()-1;
			this.width = (int)(client.getWindow().getFramebufferWidth()/newScaleFactor);
			this.height = (int)(client.getWindow().getFramebufferHeight()/newScaleFactor);
			scaleCompensation = newScaleFactor/scaleFactor;
		} else {
			scaleCompensation = 1;
		}
	}
	
	@Override
	protected void init() {
		super.init();
		isSingleplayer = false;
		if (client.world == null) {
			whyCantConfigureServer = "You're not connected to a server.";
		} else if (client.getServer() != null) {
			whyCantConfigureServer = "The singleplayer server shares the client settings.";
			isSingleplayer = true;
		} else {
			CommandDispatcher<?> disp = client.player.networkHandler.getCommandDispatcher();
			if (disp.getRoot().getChild("fabrication") == null) {
				whyCantConfigureServer = "This server doesn't have Fabrication.";
			} else {
				ClientPlayNetworkHandler cpnh = client.getNetworkHandler();
				if (cpnh instanceof GetServerConfig) {
					GetServerConfig gsc = (GetServerConfig)cpnh;
					if (!gsc.fabrication$hasHandshook()) {
						whyCantConfigureServer = "This server's version of Fabrication is too old.";
					} else {
						serverReadOnly = (disp.getRoot().getChild("fabrication").getChild("config") == null);
						serverKnownConfigKeys.clear();
						serverKnownConfigKeys.addAll(gsc.fabrication$getServerTrileanConfig().keySet());
						serverKnownConfigKeys.addAll(gsc.fabrication$getServerStringConfig().keySet());
					}
				} else {
					whyCantConfigureServer = "An internal error prevented initialization of the syncer.";
				}
			}
		}
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
//				GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT, false);
//				GlStateManager.translatef(realWidth/2f, realHeight/2f, 0);
//				GlStateManager.scalef(0.5f, 0.5f, 0.5f);
//				GlStateManager.translatef(-realWidth/2f, -realHeight/2f, 0);
				GlStateManager.translatef(realWidth/2f, realHeight, 0);
				GlStateManager.rotatef(a*(leaving ? -180 : 180), 0, 0, 1);
				GlStateManager.translatef(-realWidth/2, -realHeight, 0);
				GlStateManager.pushMatrix();
					GlStateManager.translatef(0, realHeight, 0);
					GlStateManager.translatef(realWidth/2f, realHeight/2f, 0);
					GlStateManager.rotatef(180, 0, 0, 1);
					GlStateManager.translatef(-realWidth/2f, -realHeight/2f, 0);
					fill(matrices, -realWidth, -realHeight, realWidth*2, 0, 0xFF2196F3);
					GlStateManager.pushMatrix();
						GlStateManager.scaled(scaleCompensation, scaleCompensation, 1);
						drawBackground(matrices, -200, -200, delta);
						drawForeground(matrices, -200, -200, delta);
					GlStateManager.popMatrix();
				GlStateManager.popMatrix();
				for (int x = -1; x <= 1; x++) {
					for (int y = -1; y <= 0; y++) {
						if (x == 0 && y == 0) continue;
						GlStateManager.pushMatrix();
						GlStateManager.translatef(realWidth*x, realHeight*y, 0);
						parent.renderBackgroundTexture(0);
						GlStateManager.popMatrix();
					}
				}
				parent.render(matrices, -200, -200, delta);
			GlStateManager.popMatrix();
		} else {
			GlStateManager.pushMatrix();
			GlStateManager.scaled(scaleCompensation, scaleCompensation, 1);
			drawBackground(matrices, (int)(mouseX/scaleCompensation), (int)(mouseY/scaleCompensation), delta);
			drawForeground(matrices, (int)(mouseX/scaleCompensation), (int)(mouseY/scaleCompensation), delta);
			GlStateManager.popMatrix();
		}
		if (leaving && timeLeaving > 10) {
			client.openScreen(parent);
		}
	}
	
	private void drawBackground(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		fillGradient(matrices, -width, 0, width*2, height, 0xFF2196F3, 0xFF009688);
		float ratio = 502/1080f;
		
		float w = height*ratio;
		float brk = Math.min(width-w, (width*2/3f)-(w/3));
		float brk2 = brk+w;
		float border = (float)(20/(client.getWindow().getScaleFactor()*scaleCompensation));
		
		Matrix4f mat = matrices.peek().getModel();
		
		GlStateManager.enableBlend();
		RenderSystem.defaultBlendFunc();
		float time = selectedSection == null ? 10-selectTime : prevSelectedSection == null ? selectTime : 0;
		GlStateManager.color4f(1, 1, 1, 1);
		
		GlStateManager.disableCull();
		BufferBuilder bb = Tessellator.getInstance().getBuffer();

		float top = (570/1080f)*height;
		float bottom = (901/1080f)*height;
		if (PRIDE) {
			client.getTextureManager().bindTexture(PRIDETEX);
			int flags = 21;
			int flag = Math.abs(random)%flags;
			float minU = (flag/(float)flags)+(0.5f/flags);
			float maxU = (flag/(float)flags)+(0.75f/flags);
			bb.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
			bb.vertex(mat, brk, top, 0).texture(minU, 0).next();
			bb.vertex(mat, brk2, top, 0).texture(maxU, 0).next();
			bb.vertex(mat, brk2, bottom, 0).texture(maxU, 1).next();
			bb.vertex(mat, brk, bottom, 0).texture(minU, 1).next();
			bb.end();
			BufferRenderer.draw(bb);
		} else {
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			GlStateManager.disableTexture();
			bb.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
			bb.vertex(mat, brk, top, 0).color(0.298f, 0.686f, 0.314f, 1).next();
			bb.vertex(mat, brk2, top, 0).color(0.298f, 0.686f, 0.314f, 1).next();
			bb.vertex(mat, brk2, bottom, 0).color(0.475f, 0.333f, 0.282f, 1).next();
			bb.vertex(mat, brk, bottom, 0).color(0.475f, 0.333f, 0.282f, 1).next();
			bb.end();
			BufferRenderer.draw(bb);
			GlStateManager.enableTexture();
			GlStateManager.shadeModel(GL11.GL_FLAT);
		}
		
		client.getTextureManager().bindTexture(BG);
		GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
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
		BufferRenderer.draw(bb);
		
		float a = 1-(0.3f+(sCurve5(time/10f)*0.7f));
		if (a > 0) {
			int ai = ((int)(a*255))<<24;
			fillGradient(matrices, -width, 0, width*2, height, 0x2196F3|ai, 0x009688|ai);
		}
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
		GlStateManager.pushMatrix();
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
				GlStateManager.pushMatrix();
					GlStateManager.scalef((float)(1-(Math.abs(Math.sin(a*Math.PI))/2)), 1, 1);
					fill(matrices, -60, -8, 0, 8, MathHelper.hsvToRgb(h, 0.9f, 0.9f)|0xFF000000);
					if (isSingleplayer) {
						fill(matrices, 0, -8, 60, 8, MathHelper.hsvToRgb(0.833333f, 0.9f, 0.9f)|0xFF000000);
					}
				GlStateManager.popMatrix();
				GlStateManager.pushMatrix();
					GlStateManager.rotatef(45, 0, 0, 1);
					// 8 / sqrt(2)
					float f = 5.6568542f;
					GlStateManager.scalef(f, f, 1);
					fill(matrices, -1, -1, 1, 1, 0xFFFFFFFF);
				GlStateManager.popMatrix();
				if (!isSingleplayer) {
					fill(matrices, -6, -1, -2, 1, 0xFF000000);
				}
			GlStateManager.popMatrix();
			fill(matrices, -2, -2, 2, 2, 0xFF000000);
		GlStateManager.popMatrix();
		
		textRenderer.draw(matrices, "CLIENT", width-115, 4, 0xFF000000);
		textRenderer.draw(matrices, "SERVER", width-40, 4, whyCantConfigureServer == null || isSingleplayer ? 0xFF000000 : 0x44000000);
		if (serverReadOnly && whyCantConfigureServer == null) {
			client.getTextureManager().bindTexture(new Identifier("fabrication", "lock.png"));
			GlStateManager.color4f(0, 0, 0, 1);
			drawTexture(matrices, width-49, 3, 0, 0, 0, 8, 8, 8, 8);
		}
		
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
		float selectedChoiceY = -60;
		float prevSelectedChoiceY = -60;
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
				textRenderer.draw(matrices, "§l"+("woina".equals(s) ? "W.O.I.N.A." : formatTitleCase(s)), 4, y, -1);
			}
			String desc = SECTION_DESCRIPTIONS.getOrDefault(s, "No description available");
			y += 12;
			newHeight += 12;
			int textHeight = drawWrappedText(matrices, 8, y, desc, 116, -1, false);
			y += textHeight;
			newHeight += textHeight;
			if (didClick) {
				if (mouseX >= 0 && mouseX <= 130 && mouseY > startY-4 && mouseY < y) {
					boolean deselect = s.equals(selectedSection);
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_BELL, deselect ? 0.5f : 0.6f+(i*0.1f), 1f));
					prevSelectedSection = selectedSection;
					selectedSection = deselect ? null : s;
					selectTime = 10-selectTime;
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
		if (!MixinConfigPlugin.isEnabled("general.reduced_motion") && !Objects.equal(selectedSection, prevSelectedSection)) {
			drawSection(matrices, prevSelectedSection, -200, -200, prevSelectedChoiceY, sCurve5(selectTime/10f));
		}
		
		
		List<String> notes = Lists.newArrayList();
		
		Set<String> newlyFalseKeys;
		Set<String> newlyNotFalseKeys;
		Map<String, String> changedKeysWithoutRuntimeChecks;
		
		boolean runtimeChecksToggled;
		boolean hasYellowNote = false;
		boolean hasRedNote = false;
		
		if (configuringServer) {
			checkServerData();
			newlyFalseKeys = newlyFalseKeysServer;
			newlyNotFalseKeys = newlyNotFalseKeysServer;
			changedKeysWithoutRuntimeChecks = changedKeysWithoutRuntimeChecksServer;
			runtimeChecksToggled = runtimeChecksToggledServer;
		} else {
			newlyFalseKeys = newlyFalseKeysClient;
			newlyNotFalseKeys = newlyNotFalseKeysClient;
			changedKeysWithoutRuntimeChecks = changedKeysWithoutRuntimeChecksClient;
			runtimeChecksToggled = runtimeChecksToggledClient;
		}
		if (!changedKeysWithoutRuntimeChecks.isEmpty()) {
			notes.add("§c"+changedKeysWithoutRuntimeChecks.size()+" change"+(changedKeysWithoutRuntimeChecks.size() == 1 ? "" : "s")
					+" made while Runtime Checks\n§cis disabled will not be applied until\n§cthe {} is restarted.");
			hasRedNote = true;
		}
		if (!newlyNotFalseKeys.isEmpty()) {
			notes.add("§c"+newlyNotFalseKeys.size()+" newly undisabled option"+(newlyNotFalseKeys.size() == 1 ? "" : "s")+" will\n§cnot activate until the {} is\n§crestarted.");
			hasRedNote = true;
		}
		if (runtimeChecksToggled) {
			notes.add("§eThe {} must be restarted for\n§echanges to Runtime Checks to apply.");
			hasYellowNote = true;
		}
		if (!newlyFalseKeys.isEmpty() && (isEnabled("general.runtime_checks") && !runtimeChecksToggled)) {
			notes.add(newlyFalseKeys.size()+" newly disabled option"+(newlyFalseKeys.size() == 1 ? "" : "s")+" will be\nentirely unloaded when the {} is\nrestarted.");
		}
		if (noteIndex < 0) {
			noteIndex = 0;
		}
		if (noteIndex >= notes.size()) {
			noteIndex = 0;
		}
		int textHeight = drawWrappedText(matrices, 136, height,
				(hasRedNote ? "§c\u26A0 " : hasYellowNote ? "§e" : "")+notes.size()+" note"+(notes.size() == 1 ? "" : "s")+
				(notes.isEmpty() ? " ☺" : " - hover to see "+(notes.size() == 1 ? "it" : "them")), width-250, -1, true);
		if (mouseX >= 136 && mouseX <= width-100 && mouseY >= height-textHeight) {
			if (!notes.isEmpty()) {
				List<Text> lines = Lists.newArrayList();
				for (String s : notes.get(noteIndex).replace("{}", configuringServer ? "server" : "client").split("\n")) {
					lines.add(new LiteralText(s));
				}
				if (notes.size() > 1) {
					lines.add(new LiteralText("§7Click to see other notes"));
				}
				renderTooltip(matrices, lines, mouseX, mouseY);
				if (didClick && notes.size() > 1) {
					noteIndex++;
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_LOOM_SELECT_PATTERN, 1f));
				}
			}
		}
		
		if (drawButton(matrices, width-100, height-20, 100, 20, "Done", mouseX, mouseY)) {
			onClose();
		}
		if (didClick) didClick = false;
		
		super.render(matrices, mouseX, mouseY, delta);
		
		bufferTooltips = false;
		for (Runnable r : bufferedTooltips) {
			r.run();
		}
		bufferedTooltips.clear();
		
		if (mouseX > width-120 && mouseY < 16) {
			String msg;
			if (whyCantConfigureServer != null) {
				msg = ((tooltipBlinkTicks/5)%2 == 1 ? "§c" : "§e")+whyCantConfigureServer;
			} else {
				int srv = serverKnownConfigKeys.size();
				int cli = MixinConfigPlugin.getAllKeys().size();
				msg = "§dServer has Fabrication and is recognized.";
				if (srv != cli) {
					msg += "\n§oMismatch: Server has "+srv+" options. Client has "+cli+".";
					if (srv > cli) {
						msg += "\n§cOptions unknown to the client will not appear.";
					} else if (cli > srv) {
						msg += "\n§eOptions unknown to the server will be disabled.";
					}
				}
			}
			if (serverReadOnly) {
				msg += "\n§fYou cannot configure this server.";
				if (configuringServer) {
					msg += "\n§fChanges cannot be made.";
				}
			}
			if (!isSingleplayer && (!serverReadOnly || !configuringServer)) {
				msg += "\n§fChanges will apply to the "+(configuringServer ? "§dSERVER" : "§6CLIENT")+"§f.";
			}
			renderTooltip(matrices, Lists.transform(Lists.newArrayList(msg.split("\n")),
					s -> new LiteralText(s)), mouseX+10, 20+mouseY);
		}
		GlStateManager.popMatrix();
	}
	
	private void checkServerData() {
		ClientPlayNetworkHandler cpnh = client.getNetworkHandler();
		if (cpnh != null && cpnh instanceof GetServerConfig) {
			long launchId = ((GetServerConfig)cpnh).fabrication$getLaunchId();
			if (launchId != serverLaunchId) {
				newlyFalseKeysServer.clear();
				newlyNotFalseKeysServer.clear();
				changedKeysWithoutRuntimeChecksServer.clear();
				runtimeChecksToggledServer = false;
				serverLaunchId = launchId;
			}
		}
	}

	private int drawWrappedText(MatrixStack matrices, float x, float y, String str, int width, int color, boolean fromBottom) {
		int height = 0;
		List<OrderedText> lines = textRenderer.wrapLines(new LiteralText(str), width);
		if (fromBottom) {
			y -= 12;
			lines = Lists.reverse(lines);
		}
		for (OrderedText ot : lines) {
			textRenderer.draw(matrices, ot, x, y, color);
			y += (fromBottom ? -12 : 12);
			height += 12;
		}
		return height;
	}

	private void drawSection(MatrixStack matrices, String section, float mouseX, float mouseY, float choiceY, float a) {
		if (a <= 0) return;
		if (MixinConfigPlugin.isEnabled("general.reduced_motion")) {
			a = 1;
		}
		// jesus fucking christ
		GlStateManager.pushMatrix();
		GlStateManager.translatef(60, choiceY+16, 0);
		GlStateManager.scalef(a, a, 1);
		GlStateManager.translatef(-60, -(choiceY+16), 0);
		int y = 16;
		if (section == null) {
			String v = Agnos.INST.getModVersion();
			String blurb = "§lFabrication v"+v+" §rby unascribed\n"
					+ "Click a category on the left to change settings.\n\n"
					+ "Detail offered by tooltips here is somewhat sparse. For additional detail and demonstration videos, please check the wiki.";
			int height = drawWrappedText(matrices, 140, 20, blurb, width-130, -1, false);
			if (drawButton(matrices, 140, 20+height+8, 120, 20, "Take me to the wiki", mouseX, mouseY)) {
				Util.getOperatingSystem().open("https://github.com/unascribed/Fabrication/wiki");
			}
			if (drawButton(matrices, 140, 20+height+32, 120, 20, "Reload files", mouseX, mouseY)) {
				MixinConfigPlugin.reload();
			}
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
					if (didClick && mouseX >= 134+x && mouseX <= 134+x+16 && mouseY >= 18 && mouseY <= 18+16) {
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
						"Allows changing settings arbitrarily on-the-fly, but is very slightly " +
						"slower and a fair bit less compatible, as all options that are set to " +
						"'unset' but not enabled by the profile will be initialized.\n" +
						"You can still disable something completely if it's causing problems by setting it " +
						"to false explicitly.\n" +
						"§cIf this is disabled, you must restart the game for changes made here to apply.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "general.reduced_motion", "Reduced Motion",
						"Disable high-motion GUI animations.", y, mouseX, mouseY, CLIENT_ONLY);
			} else if ("fixes".equals(section)) {
				y = drawTrilean(matrices, "fixes.sync_attacker_yaw", "Sync Attacker Yaw",
						"Makes the last attacker yaw field sync properly when the player is damaged, instead "
						+ "of always being zero. Causes the camera shake animation when being hurt to tilt "
						+ "away from the source of damage.\n"
						+ "Fixes MC-26678, which is closed as Won't Fix.\n"
						+ "Needed on both client and server, but doesn't break vanilla clients.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "fixes.furnace_minecart_pushing", "Furnace Minecart Pushing",
						"Right-clicking a furnace minecart with a non-fuel while it's out of " +
						"fuel gives it a little bit of fuel, allowing you to \"push\" it. " +
						"Removed some time after 17w46a (1.13 pre releases)\n" +
						"Unnecessary on client.\n" +
						"Note: All furnace minecart tweaks enable a mixin that overrides " +
						"multiple methods in the furnace minecart entity. If you have another " +
						"mod that changes furnace minecarts (wow!) then you'll need to disable\n" +
						"this.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "fixes.use_player_list_name_in_tag", "Use Player List Name In Tag",
						"Changes player name tags to match names in the player list. "
						+ "Good in combination with nickname mods like Drogtor.", y, mouseX, mouseY, CLIENT_ONLY);
				y = drawTrilean(matrices, "fixes.better_pause_freezing", "Better Pause Freezing",
						"Makes textures not tick while the game is paused.\n" +
						"May do more in the future.", y, mouseX, mouseY, CLIENT_ONLY);
				y = drawTrilean(matrices, "fixes.inanimates_can_be_invisible", "Inanimates Can Be Invisible",
						"Prevents inanimate entities from rendering at all if their \"invisible\" " +
						"flag is set to true.", y, mouseX, mouseY, CLIENT_ONLY);
				y = drawTrilean(matrices, "fixes.omniscent_player", "Omniscent Player",
						"The player render in the inventory follows your cursor, even if it's not" +
						"inside the game window.", y, mouseX, mouseY, CLIENT_ONLY);
				y = drawTrilean(matrices, "fixes.uncap_menu_fps", "Uncap Menu FPS",
						"Allow the menu to render at your set frame limit instead of being " +
						"locked to 60. (30 in older versions)", y, mouseX, mouseY, CLIENT_ONLY);
				y = drawTrilean(matrices, "fixes.adventure_tags_in_survival", "Adventure Tags In Survival",
						"Makes the CanDestroy and CanPlaceOn tags be honored in survival mode " +
						"instead of just adventure mode.\n" +
						"Only needed on the server, but the experience is more seamless if it's also on the client.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "fixes.stable_cacti", "Stable Cacti",
						"Fixes cactuses being made of Explodium due to long-since-fixed engine " +
						"limitations. In English: Makes cacti not break themselves if a block " +
						"is placed next to them. They will still break if they *grow* into such " +
						"a space, so cactus randomizers and cactus farms still work.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "fixes.boundless_levels", "Boundless Levels",
						"Replaces translation strings for potion and enchantment levels with a "
						+ "dynamic algorithm that supports arbitrarily large numbers.", y, mouseX, mouseY, CLIENT_ONLY);
			} else if ("utility".equals(section)) {
				y = drawTrilean(matrices, "utility.mods_command", "/mods command",
						"Adds a /mods command that anyone can run that lists installed mods. Lets " +
						"players see what changes are present on a server at a glance.", y, mouseX, mouseY, REQUIRES_FABRIC_API);
				y = drawTrilean(matrices, "utility.taggable_players", "Taggable Players",
						"Allows players to be tagged by ops with /fabrication tag. Allows " +
						"making them not need to eat food, not be targeted by mobs, have " +
						"permanent dolphin's grace or conduit power, able to breathe water, " +
						"fireproof, scare creepers, or not have phantoms spawn.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "utility.legacy_command_syntax", "Legacy Command Syntax",
						"Re-adds /toggledownfall and numeric arguments to /difficulty and "
						+ "/gamemode, capitalized arguments to /summon, and numeric arguments "
						+ "to /give and other commands that accept items.", y, mouseX, mouseY, REQUIRES_FABRIC_API);
				y = drawTrilean(matrices, "utility.books_show_enchants", "Books Show Enchants",
						"Makes enchanted books show the first letter of their enchants in the" +
						"bottom left, cycling through enchants every second if they have multiple.", y, mouseX, mouseY, CLIENT_ONLY);
				y = drawTrilean(matrices, "utility.tools_show_important_enchant", "Tools Show Important Enchant",
						"Makes tools enchanted with Silk Touch, Fortune, or Riptide show " +
						"the first letter of that enchant in the top left.\n" +
						"Never break an Ender Chest with the wrong tool again.", y, mouseX, mouseY, CLIENT_ONLY);
				y = drawTrilean(matrices, "utility.despawning_items_blink", "Despawning Items Blink",
						"Items that are about to despawn blink.\n" +
						"Needed on both sides. Server sends packets, client actually does the blinking.\n" +
						"Will not break vanilla clients.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "utility.canhit", "CanHit",
						"Adds a CanHit tag that only allows hitting entities matching given filters. Works for " +
						"melee, bows, and tridents. Bows will also check the arrow they're firing " +
						"and will only allow hitting entities that are in the bow's CanHit list as " +
						"well as the arrow's.\n" +
						"Honors Fixes > Adventure Tags In Survival.\n" +
						"Only needed on server, but the experience is more seamless if it's " +
						"also on the client.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "utility.item_despawn", "Item Despawn Control",
						"Allows fine-tuned adjustment of item despawn times.\n" +
						"See fabrication/item_despawn.ini.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "utility.i_and_more", "/i And More",
						"Adds /i, /item, /more, and /fenchant commands.\n"
						+ "/i and /item are shorthand for /give to yourself, and /more increases "
						+ "the size of your held item's stack. /fenchant is like /enchant but it "
						+ "ignores all restrictions.", y, mouseX, mouseY, REQUIRES_FABRIC_API);
			} else if ("tweaks".equals(section)) {
				y = drawTrilean(matrices, "tweaks.creepers_explode_when_on_fire", "Creepers Explode When On Fire",
						"Causes creepers to light their fuses when lit on fire. Just because.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "tweaks.tridents_in_void_return", "Tridents In Void Return",
						"Makes Loyalty tridents immune to void damage, and causes them to start " +
						"their return timer upon falling into the void.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "tweaks.less_annoying_fire", "Less Annoying Fire",
						"Makes the \"on fire\" overlay half as tall, and removes it completely if " +
						"you have Fire Resistance.", y, mouseX, mouseY, CLIENT_ONLY);
				y = drawTrilean(matrices, "tweaks.less_restrictive_note_blocks", "Less Restrictive Note Blocks",
						"Allows note blocks to play if any block next to them has a nonsolid " +
						"face, instead of only if the block above is air.\n" +
						"On the client, just adjusts the note particle to fly the right direction.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "tweaks.cactus_walk_doesnt_hurt_with_boots", "Cactus Walk Doesn't Hurt With Boots",
						"Makes walking on top of a cactus (not touching the side of one) with " +
						"boots equipped not deal damage.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "tweaks.cactus_brush_doesnt_hurt_with_chest", "Cactus Brush Doesn't Hurt With Chest",
						"Makes touching the side of a cactus (not walking on top of one) with " +
						"a chestplate equipped not deal damage.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "tweaks.bush_walk_doesnt_hurt_with_armor", "Bush Walk Doesn't Hurt With Armor",
						"Makes walking through berry bushes with both leggings and boots\n" +
						"equipped not deal damage.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "tweaks.shulker_bullets_despawn_on_death", "Shulker Bullets Despawn On Death",
						"Makes shulker bullets despawn when the shulker that shot them is killed.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "tweaks.arrows_work_in_water", "Arrows Work In Water",
						"Makes arrows viable in water by reducing their drag. Nowhere near as\n" +
						"good as a trident, but usable.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "tweaks.reverse_note_block_tuning", "Reverse Note Block Tuning",
						"Sneaking while tuning a note block reduces its pitch rather than increases.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "tweaks.campfires_place_unlit", "Campfires Place Unlit",
						"Campfires are unlit when placed and must be lit.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "tweaks.rainbow_experience", "Rainbow Experience",
						"Makes experience rainbow instead of just lime green. "+
						"Every orb picks two random colors to pulse between when spawning.", y, mouseX, mouseY, CLIENT_ONLY);
				y = drawTrilean(matrices, "tweaks.long_levelup_sound_at_30", "Long Level Up Sound At 30",
						"Plays the old longer level up sound when you hit level 30.", y, mouseX, mouseY, CLIENT_ONLY, REQUIRES_FABRIC_API);
				y = drawTrilean(matrices, "tweaks.ghost_chest_woo_woo", "Ghost Chest Woo Woo",
						"?", y, mouseX, mouseY, CLIENT_ONLY);
			} else if ("minor_mechanics".equals(section)) {
				y = drawTrilean(matrices, "minor_mechanics.feather_falling_five", "Feather Falling V",
						"Makes Feather Falling V a valid enchant that completely negates fall damage.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "minor_mechanics.feather_falling_five_damages_boots", "Feather Falling V Damages Boots",
						"Absorbing fall damage with Feather Falling V causes damage to the boots.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "minor_mechanics.furnace_minecart_any_fuel", "Furnace Minecart Any Fuel",
						"Allows furnace minecarts to accept any furnace fuel, rather than just " +
						"coal and charcoal.\n" +
						"Note: All furnace minecart tweaks enable a mixin that overrides " +
						"multiple methods in the furnace minecart entity. If you have another " +
						"mod that changes furnace minecarts (wow!) then you'll need to disable " +
						"this.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "minor_mechanics.infibows", "InfiBows (aka Bow Infinity Fix)",
						"Makes Infinity bows not require an arrow in your inventory to fire.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "minor_mechanics.note_blocks_play_on_landing", "Note Blocks Play On Landing",
						"Makes note blocks play their note when landed on. Also triggers observers.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "minor_mechanics.fire_protection_on_any_item", "Fire Protection On Any Item",
						"Fire Protection can be applied to any enchantable item rather than just " +
						"armor, and makes items enchanted with it immune to fire and lava damage.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "minor_mechanics.observers_see_entities", "Observers See Entities",
						"Observers detect when entities move in front of them if they have\n" +
						"no block in front of them. Not as laggy as it sounds.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "minor_mechanics.observers_see_entities_living_only", "Observers See Entities - Living Only",
						"Observers only detect living entities, and not e.g. item entities. " +
						"Safety option to prevent breaking a variety of vanilla contraptions.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "minor_mechanics.exact_note_block_tuning", "Exact Note Block Tuning",
						"Right-clicking a note block with a stack of sticks sets its pitch to the " +
						"size of the stack minus one.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "minor_mechanics.note_block_notes", "Note Block Notes",
						"Tells you the note the note block has been tuned to when right-clicking " +
						"it above your hotbar.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "minor_mechanics.spiders_cant_climb_glazed_terracotta", "Spiders Can't Climb Glazed Terracotta",
						"Spiders can't climb Glazed Terracotta. Slime (the stickiest substance " +
						"known to Stevekind) can't stick to it, so why should spiders?", y, mouseX, mouseY);
				y = drawTrilean(matrices, "minor_mechanics.spiders_cant_climb_while_wet", "Spiders Can't Climb While Wet",
						"Spiders can't climb while wet. Basically a much easier version of the " +
						"previous tweak, that is also a lot harder to control. May break vanilla " +
						"spider farms.\n" +
						"Not enabled by profiles other than Burnt.\n" +
						"Interacts with Mechanics > Enhanced Moistness.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "minor_mechanics.water_fills_on_break", "Water Fills On Break",
						"Water source blocks fill in broken blocks instead of air if there is " +
						"more water on its north, east, south, west, and top faces than there is " +
						"air on its north, east, south, and west faces. In case of a tie, air " +
						"wins. Makes terraforming lakes and building canals, etc much less " +
						"frustrating.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "minor_mechanics.invisibility_splash_on_inanimates", "Invisibility Splash On Inanimates",
						"Invisibility splash potions affect inanimates (minecarts, arrows, etc) " +
						"making them invisible. They will become visible again if they become wet.\n" +
						"See Fixes > Inanimates Can Be Invisible.\n" +
						"Interacts with Mechanics > Enhanced Moistness.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "minor_mechanics.fire_aspect_is_flint_and_steel", "Fire Aspect Is Flint And Steel",
						"Right-clicking a block with no action with a Fire Aspect tool " +
						"emulates a click with flint and steel, allowing you to light fires " +
						"and such with a Fire Aspect tool instead of having to carry around " +
						"flint and steel. ", y, mouseX, mouseY);
			} else if ("mechanics".equals(section)) {
				y = drawTrilean(matrices, "mechanics.enhanced_moistness", "Enhanced Moistness",
						"Entities are considered \"wet\" for 5 seconds after leaving a source of " +
						"wetness. Additionally, lingering or splash water bottles inflict " +
						"wetness. Also makes wet entities drip to show they're wet. Affects " +
						"various vanilla mechanics including fire and undead burning.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "mechanics.slowfall_splash_on_inanimates", "Slow Fall Splash On Inanimates",
						"Slow fall splash potions affect inanimates (minecarts, arrows, etc) " +
						"making them unaffected by gravity. They will become normally affected " +
						"again if they become wet. This is kind of overpowered.\n" +
						"Interacts with Enhanced Moistness.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "mechanics.obsidian_tears", "Obsidian Tears",
						"Empty bottles can be used to collect \"Obsidian Tears\" from Crying "
						+ "Obsidian. When quaffed, or dispensed onto a player, it updates the "
						+ "player's spawn to the location of the block the tears are from. "
						+ "Dispensers can also be used to fill empty bottles with tears.\n"
						+ "On client, just gives the bottle a custom appearance instead of "
						+ "a potion item.", y, mouseX, mouseY);
			} else if ("balance".equals(section)) {
				y = drawTrilean(matrices, "balance.faster_obsidian", "Faster Obsidian",
						"Makes obsidian break 3x faster. Needed on both sides to work properly. "
						+ "Does not break vanilla clients when on the server, but when on the client, "
						+ "vanilla servers will think you're cheating. (And they won't be wrong.)", y, mouseX, mouseY);
				y = drawTrilean(matrices, "balance.disable_prior_work_penalty", "Disable Prior Work Penalty",
						"Disables the anvil prior work penalty when an item has been worked " +
						"multiple times. Makes non-Mending tools relevant.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "balance.soul_speed_doesnt_damage_boots", "Soul Speed Doesn't Damage Boots",
						"Makes running on soul blocks with Soul Speed not deal damage to your boots.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "balance.infinity_mending", "Infinity & Mending",
						"Makes Mending and Infinity compatible enchantments.\n"
						+ "§4Not enabled in the \"vienna\" profile.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "balance.hyperspeed_furnace_minecart", "Hyperspeed Furnace Minecart",
						"Make furnace minecarts very fast.\n" +
						"An attempt to make rail transport relevant again, as well as furnace " +
						"carts, in a world with ice roads, swimming, elytra, etc.\n" +
						"Warning: These carts are so fast that they sometimes fall off of track " +
						"corners. Make sure to surround track corners with blocks.\n" +
						"Note: All furnace minecart tweaks enable a mixin that overrides " +
						"multiple methods in the furnace minecart entity. If you have another " +
						"mod that changes furnace minecarts (wow!) then you'll need to disable " +
						"this.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "balance.tridents_accept_power", "Tridents Accept Power",
						"Allows tridents to accept the Power enchantment, increasing their ranged " +
						"damage. It's pitiful that tridents only deal as much damage as an " +
						"unenchanted bow and this cannot be improved at all other than via " +
						"Impaling, which is exclusive to aquatic mobs (not including Drowned).\n" +
						"Power is considered incompatible with Sharpness and Impaling.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "balance.tridents_accept_sharpness", "Tridents Accept Sharpness",
						"Allows tridents to accept the Sharpness enchantment, increasing their " +
						"melee damage. See Tridents Accept Power for justification.\n" +
						"Sharpness is considered incompatible with Power and Impaling.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "balance.bedrock_impaling", "Bedrock-Like Impaling",
						"Makes the Impaling enchantment act like it does in Bedrock Edition and " +
						"Combat Test 4. Namely, it deals bonus damage to anything that is in " +
						"water or rain (i.e. is wet), instead of only aquatic mobs.\n" +
						"Interacts with Mechanics > Enhanced Moistness.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "balance.environmentally_friendly_creepers", "Environmentally Friendly Creepers",
						"Creeper explosions deal entity damage, but not block damage, even " +
						"if mobGriefing is true.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "balance.anvil_damage_only_on_fall", "Anvil Damage Only On Fall",
						"Anvils only take damage when falling from a height rather than randomly " +
						"after being used.\n"
						+ "§4Not enabled in the \"vienna\" profile.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "balance.broken_tools_drop_components", "Broken Gear Drops Components",
						"Makes items drop a configurable portion of configurable constituent "
						+ "components when broken.\n"
						+ "See fabrication/gear_components.ini.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "balance.drop_more_exp_on_death", "Drop More Experience On Death",
						"Players drop 80% of their experience upon death instead of basically none.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "balance.infinity_crossbows", "Infinity Crossbows",
						"Allow putting Infinity on crossbows. Only works for plain arrows.", y, mouseX, mouseY);
			} else if ("weird_tweaks".equals(section)) {
				y = drawTrilean(matrices, "weird_tweaks.endermen_dont_squeal", "Endermen Don't Squeal",
						"Makes Endermen not make their growling or screeching sounds when angry.\n" +
						"On client, mutes the sounds for just you. This means angry endermen don't " +
						"make ambient sounds.\n" +
						"On server, replaces the angry ambient sound with the normal ambient sound " +
						"for everyone. The stare sound is client-sided, unfortunately.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "weird_tweaks.disable_equip_sound", "Disable Equip Sound",
						"Disables the unnecessary \"Gear equips\" sound that plays when your hands " +
						"change, and is often glitchily played every tick. Armor equip sounds and " +
						"other custom equip sounds remain unchanged. You won't even notice it's " +
						"gone.\n" +
						"On client, mutes it just for you.\n" +
						"On server, prevents the sound from playing at all for everyone.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "weird_tweaks.repelling_void", "Déjà Void (aka Repelling Void)",
						"Players falling into the void teleports them back to the last place they " +
						"were on the ground and deals 6 hearts of damage.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "weird_tweaks.drop_exp_with_keep_inventory", "Drop Experience With keepInventory",
						"If keepInventory is enabled, players still drop their experience when " +
						"dying, but do so losslessly. Incents returning to where you died even " +
						"when keepInventory is enabled.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "weird_tweaks.gold_tools_useful_in_nether", "Gold Tools Useful In Nether",
						"Makes breaking nether blocks deal 50x damage to non-golden and " +
						"non-netherite tools, and makes golden tools take 1/50th the damage " +
						"when breaking the same blocks, bringing their durability just above " +
						"diamond. Relevant tags:\n" +
						"- fabrication:nether_blocks\n" +
						"- fabrication:nether_blocks_only_in_nether\n" +
						"- fabrication:gold_tools\n" +
						"- fabrication:nether_tools\n" +
						"On client, adjusts gold tool tooltips to show fractional damage.", y, mouseX, mouseY, REQUIRES_FABRIC_API);
				y = drawTrilean(matrices, "weird_tweaks.photoallergic_creepers", "Photoallergic Creepers",
						"Makes Creepers burn in sunlight. Very dangerous if combined with "
						+ "Tweaks > Creepers Explode When On Fire.\n"
						+ "§cConflicts with Photoresistant Mobs.\n"
						+ "§4Not enabled in the \"vienna\" profile.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "weird_tweaks.photoresistant_mobs", "Photoresistant Mobs",
						"Makes mobs not burn in sunlight.\n"
						+ "§cConflicts with Photoallergic Creepers. This option takes precedence.\n"
						+ "§4Not enabled in the \"vienna\" profile.", y, mouseX, mouseY);
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
						"§6Reloads resource packs.", y, mouseX, mouseY, CLIENT_ONLY, REQUIRES_FABRIC_API);
				y = drawTrilean(matrices, "pedantry.oak_is_apple", "Oak Is Apple",
						"Oak trees become apple trees. Because oak trees do not grow apples.\n"+
						"§6Reloads resource packs.", y, mouseX, mouseY, CLIENT_ONLY, REQUIRES_FABRIC_API);
			} else if ("woina".equals(section)) {
				y = drawTrilean(matrices, "woina.block_logo", "Block Logo",
						"Brings back the old animated block logo, with a dash of customizability.\n" +
						"See block_logo.png and block_logo.ini.", y, mouseX, mouseY, CLIENT_ONLY);
				y = drawTrilean(matrices, "woina.old_lava", "Old Lava",
						"Brings back the old (better) lava texture, as a dynamic texture just " +
						"like before.", y, mouseX, mouseY, CLIENT_ONLY);
				y = drawTrilean(matrices, "woina.classic_block_drops", "Classic Block Drops",
						"Changes block drops to look like they did in Survival Test. Namely, it " +
						"reduces their pixel density. By default, this is done by using a " +
						"mipmapped (downscaled) texture, but blocks with tileable textures can " +
						"instead just have a portion be rendered like the original implementation.\n" +
						"See classic_block_drops.ini.", y, mouseX, mouseY, CLIENT_ONLY);
				y = drawTrilean(matrices, "woina.blinking_drops", "Blinking Drops",
						"Makes dropped items and blocks blink white periodically like they did in " +
						"Survival Test. If Utility > Despawning Items Blink is also enabled, " +
						"the blinking becomes faster and faster as the item gets closer to " +
						"despawning.", y, mouseX, mouseY, CLIENT_ONLY);
			} else if ("situational".equals(section)) {
				y = drawTrilean(matrices, "situational.all_damage_is_fatal", "All Damage Is Fatal",
						"Any amount of damage done to an entity is unconditionally fatal.", y, mouseX, mouseY);
				y = drawTrilean(matrices, "situational.weapons_accept_silk", "Weapons Accept Silk Touch",
						"Weapons can accept Silk Touch. Does nothing on its own, but datapacks " +
						"can use this for special drops. Also makes Silk Touch incompatible with " +
						"Looting.", y, mouseX, mouseY);
			} else if ("experiments".equals(section)) {
				y = drawTrilean(matrices, "experiments.packed_atlases", "Packed Atlases",
						"Disables rounding of atlases to the next power-of-two. "
						+ "GPU drivers have supported \"NPOT\" textures since forever.\n"
						+ "§aPossibly reduces VRAM usage.\n§4May reduce performance.\n"
						+ "§eResources must be reloaded for this to take effect.", y, mouseX, mouseY, CLIENT_ONLY);
			}
		}
		GlStateManager.popMatrix();
	}

	private boolean drawButton(MatrixStack matrices, int x, int y, int w, int h, String text, float mouseX, float mouseY) {
		boolean click = false;
		boolean hover = mouseX >= x && mouseX <= x+w && mouseY >= y && mouseY <= y+h;
		fill(matrices, x, y, x+w, y+h, 0x55000000);
		if (hover) {
			fill(matrices, x, y, x+w, y+1, -1);
			fill(matrices, x, y, x+1, y+h, -1);
			fill(matrices, x, y+h-1, x+w, y+h, -1);
			fill(matrices, x+w-1, y, x+w, y+h, -1);
			if (didClick) {
				client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1f));
				click = true;
			}
		}
		int textWidth = textRenderer.getWidth(text);
		textRenderer.draw(matrices, text, x+((w-textWidth)/2), y+((h-8)/2), -1);
		return click;
	}

	private int drawTrilean(MatrixStack matrices, String key, String title, String desc, int y, float mouseX, float mouseY, TrileanFlag... flags) {
		boolean clientOnly = ArrayUtils.contains(flags, CLIENT_ONLY);
		boolean requiresFabricApi = ArrayUtils.contains(flags, REQUIRES_FABRIC_API);
		// presence of Fabric API is implied by the fact you need ModMenu to access this menu
		boolean noFabricApi = false; //!configuringServer && requiresFabricApi && !FabricLoader.getInstance().isModLoaded("fabric");
		boolean disabled = noFabricApi || (configuringServer && (serverReadOnly || clientOnly)) || !isValid(key);
		boolean noValue = noFabricApi || (configuringServer && clientOnly || !isValid(key));
		float time = optionAnimationTime.getOrDefault(key, 0f);
		float disabledTime = disabledAnimationTime.getOrDefault(key, 0f);
		boolean animateDisabled = disabledTime > 0;
		if (disabled && !knownDisabled.contains(key)) {
			disabledTime = disabledAnimationTime.compute(key, (k, f) -> 5 - (f == null ? 0 : f));
			knownDisabled.add(key);
		} else if (!disabled && knownDisabled.contains(key)) {
			disabledTime = disabledAnimationTime.compute(key, (k, f) -> 5 - (f == null ? 0 : f));
			knownDisabled.remove(key);
		}
		if (time > 0) {
			time -= client.getLastFrameDuration();
			if (time <= 0) {
				optionAnimationTime.remove(key);
				time = 0;
			} else {
				optionAnimationTime.put(key, time);
			}
		}
		if (disabledTime > 0) {
			disabledTime -= client.getLastFrameDuration();
			if (disabledTime <= 0) {
				disabledAnimationTime.remove(key);
				disabledTime = 0;
			} else {
				disabledAnimationTime.put(key, disabledTime);
			}
		}
		boolean noUnset = key.startsWith("general.");
		Trilean currentValue = noUnset ? (isEnabled(key) ? Trilean.TRUE : Trilean.FALSE) : getValue(key);
		boolean keyEnabled = isEnabled(key);
		Trilean prevValue = animateDisabled ? currentValue : optionPreviousValues.getOrDefault(key, currentValue);
		int prevX = prevValue == Trilean.FALSE ? 0 : prevValue == Trilean.TRUE ? noUnset ? 23 : 30 : 15;
		int prevHue = prevValue == Trilean.FALSE ? 0 : prevValue == Trilean.TRUE ? 120 : 55;
		int curX = currentValue == Trilean.FALSE ? 0 : currentValue == Trilean.TRUE ? noUnset ? 23 : 30 : 15;
		int curHue = currentValue == Trilean.FALSE ? 0 : currentValue == Trilean.TRUE ? 120 : 55;
		float a = sCurve5((5-time)/5f);
		float da = sCurve5((5-disabledTime)/5f);
		if (!disabled) {
			da = 1-da;
		}
		if (clientOnly) {
			fill(matrices, 133, y, 134+46, y+11, 0xFFFFAA00);
		} else {
			fill(matrices, 133, y, 134+46, y+11, 0xFFFFFFFF);
		}
		fill(matrices, 134, y+1, 134+45, y+10, 0x66000000);
		if (!noUnset) fill(matrices, 134+15, y+1, 134+15+15, y+10, 0x33000000);
		GlStateManager.pushMatrix();
		GlStateManager.translatef(134+(prevX+((curX-prevX)*a)), 0, 0);
		int knobAlpha = ((int)((noValue ? 1-da : 1) * 255))<<24;
		fill(matrices, 0, y+1, noUnset ? 22 : 15, y+10, MathHelper.hsvToRgb((prevHue+((curHue-prevHue)*a))/360f, 0.9f, 0.8f)|knobAlpha);
		if (!noUnset && a >= 1 && currentValue == Trilean.UNSET) {
			fill(matrices, keyEnabled ? 15 : -1, y+1, keyEnabled ? 16 : 0, y+10, MathHelper.hsvToRgb((keyEnabled ? 120 : 0)/360f, 0.9f, 0.8f)|knobAlpha);
		}
		GlStateManager.popMatrix();
		GlStateManager.enableBlend();
		RenderSystem.defaultBlendFunc();
		client.getTextureManager().bindTexture(new Identifier("fabrication", "trilean.png"));
		GlStateManager.color4f(1, 1, 1, 0.5f+((1-da)*0.5f));
		GlStateManager.enableTexture();
		if (noUnset) {
			drawTexture(matrices, 134+3, y+1, 0, 0, 15, 9, 45, 9);
			drawTexture(matrices, 134+4+22, y+1, 30, 0, 15, 9, 45, 9);
		} else {
			drawTexture(matrices, 134, y+1, 0, 0, 45, 9, 45, 9);
		}
		GlStateManager.disableTexture();
		if (didClick) {
			if (mouseX >= 134 && mouseX <= 134+45 && mouseY >= y+1 && mouseY <= y+10) {
				float pitch = y*0.005f;
				if (disabled) {
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO, 0.8f, 1));
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO, 0.7f, 1));
					tooltipBlinkTicks = 20;
				} else {
					int clickedIndex = (int)((mouseX-134)/(noUnset ? 22 : 15));
					Trilean newValue = clickedIndex == 0 ? Trilean.FALSE : clickedIndex == 1 && !noUnset ? Trilean.UNSET : Trilean.TRUE;
					if (newValue != currentValue) {
						client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE, 0.6f+pitch+((clickedIndex*(noUnset?2:1))*0.18f), 1f));
						optionPreviousValues.put(key, currentValue);
						optionAnimationTime.compute(key, (k, f) -> 5 - (f == null ? 0 : f));
						setValue(key, newValue.toString().toLowerCase(Locale.ROOT));
					}
				}
			}
		}
		int textAlpha = ((int)((0.7f+((1-da)*0.3f)) * 255))<<24;
		int startX = 136+50;
		int endX = textRenderer.draw(matrices, title, startX, y+2, 0xFFFFFF | textAlpha);
		if (mouseX >= startX && mouseX <= endX && mouseY >= y && mouseY <= y+10) {
			String prefix = "";
			if (clientOnly) {
				prefix += "§6Client Only ";
			}
			if (requiresFabricApi) {
				prefix += "§bRequires Fabric API ";
			}
			if (!prefix.isEmpty()) {
				prefix += "§r\n";
			}
			renderOrderedTooltip(matrices, textRenderer.wrapLines(new LiteralText(prefix+desc), mouseX < width/2 ? (int)(width-mouseX-30) : (int)mouseX-20), (int)(mouseX), (int)(20+mouseY));
		} else if (mouseX >= 134 && mouseX <= 134+45 && mouseY >= y && mouseY <= y+10) {
			if (disabled) {
				if (noFabricApi) {
					renderTooltip(matrices, new LiteralText(((tooltipBlinkTicks/5)%2 == 1 ? "§c" : "")+"This option requires Fabric API"), (int)mouseX, (int)mouseY);
				} else if (noValue) {
					renderTooltip(matrices, new LiteralText(((tooltipBlinkTicks/5)%2 == 1 ? "§c" : "")+"The server does not recognize this option"), (int)mouseX, (int)mouseY);
				} else {
					renderTooltip(matrices, new LiteralText(((tooltipBlinkTicks/5)%2 == 1 ? "§c" : "")+"You cannot configure this server"), (int)mouseX, (int)mouseY);
				}
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
		if (mouseX <= 120*scaleCompensation) {
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
				if (mouseX > (width-120)*scaleCompensation && mouseY < 16*scaleCompensation) {
					hasClonked = false;
					serverAnimateTime = 10-serverAnimateTime;
					configuringServer = false;
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_XYLOPHONE, 0.8f, 1));
				}
			} else {
				if (mouseX > (width-120)*scaleCompensation && mouseY < 16*scaleCompensation) {
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
			return ((GetServerConfig)client.getNetworkHandler()).fabrication$getServerTrileanConfig().getOrDefault(key, ResolvedTrilean.DEFAULT_FALSE);
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
		Set<String> newlyFalseKeys;
		Set<String> newlyNotFalseKeys;
		Map<String, String> changedKeysWithoutRuntimeChecks;
		
		boolean runtimeChecksToggled;
		
		if (configuringServer) {
			checkServerData();
			newlyFalseKeys = newlyFalseKeysServer;
			newlyNotFalseKeys = newlyNotFalseKeysServer;
			changedKeysWithoutRuntimeChecks = changedKeysWithoutRuntimeChecksServer;
			runtimeChecksToggled = runtimeChecksToggledServer;
		} else {
			newlyFalseKeys = newlyFalseKeysClient;
			newlyNotFalseKeys = newlyNotFalseKeysClient;
			changedKeysWithoutRuntimeChecks = changedKeysWithoutRuntimeChecksClient;
			runtimeChecksToggled = runtimeChecksToggledClient;
		}
		String oldValue = getRawValue(key);
		if ("general.runtime_checks".equals(key)) {
			runtimeChecksToggled = !runtimeChecksToggled;
		} else if (!MixinConfigPlugin.isRuntimeConfigurable(key)) {
			if (value.equals("false")) {
				if (newlyNotFalseKeys.contains(key)) {
					newlyNotFalseKeys.remove(key);
				} else {
					newlyFalseKeys.add(key);
				}
			} else if (oldValue.equals("false")) {
				if (newlyFalseKeys.contains(key)) {
					newlyFalseKeys.remove(key);
				} else {
					newlyNotFalseKeys.add(key);
				}
			}
		}
		if (!"general.runtime_checks".equals(key) && !isEnabled("general.runtime_checks") && !runtimeChecksToggled && !MixinConfigPlugin.isRuntimeConfigurable(key)) {
			if (changedKeysWithoutRuntimeChecks.containsKey(key)) {
				if (changedKeysWithoutRuntimeChecks.get(key).equals(value)) {
					changedKeysWithoutRuntimeChecks.remove(key);
				}
			} else {
				changedKeysWithoutRuntimeChecks.put(key, oldValue);
			}
		}
		if (configuringServer) {
			runtimeChecksToggledServer = runtimeChecksToggled;
			PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
			data.writeVarInt(1);
			data.writeString(key);
			data.writeString(value);
			client.getNetworkHandler().sendPacket(new CustomPayloadC2SPacket(new Identifier("fabrication", "config"), data));
		} else {
			runtimeChecksToggledClient = runtimeChecksToggled;
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
