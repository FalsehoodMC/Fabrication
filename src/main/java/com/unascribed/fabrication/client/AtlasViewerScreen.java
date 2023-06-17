package com.unascribed.fabrication.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.Resource;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static org.lwjgl.opengl.GL30C.*;

import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;
import com.unascribed.fabrication.FabRefl;

import com.google.common.collect.Lists;
import org.joml.Matrix4f;

public class AtlasViewerScreen extends Screen {

	private static final Identifier CHECKER = new Identifier("fabrication", "textures/checker.png");

	private final Identifier atlas;
	private float panX = 100;
	private float panY = 100;
	private int level = 0;

	public AtlasViewerScreen(Identifier atlas) {
		super(Text.literal("Atlas viewer"));
		this.atlas = atlas;
	}

	@Override
	protected void init() {
		client.skipGameRender = true;
	}

	@Override
	public void removed() {
		client.skipGameRender = false;
	}

	@Override
	public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
		MatrixStack matrices = drawContext.getMatrices();
		matrices.push();
		RenderSystem.clearColor(0.3f, 0.3f, 0.3f, 1);
		RenderSystem.clear(GL_COLOR_BUFFER_BIT, false);

		RenderSystem.disableDepthTest();
		RenderSystem.disableCull();
		RenderSystem.disableBlend();

		client.getTextureManager().bindTexture(CHECKER);
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderTexture(0, CHECKER);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		BufferBuilder bb = Tessellator.getInstance().getBuffer();
		Matrix4f mat = matrices.peek().getPositionMatrix();
		bb.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
			bb.vertex(mat, 0, 0, 0).texture(0, 0).next();
			bb.vertex(mat, width, 0, 0).texture(width/8, 0).next();
			bb.vertex(mat, width, height, 0).texture(width/8, height/8).next();
			bb.vertex(mat, 0, height, 0).texture(0, height/8).next();
		BufferRenderer.drawWithGlobalProgram(bb.end());

		client.getTextureManager().bindTexture(atlas);

		matrices.translate(panX, panY, 0);

		int atlasWidth = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
		int atlasHeight = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);
		int atlasMaxLevel = glGetTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL);

		RenderSystem.setShader(GameRenderer::getPositionProgram);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(1f, 1f, 1f, 0.15f);
		mat = matrices.peek().getPositionMatrix();
		bb.begin(DrawMode.QUADS, VertexFormats.POSITION);
			bb.vertex(mat, 0, 0, 0).next();
			bb.vertex(mat, atlasWidth, 0, 0).next();
			bb.vertex(mat, atlasWidth, atlasHeight, 0).next();
			bb.vertex(mat, 0, atlasHeight, 0).next();
		BufferRenderer.drawWithGlobalProgram(bb.end());

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, level);

		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderTexture(0, atlas);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		mat = matrices.peek().getPositionMatrix();
		bb.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
			bb.vertex(mat, 0, 0, 0).texture(0, 0).next();
			bb.vertex(mat, atlasWidth, 0, 0).texture(1, 0).next();
			bb.vertex(mat, atlasWidth, atlasHeight, 0).texture(1, 1).next();
			bb.vertex(mat, 0, atlasHeight, 0).texture(0, 1).next();
		BufferRenderer.drawWithGlobalProgram(bb.end());

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
		matrices.pop();

		mouseX -= panX;
		mouseY -= panY;


		SpriteAtlasTexture sat = getAtlas();
		renderTooltip(drawContext, Lists.<Text>newArrayList(
				Text.literal(atlas.toString()),
				Text.literal("§7"+atlasWidth+"×"+atlasHeight+"×"+(atlasMaxLevel+1)+" @"+(level+1)+" §f|§7 "+FabRefl.Client.getSprites(sat).size()+" sprites")
			), -9, 15);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		List<Sprite> sprites = Lists.newArrayList();
		for (Sprite s : FabRefl.Client.getSprites(sat).values()) {
			int x = FabRefl.Client.getX(s);
			int y = FabRefl.Client.getY(s);
			int w = s.getContents().getWidth();
			int h = s.getContents().getHeight();
			if (mouseX >= x && mouseX < x+w && mouseY >= y && mouseY < y+h) {
				sprites.add(s);
			}
		}
		RenderSystem.disableCull();
		RenderSystem.setShader(GameRenderer::getPositionProgram);
		for (Sprite s : sprites) {
			int x = FabRefl.Client.getX(s);
			int y = FabRefl.Client.getY(s);
			int w = s.getContents().getWidth();
			int h = s.getContents().getHeight();
			RenderSystem.setShaderColor(1, 0, 0, 0.2f);
			mat = matrices.peek().getPositionMatrix();
			bb.begin(DrawMode.QUADS, VertexFormats.POSITION);
				bb.vertex(mat, panX+x, panY+y, 0).next();
				bb.vertex(mat, panX+x+w, panY+y, 0).next();
				bb.vertex(mat, panX+x+w, panY+y+h, 0).next();
				bb.vertex(mat, panX+x, panY+y+h, 0).next();
			BufferRenderer.drawWithGlobalProgram(bb.end());
		}
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		if (sprites.isEmpty()) {
			if (mouseX >= 0 && mouseY >= 0 && mouseX < atlasWidth && mouseY < atlasHeight) {
				renderTooltip(drawContext, Lists.<Text>newArrayList(
						Text.literal("<nothing>"),
						Text.literal("§7"+mouseX+", "+mouseY),
						Text.literal("If there is something here, it's either garbage from your graphics driver or an unregistered sprite")
					), (int)(mouseX+panX), (int)(mouseY+panY));
			}
		} else if (sprites.size() == 1) {
			Sprite s = sprites.get(0);
			int x = FabRefl.Client.getX(s);
			int y = FabRefl.Client.getY(s);
			int w = s.getContents().getWidth();
			int h = s.getContents().getHeight();
			Identifier tex = new Identifier(s.getContents().getId().getNamespace(), "textures/"+s.getContents().getId().getPath()+".png");
			String src = "??";
			if (s.getClass() == Sprite.class) {
				try {
					Optional<Resource> opt = client.getResourceManager().getResource(tex);
					if (opt.isPresent()) {
						src = opt.get().getResourcePackName();
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			} else {
				src = "Custom Sprite subclass "+s.getClass().getName();
			}
			String anim = "";
			/*if (animation != null) {
				Sprite.Animation sa = (Sprite.Animation)s.getAnimation();
				anim = FabRefl.Client.getFrameCount(s)+" @"+FabRefl.Client.getFrameIndex(sa)+"."+FabRefl.Client.getFrameTicks(sa);
			}*/
			renderTooltip(drawContext, Lists.<Text>newArrayList(
				Text.literal(s.getContents().getId().toString()),
				Text.literal("§7At "+x+","+y+" "+w+"×"+h+"×"+anim),
				Text.literal("§7From §f"+src)
			), (int)(mouseX+panX), (int)(mouseY+panY));
		} else {
			List<Text> li = Lists.newArrayList(
				Text.literal("§c\u26A0 MULTIPLE SPRITES \u26A0")
			);
			for (Sprite s : sprites) {
				int x = FabRefl.Client.getX(s);
				int y = FabRefl.Client.getY(s);
				int w = s.getContents().getWidth();
				int h = s.getContents().getHeight();
				li.add(Text.literal(s.getContents().getId().toString()));
				String anim = "";
				/*if (animation != null) {
					anim = FabRefl.Client.getFrameCount(s)+" @"+FabRefl.Client.getFrameIndex(sa)+"."+FabRefl.Client.getFrameTicks(sa);
				}*/
				li.add(Text.literal("§7At "+x+","+y+" "+w+"×"+h+"×"+anim));
			}
			renderTooltip(drawContext, li, (int)(mouseX+panX), (int)(mouseY+panY));
		}
	}

	public void renderTooltip(DrawContext matrices, List<Text> lines, int x, int y) {
		List<OrderedText> ordered = Lists.newArrayList();
		for (Text t : lines) {
			ordered.addAll(textRenderer.wrapLines(t, 240));
		}
		matrices.drawOrderedTooltip(textRenderer, ordered, x, y);
	}

	private SpriteAtlasTexture getAtlas() {
		for (SpriteAtlasTexture cand : AtlasTracking.allAtlases) {
			if (cand.getId().equals(this.atlas)) return cand;
		}
		return null;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (button == 0) {
			panX += deltaX;
			panY += deltaY;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		int oldBind = glGetInteger(GL_TEXTURE_BINDING_2D);
		SpriteAtlasTexture sat = getAtlas();
		if (sat != null) {
			glBindTexture(GL_TEXTURE_2D, sat.getGlId());
			int maxLevel = glGetTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL);
			level += amount;
			if (level > maxLevel) level = maxLevel;
			if (level < 0) level = 0;
		}
		glBindTexture(GL_TEXTURE_2D, oldBind);
		return true;
	}

}

