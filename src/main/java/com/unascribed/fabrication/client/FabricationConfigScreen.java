package com.unascribed.fabrication.client;

import static com.unascribed.fabrication.client.FabricationConfigScreen.ConfigValueFlag.CLIENT_ONLY;
import static com.unascribed.fabrication.client.FabricationConfigScreen.ConfigValueFlag.HIGHLIGHT_QUERY_MATCH;
import static com.unascribed.fabrication.client.FabricationConfigScreen.ConfigValueFlag.REQUIRES_FABRIC_API;
import static com.unascribed.fabrication.client.FabricationConfigScreen.ConfigValueFlag.SHOW_SOURCE_SECTION;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.CommandDispatcher;
import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.FabricationModClient;
import com.unascribed.fabrication.FeaturesFile;
import com.unascribed.fabrication.FeaturesFile.FeatureEntry;
import com.unascribed.fabrication.FeaturesFile.Sides;
import com.unascribed.fabrication.interfaces.GetServerConfig;
import com.unascribed.fabrication.support.ConfigValue;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.MixinConfigPlugin.Profile;
import com.unascribed.fabrication.support.ResolvedConfigValue;

import com.google.common.base.CharMatcher;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import io.github.queerbric.pride.PrideFlag;
import io.github.queerbric.pride.PrideFlags;
import io.netty.buffer.Unpooled;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat.DrawMode;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

public class FabricationConfigScreen extends Screen {

	public enum ConfigValueFlag {
		CLIENT_ONLY, REQUIRES_FABRIC_API, SHOW_SOURCE_SECTION, HIGHLIGHT_QUERY_MATCH
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
	private static final Identifier BG_DARK = new Identifier("fabrication", "bg-dark.png");
	private static final Identifier BG_GRAD = new Identifier("fabrication", "bg-grad.png");
	private static final Identifier BG_GRAD_DARK = new Identifier("fabrication", "bg-grad-dark.png");

	private static long serverLaunchId = -1;

	private static final Set<String> newlyBannedKeysClient = Sets.newHashSet();
	private static final Set<String> newlyBannedKeysServer = Sets.newHashSet();

	private static final Set<String> newlyUnbannedKeysClient = Sets.newHashSet();
	private static final Set<String> newlyUnbannedKeysServer = Sets.newHashSet();

	private final Screen parent;

	private final PrideFlag prideFlag;

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

	private final List<String> tabs = Lists.newArrayList();
	private final Multimap<String, String> options = Multimaps.newMultimap(Maps.newLinkedHashMap(), Lists::newArrayList);

	private final Map<String, ConfigValue> optionPreviousValues = Maps.newHashMap();
	private final Map<String, Float> optionAnimationTime = Maps.newHashMap();
	private final Map<String, Float> disabledAnimationTime = Maps.newHashMap();
	private final Map<String, Float> becomeBanAnimationTime = Maps.newHashMap();
	private final Set<String> knownDisabled = Sets.newHashSet();
	private final Set<String> onlyBannableds = Sets.newHashSet();

	private boolean bufferTooltips = false;
	private final List<Runnable> bufferedTooltips = Lists.newArrayList();

	private int noteIndex = 0;

	private TextFieldWidget searchField;
	private Pattern queryPattern = Pattern.compile("");
	private boolean emptyQuery = true;
	private boolean searchingScriptable = false;

	public FabricationConfigScreen(Screen parent) {
		super(new LiteralText("Fabrication configuration"));
		this.parent = parent;
		prideFlag = PrideFlags.isPrideMonth() ? PrideFlags.getRandomFlag() : null;
		for (String sec : MixinConfigPlugin.getAllSections()) {
			SECTION_DESCRIPTIONS.put(sec, FeaturesFile.get(sec).desc);
		}
		for (Profile prof : Profile.values()) {
			PROFILE_DESCRIPTIONS.put(prof, FeaturesFile.get("general.profile."+prof.name().toLowerCase(Locale.ROOT)).desc);
		}
		for (String key : MixinConfigPlugin.getAllKeys()) {
			int dot = key.indexOf('.');
			String section = key.substring(0, dot);
			String name = key.substring(dot+1);
			options.put(section, name);
		}
		tabs.add("search");
		tabs.addAll(options.keySet());
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
		searchField = new TextFieldWidget(textRenderer, 131, 1, width-252, 14, searchField, new LiteralText("Search"));
		if (Agnos.isModLoaded("fscript")) searchField.setWidth(searchField.getWidth()-16);

		searchField.setChangedListener((s) -> {
			s = s.trim();
			emptyQuery = s.isEmpty();
			queryPattern = Pattern.compile(s, Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
		});
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
			matrices.push();
			matrices.translate(width/2f, height, 0);
			matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(a*(leaving ? -180 : 180)));
			matrices.translate(-width/2, -height, 0);
			matrices.push();
			matrices.translate(0, height, 0);
			matrices.translate(width/2f, height/2f, 0);
			matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180));
			matrices.translate(-width/2f, -height/2f, 0);
			fill(matrices, -width, -height, width*2, 0, MixinConfigPlugin.isEnabled("general.dark_mode") ? 0xFF212020 : 0xFF2196F3);
			matrices.push();
			drawBackground(matrices, -200, -200, delta, 0, 0);
			drawForeground(matrices, -200, -200, delta);
			matrices.pop();
			matrices.pop();
			matrices.pop();

			// background rendering ignores the matrixstack, so we have to Make A Mess in the projection matrix instead
			MatrixStack projection = new MatrixStack();
			projection.method_34425(RenderSystem.getProjectionMatrix());
			projection.push();
			projection.translate(width/2f, height, 0);
			projection.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(a*(leaving ? -180 : 180)));
			projection.translate(-width/2, -height, 0);
			for (int x = -1; x <= 1; x++) {
				for (int y = -1; y <= 0; y++) {
					if (x == 0 && y == 0) continue;
					projection.push();
					projection.translate(width*x, height*y, 0);
					RenderSystem.setProjectionMatrix(projection.peek().getModel());
					parent.renderBackgroundTexture(0);
					projection.pop();
				}
			}
			RenderSystem.setProjectionMatrix(projection.peek().getModel());
			parent.render(matrices, -200, -200, delta);
			projection.pop();
			RenderSystem.setProjectionMatrix(projection.peek().getModel());
		} else {
			matrices.push();
			drawBackground(matrices, mouseX, mouseY, delta, 0, 0);
			drawForeground(matrices, mouseX, mouseY, delta);
			matrices.pop();
		}
		if (leaving && timeLeaving > 10) {
			client.setScreen(parent);
		}
	}
	private void drawBackground(MatrixStack matrices, int mouseX, int mouseY, float delta, int cutoffX, int cutoffY) {
		drawBackground(height, width, client, prideFlag, selectedSection == null ? 10-selectTime : prevSelectedSection == null ? selectTime : 0, matrices, mouseX, mouseY, delta, cutoffX, cutoffY);
	}
	public static void drawBackground(int height, int width, MinecraftClient client, PrideFlag prideFlag, float time, MatrixStack matrices, int mouseX, int mouseY, float delta, int cutoffX, int cutoffY) {
		float cutoffV = cutoffY/(float)height;
		Identifier bg = MixinConfigPlugin.isEnabled("general.dark_mode") ? BG_DARK : BG;
		Identifier bgGrad = MixinConfigPlugin.isEnabled("general.dark_mode") ? BG_GRAD_DARK : BG_GRAD;
		BufferBuilder bb = Tessellator.getInstance().getBuffer();
		Matrix4f mat = matrices.peek().getModel();

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();

		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.setShaderTexture(0, bgGrad);
		client.getTextureManager().bindTexture(bgGrad);
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);

		int startX = cutoffX == 0 ? -width : cutoffX;

		bb.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		bb.vertex(mat, startX, cutoffY, 0).texture(0, cutoffV).next();
		bb.vertex(mat, width*2, cutoffY, 0).texture(1, cutoffV).next();
		bb.vertex(mat, width*2, height, 0).texture(1, 1).next();
		bb.vertex(mat, startX, height, 0).texture(0, 1).next();
		bb.end();
		BufferRenderer.draw(bb);
		float ratio = 502/1080f;

		float w = height*ratio;
		float brk = Math.min(width-w, (width*2/3f)-(w/3));
		float brk2 = brk+w;
		float border = (float)(20/(client.getWindow().getScaleFactor()));
		if (brk < cutoffX) brk = cutoffX;


		float top = (570/1080f)*height;
		float bottom = (901/1080f)*height;
		if (cutoffY < bottom) {
			float h = bottom-top;
			float flagCutoffV = 0;
			if (top < cutoffY) {
				top = cutoffY;
				flagCutoffV = 1-((bottom-top)/h);
			}
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			if (prideFlag != null) {
				prideFlag.render(matrices, brk, top, w, bottom-top);
			} else {
				RenderSystem.disableTexture();
				bb.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
				float r = MathHelper.lerp(flagCutoffV, 0.298f, 0.475f);
				float g = MathHelper.lerp(flagCutoffV, 0.686f, 0.333f);
				float b = MathHelper.lerp(flagCutoffV, 0.314f, 0.282f);
				bb.vertex(mat, brk, top, 0).color(r, g, b, 1).next();
				bb.vertex(mat, brk2, top, 0).color(r, g, b, 1).next();
				bb.vertex(mat, brk2, bottom, 0).color(0.475f, 0.333f, 0.282f, 1).next();
				bb.vertex(mat, brk, bottom, 0).color(0.475f, 0.333f, 0.282f, 1).next();
				bb.end();
				BufferRenderer.draw(bb);
				RenderSystem.enableTexture();
			}
		}

		RenderSystem.setShaderTexture(0, bg);
		client.getTextureManager().bindTexture(bg);
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		bb.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		bb.vertex(mat, Math.max(cutoffX, border), cutoffY, 0).texture(0, cutoffV).next();
		bb.vertex(mat, brk, cutoffY, 0).texture(0, cutoffV).next();
		bb.vertex(mat, brk, height, 0).texture(0, 1).next();
		bb.vertex(mat, Math.max(cutoffX, border), height, 0).texture(0, 1).next();

		bb.vertex(mat, brk, cutoffY, 0).texture(0, cutoffV).next();
		bb.vertex(mat, brk2, cutoffY, 0).texture(1, cutoffV).next();
		bb.vertex(mat, brk2, height, 0).texture(1, 1).next();
		bb.vertex(mat, brk, height, 0).texture(0, 1).next();

		bb.vertex(mat, brk2, cutoffY, 0).texture(1, cutoffV).next();
		bb.vertex(mat, width-border, cutoffY, 0).texture(1, cutoffV).next();
		bb.vertex(mat, width-border, height, 0).texture(1, 1).next();
		bb.vertex(mat, brk2, height, 0).texture(1, 1).next();

		bb.end();
		BufferRenderer.draw(bb);

		float a = 1-(0.3f+(sCurve5(time/10f)*0.7f));
		if (a > 0) {
			RenderSystem.setShaderColor(1, 1, 1, a);
			RenderSystem.setShaderTexture(0, bgGrad);
			bb.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
			bb.vertex(mat, startX, cutoffY, 0).texture(0, cutoffV).next();
			bb.vertex(mat, width*2, cutoffY, 0).texture(1, cutoffV).next();
			bb.vertex(mat, width*2, height, 0).texture(1, 1).next();
			bb.vertex(mat, startX, height, 0).texture(0, 1).next();
			bb.end();
			BufferRenderer.draw(bb);
		}

		RenderSystem.setShaderColor(1, 1, 1, 1);
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
		float scroll = sidebarHeight < height ? 0 : lastSidebarScroll+((sidebarScroll-lastSidebarScroll)*client.getTickDelta());
		scroll = (float) (Math.floor((scroll*client.getWindow().getScaleFactor()))/client.getWindow().getScaleFactor());
		float y = 8-scroll;
		int newHeight = 8;
		int i = 0;
		float selectedChoiceY = -60;
		float prevSelectedChoiceY = -60;
		for (String s : tabs) {
			int thisHeight = 8;
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
			float startY = y;
			if (y >= -24 && y < height) {
				int icoY = 0;
				int size = 24;
				if ("search".equals(s)) {
					size = 12;
					icoY = -4;
				}
				Identifier id = new Identifier("fabrication", "category/"+s+".png");
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				client.getTextureManager().bindTexture(id);
				RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
				RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
				RenderSystem.setShaderColor(1, 1, 1, 0.4f);
				RenderSystem.setShaderTexture(0, id);
				matrices.push();
				matrices.translate(0, y, 0);
				drawTexture(matrices, (130-4-size), icoY, 0, 0, 0, size, Math.min(size, (int)Math.ceil(height-y)), size, size);
				matrices.pop();
			}
			if (y >= -12 && y < height) {
				textRenderer.draw(matrices, "§l"+FeaturesFile.get(s).shortName, 4, y, -1);
			}
			y += 12;
			thisHeight += 12;
			if (!"search".equals(s)) {
				String desc = SECTION_DESCRIPTIONS.getOrDefault(s, "No description available");
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
				thisHeight += 12;
			}
			if (didClick) {
				if (mouseX >= 0 && mouseX <= 130 && mouseY > startY-4 && mouseY < y) {
					boolean deselect = s.equals(selectedSection);
					if ("search".equals(s) && !deselect) {
						client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_PLING, 1.2f, 1f));
					} else {
						client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_BELL, deselect ? 0.5f : 0.6f+(i*0.1f), 1f));
					}
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
			if (selectA > 0) {
				RenderSystem.disableCull();
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				RenderSystem.disableTexture();
				RenderSystem.setShader(GameRenderer::getPositionColorShader);
				BufferBuilder bb = Tessellator.getInstance().getBuffer();
				Matrix4f mat = matrices.peek().getModel();
				bb.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
				bb.vertex(mat, 0, y-thisHeight-8, 0).color(1, 1, 1, 0.2f).next();
				bb.vertex(mat, 130*selectA, y-thisHeight-8, 0).color(1, 1, 1, 0.2f+((1-selectA)*0.8f)).next();
				bb.vertex(mat, 130*selectA, y, 0).color(1, 1, 1, 0.2f+((1-selectA)*0.8f)).next();
				bb.vertex(mat, 0, y, 0).color(1, 1, 1, 0.2f).next();
				bb.end();
				BufferRenderer.draw(bb);
				RenderSystem.enableTexture();
			}
			y += 8;
			newHeight += thisHeight;
			i++;
		}
		sidebarHeight = newHeight;
		if (sidebarHeight >= height) {
			float knobHeight = (height/sidebarHeight)*height;
			float knobY = (scroll/(sidebarHeight-height))*(height-knobHeight);
			fill(matrices, 128, (int)knobY, 130, (int)(knobY+knobHeight), 0xAAFFFFFF);
		}

		bufferTooltips = true;
		float selectedA = sCurve5((10-selectTime)/10f);
		float prevSelectedA = sCurve5(selectTime/10f);
		drawSection(matrices, selectedSection, mouseX, mouseY, selectedChoiceY, selectedA, true);
		if (!MixinConfigPlugin.isEnabled("general.reduced_motion") && !Objects.equal(selectedSection, prevSelectedSection)) {
			drawSection(matrices, prevSelectedSection, -200, -200, prevSelectedChoiceY, prevSelectedA, false);
		}

		boolean searchSelected = "search".equals(selectedSection);
		boolean searchWasSelected = "search".equals(prevSelectedSection);
		if (searchSelected) {
			RenderSystem.setShaderColor(1, 1, 1, selectedA);
			searchField.setAlpha(selectedA);
			searchField.render(matrices, mouseX, mouseY, delta);
		} else if (searchWasSelected && prevSelectedA > 0) {
			RenderSystem.setShaderColor(1, 1, 1, prevSelectedA);
			searchField.setAlpha(prevSelectedA);
			searchField.render(matrices, mouseX, mouseY, delta);
		}
		searchField.setTextFieldFocused(searchSelected);
		RenderSystem.setShaderColor(1, 1, 1, 1);

		matrices.push();
		RenderSystem.disableDepthTest();
		fill(matrices, width-120, 0, width*2, 16, 0x33000000);
		matrices.push();
		matrices.translate(width-60, 8, 0);
		matrices.push();
		matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(a*-180));
		float h = (40+(a*-100))/360f;
		if (h < 0) {
			h = 1+h;
		}
		matrices.push();
		matrices.scale((float)(1-(Math.abs(Math.sin(a*Math.PI))/2)), 1, 1);
		fill(matrices, -60, -8, 0, 8, MathHelper.hsvToRgb(h, 0.9f, 0.9f)|0xFF000000);
		if (isSingleplayer) {
			fill(matrices, 0, -8, 60, 8, MathHelper.hsvToRgb(0.833333f, 0.9f, 0.9f)|0xFF000000);
		}
		matrices.pop();
		matrices.push();
		matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(45));
		// 8 / sqrt(2)
		float f = 5.6568542f;
		matrices.scale(f, f, 1);
		fill(matrices, -1, -1, 1, 1, 0xFFFFFFFF);
		matrices.pop();
		if (!isSingleplayer) {
			fill(matrices, -6, -1, -2, 1, 0xFF000000);
		}
		matrices.pop();
		fill(matrices, -2, -2, 2, 2, 0xFF000000);
		matrices.pop();

		boolean darkMode = MixinConfigPlugin.isEnabled("general.dark_mode");

		textRenderer.draw(matrices, "CLIENT", width-115, 4, 0xFF000000);
		textRenderer.draw(matrices, "SERVER", width-40, 4, whyCantConfigureServer == null || isSingleplayer ? 0xFF000000 : darkMode ? 0x44FFFFFF : 0x44000000);
		if (serverReadOnly && whyCantConfigureServer == null) {
			RenderSystem.setShaderTexture(0, new Identifier("fabrication", "lock.png"));
			RenderSystem.setShaderColor(0, 0, 0, 1);
			drawTexture(matrices, width-49, 3, 0, 0, 0, 8, 8, 8, 8);
		}
		if (searchSelected && Agnos.isModLoaded("fscript")) {
			if(didClick && mouseX >= width-136 && mouseX < width-120 && mouseY <= 16) {
				client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1f));
				searchingScriptable = !searchingScriptable;
			}
			RenderSystem.setShaderTexture(0, new Identifier("fabrication", "fscript.png"));
			RenderSystem.setShaderColor(1, 1, 1, 1);
			fill(matrices, width-136, 0, width-120, 16, searchingScriptable? 0xFF0AA000 : 0x55000000);
			drawTexture(matrices, width-136, 0, 0, 0, 0, 16, 16, 16, 16);
		}
		drawBackground(matrices, mouseX, mouseY, delta, 130, height-20);

		List<String> notes = Lists.newArrayList();

		Set<String> newlyBannedKeys;
		Set<String> newlyUnbannedKeys;

		boolean hasYellowNote = false;
		boolean hasRedNote = false;

		if (configuringServer) {
			checkServerData();
			newlyBannedKeys = newlyBannedKeysServer;
			newlyUnbannedKeys = newlyUnbannedKeysServer;
		} else {
			newlyBannedKeys = newlyBannedKeysClient;
			newlyUnbannedKeys = newlyUnbannedKeysClient;
		}
		if (!newlyUnbannedKeys.isEmpty()) {
			notes.add("§c"+newlyUnbannedKeys.size()+" newly unbanned option"+(newlyUnbannedKeys.size() == 1 ? "" : "s")+" will\n§cnot activate until the {} is\n§crestarted.");
			hasRedNote = true;
		}
		if (!newlyBannedKeys.isEmpty()) {
			notes.add(newlyBannedKeys.size()+" newly banned option"+(newlyBannedKeys.size() == 1 ? "" : "s")+" will be\nunloaded when the {} is\nrestarted.");
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
		matrices.pop();
	}

	private void checkServerData() {
		ClientPlayNetworkHandler cpnh = client.getNetworkHandler();
		if (cpnh != null && cpnh instanceof GetServerConfig) {
			long launchId = ((GetServerConfig)cpnh).fabrication$getLaunchId();
			if (launchId != serverLaunchId) {
				newlyBannedKeysServer.clear();
				newlyUnbannedKeysServer.clear();
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
		matrices.push();
		matrices.translate(60, choiceY+16, 0);
		matrices.scale(a, a, 1);
		matrices.translate(-60, -(choiceY+16), 0);
		float lastScrollOfs = (selected ? lastSelectedSectionScroll : lastPrevSelectedSectionScroll);
		float scrollOfs = (selected ? selectedSectionScroll : prevSelectedSectionScroll);
		float scroll = (selected ? selectedSectionHeight : prevSelectedSectionHeight) < height-36 ? 0 : lastScrollOfs+((scrollOfs-lastScrollOfs)*client.getTickDelta());
		int startY = 16-(int)(scroll);
		int y = startY;
		if (section == null) {
			String v = getVersion();
			String blurb = "§lFabrication v"+v+" §rby unascribed and SFort\nRunning under Minecraft "+SharedConstants.getGameVersion().getName()+"\n"+(configuringServer ? "(Local version: v"+Agnos.getModVersion()+")" : "")
					+ "\nClick a category on the left to change settings.";
			int height = drawWrappedText(matrices, 140, 20, blurb, width-130, -1, false);
			if (drawButton(matrices, 140, 20+height+32, 120, 20, "Reload files", mouseX, mouseY)) {
				MixinConfigPlugin.reload();
			}
			y += height;
			y += 22;
		} else {
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.setShaderTexture(0, new Identifier("fabrication", "category/"+section+".png"));
			RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			RenderSystem.setShaderColor(1, 1, 1, 0.1f);
			matrices.push();
			matrices.translate(130+((width-130)/2f), height/2f, 0);
			drawTexture(matrices, -80, -80, 0, 0, 0, 160, 160, 160, 160);
			matrices.pop();
			if ("general".equals(section)) {
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				RenderSystem.setShaderTexture(0, new Identifier("fabrication", "coffee_bean.png"));
				RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
				RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
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
					color(PROFILE_COLORS.get(p), profSel ? 1f : (hovered == p ? 0.6f : 0.3f) * (MixinConfigPlugin.isEnabled("general.dark_mode") ? 0.5f : 1));
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
				y = drawConfigValues(matrices, y, mouseX, mouseY, (en) -> en.key.startsWith("general."));
			} else if ("search".equals(section)) {
				y += 4;
				Predicate<FeatureEntry> pen= (en) -> emptyQuery || (queryPattern.matcher(en.name).find() || queryPattern.matcher(en.shortName).find() || queryPattern.matcher(en.desc).find());
				if (Agnos.isModLoaded("fscript") && searchingScriptable) pen = ((Predicate<FeatureEntry>) en -> en.fscript != null).and(pen);
				y = drawConfigValues(matrices, y, mouseX, mouseY, pen, SHOW_SOURCE_SECTION, emptyQuery ? null : HIGHLIGHT_QUERY_MATCH);
			} else {
				y = drawConfigValues(matrices, y, mouseX, mouseY, (en) -> en.key.startsWith(section+".") && !en.extra);
				int titleY = y;
				y += 25;
				int endY = drawConfigValues(matrices, y, mouseX, mouseY, (en) -> en.key.startsWith(section+".") && en.extra);
				if (endY != y && y < height-8) {
					textRenderer.draw(matrices, "§lExtra", 135, titleY+10, -1);
				}
				y = endY;
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
		matrices.pop();
	}

	private int drawConfigValues(MatrixStack matrices, int y, float mouseX, float mouseY, Predicate<FeatureEntry> pred, ConfigValueFlag... defaultFlags) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		for (Map.Entry<String, FeatureEntry> en : FeaturesFile.getAll().entrySet()) {
			if ("general.profile".equals(en.getKey())) continue;
			FeatureEntry fe = en.getValue();
			if (fe.meta || fe.section) continue;
			if (!pred.test(fe)) continue;
			ConfigValueFlag[] flags = defaultFlags;
			if (fe.sides == Sides.CLIENT_ONLY) flags = ArrayUtils.add(flags, CLIENT_ONLY);
			y = drawConfigValue(matrices, en.getKey(), fe.name, fe.desc, y, mouseX, mouseY, flags);
		}
		return y;
	}

	private boolean drawButton(MatrixStack matrices, int x, int y, int w, int h, String text, float mouseX, float mouseY) {
		boolean click = false;
		boolean hover = mouseX >= x && mouseX <= x+w && mouseY >= y && mouseY <= y+h;
		fill(matrices, x, y, x+w, y+h, MixinConfigPlugin.isEnabled("general.dark_mode") ? 0x44FFFFFF : 0x55000000);
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

	private int drawConfigValue(MatrixStack matrices, String key, String title, String desc, int y, float mouseX, float mouseY, ConfigValueFlag... flags) {
		if (y < -12 || y > height-16) return y+14;
		boolean clientOnly = ArrayUtils.contains(flags, CLIENT_ONLY);
		boolean onlyBannable = clientOnly && configuringServer;
		boolean requiresFabricApi = ArrayUtils.contains(flags, REQUIRES_FABRIC_API);
		boolean showSourceSection = ArrayUtils.contains(flags, SHOW_SOURCE_SECTION);
		boolean highlightQueryMatch = ArrayUtils.contains(flags, HIGHLIGHT_QUERY_MATCH);
		// presence of Fabric API is implied by the fact you need ModMenu to access this menu
		boolean noFabricApi = false; //!configuringServer && requiresFabricApi && !FabricLoader.getInstance().isModLoaded("fabric");
		boolean failed = isFailed(key);
		boolean banned = !configuringServer && FabricationModClient.isBannedByServer(key);
		boolean disabled = failed || banned || noFabricApi || (configuringServer && serverReadOnly) || !isValid(key);
		boolean noValue = noFabricApi || (configuringServer && clientOnly || !isValid(key));
		float time = optionAnimationTime.getOrDefault(key, 0f);
		float disabledTime = disabledAnimationTime.getOrDefault(key, 0f);
		float becomeBanTime = becomeBanAnimationTime.getOrDefault(key, 0f);
		if (onlyBannable && !onlyBannableds.contains(key)) {
			becomeBanTime = becomeBanAnimationTime.compute(key, (k, f) -> 5 - (f == null ? 0 : f));
			onlyBannableds.add(key);
		} else if (!onlyBannable && onlyBannableds.contains(key)) {
			becomeBanTime = becomeBanAnimationTime.compute(key, (k, f) -> 5 - (f == null ? 0 : f));
			onlyBannableds.remove(key);
		}
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
		if (becomeBanTime > 0) {
			becomeBanTime -= client.getLastFrameDuration();
			if (becomeBanTime <= 0) {
				becomeBanAnimationTime.remove(key);
				becomeBanTime = 0;
			} else {
				becomeBanAnimationTime.put(key, becomeBanTime);
			}
		}
		matrices.push();
		matrices.translate(0, y, 0);
		float dia = sCurve5((5-becomeBanTime)/5f);
		float scale = 1;
		boolean noUnset = key.startsWith("general.");
		ConfigValue currentValue = noUnset ? (isEnabled(key) ? ConfigValue.TRUE : ConfigValue.FALSE) : onlyBannable ? getValue(key) == ConfigValue.BANNED ? ConfigValue.BANNED : ConfigValue.UNSET : getValue(key);
		boolean keyEnabled = isEnabled(key);
		ConfigValue prevValue = animateDisabled ? currentValue : optionPreviousValues.getOrDefault(key, currentValue);
		int[] xes;
		if (noUnset) {
			xes = new int[] { 0, 23, 0, 0 };
		} else if (onlyBannable) {
			xes = new int[] { 30, 30, 30, 0 };
		} else {
			xes = new int[] { 30, 45, 15, 0 };
		}
		int[] hues = { 50, 130, -10, -90 };
		int[] values = { 90, 85, 90, 20 };
		int prevX = xes[prevValue.ordinal()];
		int prevHue = hues[prevValue.ordinal()];
		int prevHSValue = values[prevValue.ordinal()];
		int curX = xes[currentValue.ordinal()];
		int curHue = hues[currentValue.ordinal()];
		int curHSValue = values[currentValue.ordinal()];
		float a = sCurve5((5-time)/5f);
		float da = sCurve5((5-disabledTime)/5f);
		if (!disabled) {
			da = 1-da;
		}
		int trackSize = (noUnset?45:60);
		if (clientOnly) {
			fill(matrices, 133, 0, 134+trackSize+1, 11, 0xFFFFAA00);
		} else {
			fill(matrices, 133, 0, 134+trackSize+1, 11, 0xFFFFFFFF);
		}
		fill(matrices, 134, 1, 134+trackSize, 10, 0x66000000);
		if (!noUnset && !onlyBannable) {
			fill(matrices, 134+15, 1, 134+15+15, 10, 0x33000000);
			fill(matrices, 134+45, 1, 134+45+15, 10, 0x33000000);
		}
		matrices.push();
		matrices.translate(134+(prevX+((curX-prevX)*a)), 0, 0);
		int knobAlpha = ((int)((noValue ? 1-da : 1) * 255))<<24;
		fill(matrices, 0, 1, noUnset ? 22 : onlyBannable ? 30 : 15, 10, MathHelper.hsvToRgb(Math.floorMod((int)(prevHue+((curHue-prevHue)*a)), 360)/360f, 0.9f, (prevHSValue+((curHSValue-prevHSValue)*a))/100f)|knobAlpha);
		if (!noUnset && a >= 1 && currentValue == ConfigValue.UNSET && !onlyBannable) {
			fill(matrices, keyEnabled ? 15 : -1, 1, keyEnabled ? 16 : 0, 10, MathHelper.hsvToRgb((keyEnabled ? 120 : 0)/360f, 0.9f, 0.8f)|knobAlpha);
		}
		matrices.pop();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderTexture(0, new Identifier("fabrication", "configvalue.png"));
		RenderSystem.setShaderColor(1, 1, 1, 0.5f+((1-da)*0.5f));
		RenderSystem.enableTexture();
		if (noUnset) {
			drawTexture(matrices, 134+3, 1, 15, 0, 15, 9, 60, 9);
			drawTexture(matrices, 134+4+22, 1, 45, 0, 15, 9, 60, 9);
		} else {
			if (onlyBannable) {
				drawTexture(matrices, 134+7, 1, 0, 0, 15, 9, 60, 9);
				drawTexture(matrices, 134+38, 1, 30, 0, 15, 9, 60, 9);
			} else {
				drawTexture(matrices, 134, 1, 0, 0, 60, 9, 60, 9);
			}
		}

		RenderSystem.disableTexture();
		if (didClick) {
			if (mouseX >= 134 && mouseX <= 134+trackSize && mouseY >= y+1 && mouseY <= y+10) {
				float pitch = y*0.005f;
				if (disabled) {
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO, 0.8f, 1));
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO, 0.7f, 1));
					tooltipBlinkTicks = 20;
				} else {
					int clickedIndex = (int)((mouseX-134)/(noUnset ? 22 : onlyBannable ? 30 : 15));
					ConfigValue newValue;
					if (noUnset) {
						newValue = clickedIndex == 0 ? ConfigValue.FALSE : ConfigValue.TRUE;
					} else if (onlyBannable) {
						newValue = clickedIndex == 0 ? ConfigValue.BANNED : ConfigValue.UNSET;
					} else {
						switch (clickedIndex) {
							case 0:
								newValue = ConfigValue.BANNED;
								break;
							case 1:
								newValue = ConfigValue.FALSE;
								break;
							case 2:
								newValue = ConfigValue.UNSET;
								break;
							case 3:
								newValue = ConfigValue.TRUE;
								break;
							default:
								newValue = ConfigValue.UNSET;
								break;
						}
					}
					client.getSoundManager().play(PositionedSoundInstance.master(
							newValue == ConfigValue.BANNED ? SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM :
								newValue == ConfigValue.FALSE ? SoundEvents.BLOCK_NOTE_BLOCK_BASS :
									newValue == ConfigValue.UNSET ? SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL :
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
		int startX = 136+(noUnset ? 45 : 60)+5;
		int startStartX = startX;
		String section = null;
		if (showSourceSection && key.contains(".")) {
			section = key.substring(0, key.indexOf('.'));
			Identifier id = new Identifier("fabrication", "category/"+section+".png");
			RenderSystem.setShaderTexture(0, id);
			RenderSystem.setShaderColor(1, 1, 1, 1);
			drawTexture(matrices, startX-2, 0, 0, 0, 12, 12, 12, 12);
			startX += 14;
		}
		String drawTitle = title;
		String drawDesc = desc;
		if (highlightQueryMatch) {
			drawTitle = queryPattern.matcher(drawTitle).replaceAll("§e§l$0§r");
			drawDesc = queryPattern.matcher(drawDesc).replaceAll("§e§l$0§r");
		}
		y += drawWrappedText(matrices, startX, 2, drawTitle, width-startX-6, 0xFFFFFF | textAlpha, false)*scale;
		int endX = startY == y-8 ? width - 6 : startX+textRenderer.getWidth(title);
		//		int endX = textRenderer.draw(matrices, title, startX, 2, 0xFFFFFF | textAlpha);
		if (mouseX >= 134+trackSize && mouseX <= endX && mouseY >= startY+1 && mouseY <= startY+10 && FeaturesFile.get(key).fscript != null && Agnos.isModLoaded("fscript")){
			if (didClick){
				client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1f));
				client.setScreen(OptionalFScriptScreen.construct(this, prideFlag, title, key));
			}
			fill(matrices, startX-2, 9, endX, 10, -1);
		}
		matrices.pop();
		if ((("search".equals(selectedSection) ? false : mouseX <= width-120) || mouseY >= 16) && mouseY < height-20) {
			if (section != null && mouseX >= startStartX && mouseX <= startX && mouseY >= startY && mouseY <= y) {
				renderWrappedTooltip(matrices, FeaturesFile.get(section).shortName, mouseX, mouseY);
			} else if (mouseX >= startX && mouseX <= endX && mouseY >= startY && mouseY <= y) {
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
				renderWrappedTooltip(matrices, prefix+drawDesc, mouseX, mouseY);
			} else if (mouseX >= 134 && mouseX <= 134+trackSize && mouseY >= startY && mouseY <= startY+10) {
				if (disabled) {
					if (noFabricApi) {
						renderTooltip(matrices, new LiteralText(((tooltipBlinkTicks/5)%2 == 1 ? "§c" : "")+"This option requires Fabric API"), (int)mouseX, (int)mouseY);
					} else if (noValue) {
						renderTooltip(matrices, new LiteralText(((tooltipBlinkTicks/5)%2 == 1 ? "§c" : "")+"The server does not recognize this option"), (int)mouseX, (int)mouseY);
					} else if (failed) {
						renderTooltip(matrices, new LiteralText(((tooltipBlinkTicks/5)%2 == 1 ? "§c" : "")+"This feature failed to initialize"), (int)mouseX, (int)mouseY);
					} else if (banned) {
						renderTooltip(matrices, new LiteralText(((tooltipBlinkTicks/5)%2 == 1 ? "§c" : "")+"This feature is banned by the server"), (int)mouseX, (int)mouseY);
					} else {
						renderTooltip(matrices, new LiteralText(((tooltipBlinkTicks/5)%2 == 1 ? "§c" : "")+"You cannot configure this server"), (int)mouseX, (int)mouseY);
					}
				} else {
					int index = (int)((mouseX-134)/(noUnset ? 22 : onlyBannable ? 30 : 15));
					if (onlyBannable) {
						if (index == 0) {
							renderTooltip(matrices, Lists.newArrayList(
									new LiteralText("§7Ban"),
									new LiteralText("Disallow use by clients")
									), (int)mouseX, (int)mouseY);
						} else {
							renderTooltip(matrices, Lists.newArrayList(
									new LiteralText("§eUnset"),
									new LiteralText("Allow use by clients")
									), (int)mouseX, (int)mouseY);
						}
					} else {
						if (index == (noUnset ? 0 : 1)) {
							renderTooltip(matrices, new LiteralText("§cDisable"), (int)mouseX, (int)mouseY);
						} else if (index == (noUnset ? -99 : 2)) {
							if (currentValue == ConfigValue.UNSET) {
								renderTooltip(matrices, Lists.newArrayList(
										new LiteralText("§eUse default value §f(see General > Profile)"),
										new LiteralText("§rCurrent default: "+(keyEnabled ? "§aEnabled" : "§cDisabled"))
										), (int)mouseX, (int)mouseY);
							} else {
								renderTooltip(matrices, new LiteralText("§eUse default value §f(see General > Profile)"), (int)mouseX, (int)mouseY);
							}
						} else if (index == 0) {
							List<Text> li = Lists.newArrayList(
									new LiteralText("§7Ban"),
									new LiteralText("Prevent feature from loading entirely")
									);
							if (configuringServer) {
								li.add(new LiteralText("and disallow usage by clients"));
							}
							renderTooltip(matrices, li, (int)mouseX, (int)mouseY);
						} else {
							renderTooltip(matrices, new LiteralText("§aEnable"), (int)mouseX, (int)mouseY);
						}
					}
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
		RenderSystem.setShaderColor(((packed>>16)&0xFF)/255f, ((packed>>8)&0xFF)/255f, ((packed>>0)&0xFF)/255f, alpha);
	}

	@Override
	public void onClose() {
		if (!MixinConfigPlugin.isEnabled("*.reduced_motion") && !leaving) {
			leaving = true;
			client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_BARREL_CLOSE, 0.7f));
			client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_SHROOMLIGHT_PLACE, 2f, 1f));
		} else {
			client.setScreen(parent);
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
		searchField.tick();
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
		if ("search".equals(selectedSection)) {
			searchField.mouseClicked(mouseX, mouseY, button);
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if ("search".equals(selectedSection)) {
			searchField.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		if ("search".equals(selectedSection)) {
			searchField.mouseMoved(mouseX, mouseY);
		}
		super.mouseMoved(mouseX, mouseY);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if ("search".equals(selectedSection)) {
			searchField.mouseReleased(mouseX, mouseY, button);
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if ("search".equals(selectedSection)) {
			searchField.charTyped(chr, modifiers);
		}
		return super.charTyped(chr, modifiers);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if ("search".equals(selectedSection)) {
			searchField.keyPressed(keyCode, scanCode, modifiers);
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		if ("search".equals(selectedSection)) {
			searchField.keyReleased(keyCode, scanCode, modifiers);
		}
		return super.keyReleased(keyCode, scanCode, modifiers);
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
			return MixinConfigPlugin.isStandardValue(key);
		}
	}

	private ResolvedConfigValue getResolvedValue(String key) {
		if (configuringServer) {
			return ((GetServerConfig)client.getNetworkHandler()).fabrication$getServerTrileanConfig().getOrDefault(key, ResolvedConfigValue.DEFAULT_FALSE);
		} else {
			return MixinConfigPlugin.getResolvedValue(key);
		}
	}

	private ConfigValue getValue(String key) {
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
		Set<String> newlyBannedKeys;
		Set<String> newlyUnbannedKeys;

		if (configuringServer) {
			checkServerData();
			newlyBannedKeys = newlyBannedKeysServer;
			newlyUnbannedKeys = newlyUnbannedKeysServer;
		} else {
			newlyBannedKeys = newlyBannedKeysClient;
			newlyUnbannedKeys = newlyUnbannedKeysClient;
		}
		String oldValue = getRawValue(key);
		if (!MixinConfigPlugin.isRuntimeConfigurable(key) && !(configuringServer && FeaturesFile.get(key).sides == Sides.CLIENT_ONLY)) {
			if (value.equals("banned")) {
				if (newlyUnbannedKeys.contains(key)) {
					newlyUnbannedKeys.remove(key);
				} else {
					newlyBannedKeys.add(key);
				}
			} else if (oldValue.equals("banned")) {
				if (newlyBannedKeys.contains(key)) {
					newlyBannedKeys.remove(key);
				} else {
					newlyUnbannedKeys.add(key);
				}
			}
		}
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
