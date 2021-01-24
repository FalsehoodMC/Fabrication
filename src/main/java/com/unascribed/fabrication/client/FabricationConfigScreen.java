package com.unascribed.fabrication.client;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL11;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.CommandDispatcher;
import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.FeaturesFile;
import com.unascribed.fabrication.FeaturesFile.FeatureEntry;
import com.unascribed.fabrication.FeaturesFile.Sides;
import com.unascribed.fabrication.interfaces.GetServerConfig;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.MixinConfigPlugin.Profile;
import com.unascribed.fabrication.support.ResolvedTrilean;
import com.unascribed.fabrication.support.Trilean;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
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
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;

import static com.unascribed.fabrication.client.FabricationConfigScreen.TrileanFlag.*;

public class FabricationConfigScreen extends Screen {

	public enum TrileanFlag {
		CLIENT_ONLY, REQUIRES_FABRIC_API
	}

	private final Map<String, String> SECTION_DESCRIPTIONS = Maps.newHashMap();
	private final Map<Profile, String> PROFILE_DESCRIPTIONS = Maps.newHashMap();
	
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
	
	private static final boolean PRIDE = Calendar.getInstance().get(Calendar.MONTH) == Calendar.JUNE
			|| Boolean.getBoolean("com.unascribed.fabrication.everyMonthIsPrideMonth") || Boolean.getBoolean("fabrication.everyMonthIsPrideMonth")
			|| System.getProperty("fabrication.pride") != null || System.getProperty("fabrication.iAm") != null || System.getProperty("fabrication.iAmA") != null;
	private static final List<String> prideIdentifiers = Lists.newArrayList();
	private static final Map<String, String> prideAliases = Maps.newHashMap();
	
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
	
	private float timeExisted;
	private boolean leaving = false;
	private float timeLeaving;
	private float sidebarScrollTarget;
	private float sidebarScroll;
	private float lastSidebarScroll;
	private float sidebarHeight;
	
	private boolean didClick;
	private float selectTime;
	private String selectedSection;
	private String prevSelectedSection;
	private float selectedSectionHeight;
	private float prevSelectedSectionHeight;
	private float selectedSectionScroll;
	private float prevSelectedSectionScroll;
	private float lastSelectedSectionScroll;
	private float lastPrevSelectedSectionScroll;
	private float selectedSectionScrollTarget;
	private float prevSelectedSectionScrollTarget;
	
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
	private final Map<String, Float> disappearAnimationTime = Maps.newHashMap();
	private final Set<String> knownDisabled = Sets.newHashSet();
	private final Set<String> disappeared = Sets.newHashSet();
	
	private boolean bufferTooltips = false;
	private final List<Runnable> bufferedTooltips = Lists.newArrayList();
	
	private int noteIndex = 0;
	private int fixedPrideFlag = -1;
	
	public FabricationConfigScreen(Screen parent) {
		super(new LiteralText("Fabrication configuration"));
		this.parent = parent;
		if (PRIDE && prideIdentifiers.isEmpty()) {
			try (InputStream is = MinecraftClient.getInstance().getResourceManager().getResource(new Identifier("fabrication", "pride.json")).getInputStream()) {
				JsonArray arr = new Gson().fromJson(new InputStreamReader(is, Charsets.UTF_8), JsonArray.class);
				for (JsonElement je : arr) {
					if (je.isJsonArray()) {
						JsonArray child = (JsonArray)je;
						String main = child.get(0).getAsString();
						prideIdentifiers.add(main);
						for (int i = 1; i < child.size(); i++) {
							prideAliases.put(child.get(i).getAsString(), main);
						}
					} else {
						prideIdentifiers.add(je.getAsString());
					}
				}
			} catch (Throwable t) {}
		}
		for (String sec : MixinConfigPlugin.getAllSections()) {
			SECTION_DESCRIPTIONS.put(sec, FeaturesFile.get(sec).desc);
		}
		for (Profile prof : Profile.values()) {
			PROFILE_DESCRIPTIONS.put(prof, FeaturesFile.get("general.profile."+prof.name().toLowerCase(Locale.ROOT)).desc);
		}
	}
	
	@Override
	public void init(MinecraftClient client, int width, int height) {
		super.init(client, width, height);
		String wantedFlag = null;
		if (System.getProperty("fabrication.pride") != null) {
			wantedFlag = System.getProperty("fabrication.pride");
		} else if (System.getProperty("fabrication.iAm") != null) {
			wantedFlag = System.getProperty("fabrication.iAm");
		} else if (System.getProperty("fabrication.iAmA") != null) {
			wantedFlag = System.getProperty("fabrication.iAmA");
		}
		if (wantedFlag != null) {
			wantedFlag = wantedFlag.toLowerCase(Locale.ROOT).replace("-", "");
			wantedFlag = prideAliases.getOrDefault(wantedFlag, wantedFlag);
			int idx = prideIdentifiers.indexOf(wantedFlag);
			if (idx == -1) {
				FabLog.warn("Don't have a pride flag by the name of "+wantedFlag);
			} else {
				fixedPrideFlag = idx;
			}
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
				GlStateManager.translatef(width/2f, height, 0);
				GlStateManager.rotatef(a*(leaving ? -180 : 180), 0, 0, 1);
				GlStateManager.translatef(-width/2, -height, 0);
				GlStateManager.pushMatrix();
					GlStateManager.translatef(0, height, 0);
					GlStateManager.translatef(width/2f, height/2f, 0);
					GlStateManager.rotatef(180, 0, 0, 1);
					GlStateManager.translatef(-width/2f, -height/2f, 0);
					fill(matrices, -width, -height, width*2, 0, 0xFF2196F3);
					GlStateManager.pushMatrix();
						drawBackground(matrices, -200, -200, delta, 0, 0);
						drawForeground(matrices, -200, -200, delta);
					GlStateManager.popMatrix();
				GlStateManager.popMatrix();
				for (int x = -1; x <= 1; x++) {
					for (int y = -1; y <= 0; y++) {
						if (x == 0 && y == 0) continue;
						GlStateManager.pushMatrix();
						GlStateManager.translatef(width*x, height*y, 0);
						parent.renderBackgroundTexture(0);
						GlStateManager.popMatrix();
					}
				}
				parent.render(matrices, -200, -200, delta);
			GlStateManager.popMatrix();
		} else {
			GlStateManager.pushMatrix();
			drawBackground(matrices, mouseX, mouseY, delta, 0, 0);
			drawForeground(matrices, mouseX, mouseY, delta);
			GlStateManager.popMatrix();
		}
		if (leaving && timeLeaving > 10) {
			client.openScreen(parent);
		}
	}
	
	private void drawBackground(MatrixStack matrices, int mouseX, int mouseY, float delta, int cutoffX, int cutoffY) {
		float cutoffV = cutoffY/(float)height;
		
		fillGradient(matrices, cutoffX == 0 ? -width : cutoffX, cutoffY, width*2, height, lerpColor(0xFF2196F3, 0xFF009688, cutoffV), 0xFF009688);
		float ratio = 502/1080f;
		
		float w = height*ratio;
		float brk = Math.min(width-w, (width*2/3f)-(w/3));
		float brk2 = brk+w;
		float border = (float)(20/(client.getWindow().getScaleFactor()));
		if (brk < cutoffX) brk = cutoffX;
		
		Matrix4f mat = matrices.peek().getModel();
		
		GlStateManager.enableBlend();
		RenderSystem.defaultBlendFunc();
		float time = selectedSection == null ? 10-selectTime : prevSelectedSection == null ? selectTime : 0;
		GlStateManager.color4f(1, 1, 1, 1);
		
		GlStateManager.disableCull();

		float top = (570/1080f)*height;
		float bottom = (901/1080f)*height;
		if (cutoffY < bottom) {
			float h = bottom-top;
			float flagCutoffV = 0;
			if (top < cutoffY) {
				top = cutoffY;
				flagCutoffV = 1-((bottom-top)/h);
			}
			if (PRIDE) {
				client.getTextureManager().bindTexture(PRIDETEX);
				int flags = 21;
				int flag = fixedPrideFlag == -1 ? Math.abs(random)%flags : fixedPrideFlag;
				float minU = (flag/(float)flags)+(0.5f/flags);
				float maxU = (flag/(float)flags)+(0.75f/flags);
				
				float minV = flagCutoffV;
				float maxV = 1;
				
				GL11.glBegin(GL11.GL_QUADS);
					GL11.glTexCoord2f(minU, minV);
					GL11.glVertex2f(brk, top);
					
					GL11.glTexCoord2f(maxU, minV);
					GL11.glVertex2f(brk2, top);
					
					GL11.glTexCoord2f(maxU, maxV);
					GL11.glVertex2f(brk2, bottom);
					
					GL11.glTexCoord2f(minU, maxV);
					GL11.glVertex2f(brk, bottom);
				GL11.glEnd();
			} else {
				GlStateManager.shadeModel(GL11.GL_SMOOTH);
				GlStateManager.disableTexture();
				float r = MathHelper.lerp(flagCutoffV, 0.298f, 0.475f);
				float g = MathHelper.lerp(flagCutoffV, 0.686f, 0.333f);
				float b = MathHelper.lerp(flagCutoffV, 0.314f, 0.282f);
				
				GL11.glBegin(GL11.GL_QUADS);
					GL11.glColor4f(r, g, b, 1);
					GL11.glVertex2f(brk, top);
					
					GL11.glColor4f(r, g, b, 1);
					GL11.glVertex2f(brk2, top);
					
					GL11.glColor4f(0.475f, 0.333f, 0.282f, 1);
					GL11.glVertex2f(brk2, bottom);
					
					GL11.glColor4f(0.475f, 0.333f, 0.282f, 1);
					GL11.glVertex2f(brk, bottom);
				GL11.glEnd();
				
				GlStateManager.enableTexture();
				GlStateManager.shadeModel(GL11.GL_FLAT);
			}
		}
		
		client.getTextureManager().bindTexture(BG);
		GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glBegin(GL11.GL_QUADS);
			GL11.glTexCoord2f(0, cutoffV);
			GL11.glVertex2f(Math.max(cutoffX, border), cutoffY);
			
			GL11.glTexCoord2f(0, cutoffV);
			GL11.glVertex2f(brk, cutoffY);
			
			GL11.glTexCoord2f(0, 1);
			GL11.glVertex2f(brk, height);
			
			GL11.glTexCoord2f(0, 1);
			GL11.glVertex2f(Math.max(cutoffX, border), height);
	
			
			GL11.glTexCoord2f(0, cutoffV);
			GL11.glVertex2f(brk, cutoffY);
			
			GL11.glTexCoord2f(1, cutoffV);
			GL11.glVertex2f(brk2, cutoffY);
			
			GL11.glTexCoord2f(1, 1);
			GL11.glVertex2f(brk2, height);
			
			GL11.glTexCoord2f(0, 1);
			GL11.glVertex2f(brk, height);
			
			
			GL11.glTexCoord2f(1, cutoffV);
			GL11.glVertex2f(brk2, cutoffY);
			
			GL11.glTexCoord2f(1, cutoffV);
			GL11.glVertex2f(width-border, cutoffY);
			
			GL11.glTexCoord2f(1, 1);
			GL11.glVertex2f(width-border, height);
			
			GL11.glTexCoord2f(1, 1);
			GL11.glVertex2f(brk2, height);
		GL11.glEnd();
		
		float a = 1-(0.3f+(sCurve5(time/10f)*0.7f));
		if (a > 0) {
			int ai = ((int)(a*255))<<24;
			fillGradient(matrices, cutoffX == 0 ? -width : cutoffX, cutoffY, width*2, height, lerpColor(0x2196F3, 0x009688, cutoffV)|ai, 0x009688|ai);
		}
	}

	private int lerpColor(int from, int to, float delta) {
		float a = MathHelper.lerp(delta, ((from>>24)&0xFF)/255f, ((to>>24)&0xFF)/255f);
		float r = MathHelper.lerp(delta, ((from>>16)&0xFF)/255f, ((to>>16)&0xFF)/255f);
		float g = MathHelper.lerp(delta, ((from>>8 )&0xFF)/255f, ((to>>8 )&0xFF)/255f);
		float b = MathHelper.lerp(delta, ((from>>0 )&0xFF)/255f, ((to>>0 )&0xFF)/255f);
		int c = 0;
		c |= ((int)(a*255)&0xFF)<<24;
		c |= ((int)(r*255)&0xFF)<<16;
		c |= ((int)(g*255)&0xFF)<<8;
		c |= ((int)(b*255)&0xFF)<<0;
		return c;
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
				GlStateManager.shadeModel(GL11.GL_SMOOTH);
				GL11.glBegin(GL11.GL_QUADS);
				GL11.glColor4f(1, 1, 1, 0.2f);
				GL11.glVertex2f(0, y-4);
				GL11.glColor4f(1, 1, 1, 0.2f+((1-selectA)*0.8f));
				GL11.glVertex2f(130*selectA, y-4);
				GL11.glColor4f(1, 1, 1, 0.2f+((1-selectA)*0.8f));
				GL11.glVertex2f(130*selectA, y+36);
				GL11.glColor4f(1, 1, 1, 0.2f);
				GL11.glVertex2f(0, y+36);
				GL11.glEnd();
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
				textRenderer.draw(matrices, "§l"+FeaturesFile.get(s).shortName, 4, y, -1);
			}
			String desc = SECTION_DESCRIPTIONS.getOrDefault(s, "No description available");
			y += 12;
			newHeight += 12;
			int x = 8;
			int line = 0;
			for (String word : Splitter.on(CharMatcher.whitespace()).split(desc)) {
				int w = textRenderer.getWidth(word);
				if (x+w > 100 && line == 0) {
					x = 8;
					y += 12;
					newHeight += 12;
					line = 1;
				}
				x = textRenderer.draw(matrices, word+" ", x, y, -1);
			}
			y += 12;
			newHeight += 12;
			if (didClick) {
				if (mouseX >= 0 && mouseX <= 130 && mouseY > startY-4 && mouseY < y) {
					boolean deselect = s.equals(selectedSection);
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_BELL, deselect ? 0.5f : 0.6f+(i*0.1f), 1f));
					prevSelectedSection = selectedSection;
					selectedSection = deselect ? null : s;
					
					prevSelectedSectionScroll = selectedSectionScroll;
					lastPrevSelectedSectionScroll = lastSelectedSectionScroll;
					prevSelectedSectionHeight = selectedSectionHeight;
					prevSelectedSectionScrollTarget = selectedSectionScrollTarget;
					
					selectedSectionScroll = 0;
					lastSelectedSectionScroll = 0;
					selectedSectionHeight = 0;
					selectedSectionScrollTarget = 0;
					
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
		drawSection(matrices, selectedSection, mouseX, mouseY, selectedChoiceY, sCurve5((10-selectTime)/10f), true);
		if (!MixinConfigPlugin.isEnabled("general.reduced_motion") && !Objects.equal(selectedSection, prevSelectedSection)) {
			drawSection(matrices, prevSelectedSection, -200, -200, prevSelectedChoiceY, sCurve5(selectTime/10f), false);
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
		
		drawBackground(matrices, mouseX, mouseY, delta, 130, height-20);
		
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

	private void drawSection(MatrixStack matrices, String section, float mouseX, float mouseY, float choiceY, float a, boolean selected) {
		if (a <= 0) return;
		if (MixinConfigPlugin.isEnabled("general.reduced_motion")) {
			a = 1;
		}
		GlStateManager.pushMatrix();
		GlStateManager.translatef(60, choiceY+16, 0);
		GlStateManager.scalef(a, a, 1);
		GlStateManager.translatef(-60, -(choiceY+16), 0);
		float lastScrollOfs = (selected ? lastSelectedSectionScroll : lastPrevSelectedSectionScroll);
		float scrollOfs = (selected ? selectedSectionScroll : prevSelectedSectionScroll);
		float scroll = (selected ? selectedSectionHeight : prevSelectedSectionHeight) < height-36 ? 0 : lastScrollOfs+((scrollOfs-lastScrollOfs)*client.getTickDelta());
		int startY = 16-(int)(scroll);
		int y = startY;
		if (section == null) {
			String v = getVersion();
			String blurb = "§lFabrication v"+v+" §rby unascribed\n"+(configuringServer ? "(Local version: v"+Agnos.getModVersion()+")" : "")
					+ "\nClick a category on the left to change settings.";
			int height = drawWrappedText(matrices, 140, 20, blurb, width-130, -1, false);
			if (drawButton(matrices, 140, 20+height+32, 120, 20, "Reload files", mouseX, mouseY)) {
				MixinConfigPlugin.reload();
			}
			y += height;
			y += 22;
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
					boolean profSel = getRawValue("general.profile").toUpperCase(Locale.ROOT).equals(p.name());
					if (mouseX >= 134+x && mouseX <= 134+x+16 && mouseY >= 18 && mouseY <= 18+16) {
						hovered = p;
					}
					if (didClick && mouseX >= 134+x && mouseX <= 134+x+16 && mouseY >= 18 && mouseY <= 18+16) {
						if (p == Profile.BURNT) {
							client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_CHIME, 1.8f, 1f));
							client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ITEM_FLINTANDSTEEL_USE, 1f));
							client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_FIRE_AMBIENT, 1f, 1f));
						} else if (p == Profile.GREEN) {
							client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1f));
						} else {
							client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL, 0.707107f+(p.ordinal()*0.22f), 1f));
						}
						setValue("general.profile", p.name().toLowerCase(Locale.ROOT));
					}
					color(PROFILE_COLORS.get(p), profSel ? 1f : hovered == p ? 0.6f : 0.3f);
					drawTexture(matrices, 134+x, 18, 0, 0, 0, 16, 16, 16, 16);
					x += 18;
				}
				FeatureEntry profile = FeaturesFile.get("general.profile");
				int textRight = textRenderer.draw(matrices, profile.name, 136, 6, -1);
				if (mouseX >= 136 && mouseX <= textRight && mouseY >= 6 && mouseY <= 18) {
					renderWrappedTooltip(matrices, profile.desc, mouseX, mouseY);
				}
				if (hovered != null) {
					FeatureEntry hoveredEntry = FeaturesFile.get("general.profile."+hovered.name().toLowerCase(Locale.ROOT));
					renderWrappedTooltip(matrices, "§l"+hoveredEntry.name+"\n§f"+hoveredEntry.desc, mouseX, mouseY);
				}
				y = 40;
				FeatureEntry rchecks = FeaturesFile.get("general.runtime_checks");
				y = drawTrilean(matrices, "general.runtime_checks", rchecks.name, rchecks.desc, y, mouseX, mouseY);
				FeatureEntry rmot = FeaturesFile.get("general.reduced_motion");
				y = drawTrilean(matrices, "general.reduced_motion", rmot.name, rmot.desc, y, mouseX, mouseY, CLIENT_ONLY);
			} else {
				for (Map.Entry<String, FeatureEntry> en : FeaturesFile.getAll().entrySet()) {
					if (en.getKey().startsWith(section+".")) {
						FeatureEntry fe = en.getValue();
						if (fe.meta) continue;
						TrileanFlag[] flags = {};
						if (fe.sides == Sides.CLIENT_ONLY) flags = ArrayUtils.add(flags, CLIENT_ONLY);
						y = drawTrilean(matrices, en.getKey(), fe.name, fe.desc, y, mouseX, mouseY, flags);
					}
				}
			}
		}
		if (y == startY) {
			textRenderer.draw(matrices, "There are no available features in this category", 136, startY+14, -1);
		}
		float h = y-startY;
		if (selected) {
			selectedSectionHeight = h;
		} else {
			prevSelectedSectionHeight = h;
		}
		int sh = height-36;
		if (h > sh) {
			float knobHeight = (sh/h)*sh;
			float knobY = ((selected ? selectedSectionScroll : prevSelectedSectionScroll)/(h-sh))*(sh-knobHeight)+16;
			fill(matrices, width-2, Math.max(16, (int)knobY), width, Math.min(height-20, (int)(knobY+knobHeight)), 0xAAFFFFFF);
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
		if (y < -12 || y > height-16) return y+14;
		boolean clientOnly = ArrayUtils.contains(flags, CLIENT_ONLY);
		boolean disappear = clientOnly && configuringServer;
		boolean requiresFabricApi = ArrayUtils.contains(flags, REQUIRES_FABRIC_API);
		// presence of Fabric API is implied by the fact you need ModMenu to access this menu
		boolean noFabricApi = false; //!configuringServer && requiresFabricApi && !FabricLoader.getInstance().isModLoaded("fabric");
		boolean failed = isFailed(key);
		boolean disabled = failed || noFabricApi || (configuringServer && (serverReadOnly || clientOnly)) || !isValid(key);
		boolean noValue = noFabricApi || (configuringServer && clientOnly || !isValid(key));
		float time = optionAnimationTime.getOrDefault(key, 0f);
		float disabledTime = disabledAnimationTime.getOrDefault(key, 0f);
		float disappearTime = disappearAnimationTime.getOrDefault(key, 0f);
		if (disappear && !disappeared.contains(key)) {
			disappearTime = disappearAnimationTime.compute(key, (k, f) -> 5 - (f == null ? 0 : f));
			disappeared.add(key);
		} else if (!disappear && disappeared.contains(key)) {
			disappearTime = disappearAnimationTime.compute(key, (k, f) -> 5 - (f == null ? 0 : f));
			disappeared.remove(key);
		}
		if (disappear && disappearTime <= 0) return y;
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
		if (disappearTime > 0) {
			disappearTime -= client.getLastFrameDuration();
			if (disappearTime <= 0) {
				disappearAnimationTime.remove(key);
				disappearTime = 0;
			} else {
				disappearAnimationTime.put(key, disappearTime);
			}
		}
		GlStateManager.pushMatrix();
		GlStateManager.translatef(0, y, 0);
		float dia = sCurve5((5-disappearTime)/5f);
		float scale;
		if (disappear) {
			GlStateManager.scalef(1, scale = 1-dia, 1);
		} else {
			GlStateManager.scalef(1, scale = dia, 1);
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
			fill(matrices, 133, 0, 134+46, 11, 0xFFFFAA00);
		} else {
			fill(matrices, 133, 0, 134+46, 11, 0xFFFFFFFF);
		}
		fill(matrices, 134, 1, 134+45, 10, 0x66000000);
		if (!noUnset) fill(matrices, 134+15, 1, 134+15+15, 10, 0x33000000);
		GlStateManager.pushMatrix();
		GlStateManager.translatef(134+(prevX+((curX-prevX)*a)), 0, 0);
		int knobAlpha = ((int)((noValue ? 1-da : 1) * 255))<<24;
		fill(matrices, 0, 1, noUnset ? 22 : 15, 10, MathHelper.hsvToRgb((prevHue+((curHue-prevHue)*a))/360f, 0.9f, 0.8f)|knobAlpha);
		if (!noUnset && a >= 1 && currentValue == Trilean.UNSET) {
			fill(matrices, keyEnabled ? 15 : -1, 1, keyEnabled ? 16 : 0, 10, MathHelper.hsvToRgb((keyEnabled ? 120 : 0)/360f, 0.9f, 0.8f)|knobAlpha);
		}
		GlStateManager.popMatrix();
		GlStateManager.enableBlend();
		RenderSystem.defaultBlendFunc();
		client.getTextureManager().bindTexture(new Identifier("fabrication", "trilean.png"));
		GlStateManager.color4f(1, 1, 1, 0.5f+((1-da)*0.5f));
		GlStateManager.enableTexture();
		if (noUnset) {
			drawTexture(matrices, 134+3, 1, 0, 0, 15, 9, 45, 9);
			drawTexture(matrices, 134+4+22, 1, 30, 0, 15, 9, 45, 9);
		} else {
			drawTexture(matrices, 134, 1, 0, 0, 45, 9, 45, 9);
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
					client.getSoundManager().play(PositionedSoundInstance.master(
							newValue == Trilean.FALSE ? SoundEvents.BLOCK_NOTE_BLOCK_BASS :
								newValue == Trilean.UNSET ? SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL :
									SoundEvents.BLOCK_NOTE_BLOCK_CHIME,
							0.6f+pitch, 1f));
					if (newValue != currentValue) {
						optionPreviousValues.put(key, currentValue);
						optionAnimationTime.compute(key, (k, f) -> 5 - (f == null ? 0 : f));
						setValue(key, newValue.toString().toLowerCase(Locale.ROOT));
					}
				}
			}
		}
		int textAlpha = ((int)((0.7f+((1-da)*0.3f)) * 255))<<24;
		int startY = y;
		int startX = 136+50;
		y += drawWrappedText(matrices, startX, 2, title, width-startX-6, 0xFFFFFF | textAlpha, false)*scale;
		int endX = width-6;
//		int endX = textRenderer.draw(matrices, title, startX, 2, 0xFFFFFF | textAlpha);
		GlStateManager.popMatrix();
		if (mouseX >= startX && mouseX <= endX && mouseY >= startY && mouseY <= y) {
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
			renderWrappedTooltip(matrices, prefix+desc, mouseX, mouseY);
		} else if (mouseX >= 134 && mouseX <= 134+45 && mouseY >= startY && mouseY <= startY+10) {
			if (disabled) {
				if (noFabricApi) {
					renderTooltip(matrices, new LiteralText(((tooltipBlinkTicks/5)%2 == 1 ? "§c" : "")+"This option requires Fabric API"), (int)mouseX, (int)mouseY);
				} else if (noValue) {
					renderTooltip(matrices, new LiteralText(((tooltipBlinkTicks/5)%2 == 1 ? "§c" : "")+"The server does not recognize this option"), (int)mouseX, (int)mouseY);
				} else if (failed) {
					renderTooltip(matrices, new LiteralText(((tooltipBlinkTicks/5)%2 == 1 ? "§c" : "")+"This feature failed to initialize"), (int)mouseX, (int)mouseY);
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
		return (y+2);
	}

	private void renderWrappedTooltip(MatrixStack matrices, String str, float mouseX, float mouseY) {
		renderOrderedTooltip(matrices, textRenderer.wrapLines(new LiteralText(str), mouseX < width/2 ? (int)(width-mouseX-30) : (int)mouseX-20), (int)(mouseX), (int)(20+mouseY));
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
		if (!MixinConfigPlugin.isEnabled("*.reduced_motion") && !leaving) {
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
		if (sidebarHeight > height) {
			lastSidebarScroll = sidebarScroll;
			sidebarScroll += (sidebarScrollTarget-sidebarScroll)/2;
			if (sidebarScrollTarget < 0) sidebarScrollTarget /= 2;
			float h = sidebarHeight-height;
			if (sidebarScrollTarget > h) sidebarScrollTarget = h+((sidebarScrollTarget-h)/2);
		}
		if (selectedSectionHeight > height-36) {
			lastSelectedSectionScroll = selectedSectionScroll;
			selectedSectionScroll += (selectedSectionScrollTarget-selectedSectionScroll)/2;
			if (selectedSectionScrollTarget < 0) selectedSectionScrollTarget /= 2;
			float h = selectedSectionHeight-(height-36);
			if (selectedSectionScrollTarget > h) selectedSectionScrollTarget = h+((selectedSectionScrollTarget-h)/2);
		}
		if (prevSelectedSectionHeight > height-36) {
			lastPrevSelectedSectionScroll = prevSelectedSectionScroll;
			prevSelectedSectionScroll += (prevSelectedSectionScrollTarget-prevSelectedSectionScroll)/2;
			if (prevSelectedSectionScrollTarget < 0) prevSelectedSectionScrollTarget /= 2;
			float h = prevSelectedSectionHeight-(height-36);
			if (prevSelectedSectionScrollTarget > h) prevSelectedSectionScrollTarget = h+((prevSelectedSectionScrollTarget-h)/2);
		}
		if (tooltipBlinkTicks > 0) {
			tooltipBlinkTicks--;
		}
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (mouseX <= 120) {
			sidebarScrollTarget -= amount*20;
		} else {
			selectedSectionScrollTarget -= amount*20;
		}
		return super.mouseScrolled(mouseX, mouseY, amount);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			if (configuringServer) {
				if (mouseX > (width-120) && mouseY < 16) {
					hasClonked = false;
					serverAnimateTime = 10-serverAnimateTime;
					configuringServer = false;
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_XYLOPHONE, 0.8f, 1));
				}
			} else {
				if (mouseX > (width-120) && mouseY < 16) {
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
	
	private String getVersion() {
		if (configuringServer) {
			return ((GetServerConfig)client.getNetworkHandler()).fabrication$getServerVersion();
		} else {
			return Agnos.getModVersion();
		}
	}
	
	private boolean isFailed(String key) {
		if (configuringServer) {
			return ((GetServerConfig)client.getNetworkHandler()).fabrication$getServerFailedConfig().contains(key);
		} else {
			return MixinConfigPlugin.isFailed(key);
		}
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
