package com.unascribed.fabrication.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static org.lwjgl.opengl.GL14.*;

import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.unascribed.fabrication.FabRefl;

import com.google.common.collect.Lists;

public class AtlasViewerScreen extends Screen {

	private static final Identifier CHECKER = new Identifier("fabrication", "textures/checker.png");
	
	private final Identifier atlas;
	private float panX = 100;
	private float panY = 100;
	private int level = 0;
	
	public AtlasViewerScreen(Identifier atlas) {
		super(new LiteralText("Atlas viewer"));
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
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		// GlStateManager can get fucked
		glPushAttrib(GL_ALL_ATTRIB_BITS);
			glPushMatrix();
			glClearColor(0.3f, 0.3f, 0.3f, 1);
			glClear(GL_COLOR_BUFFER_BIT);

			glDisable(GL_DEPTH_TEST);
			glDisable(GL_CULL_FACE);
			glDisable(GL_BLEND);
			glDisable(GL_ALPHA_TEST);
			
			client.getTextureManager().bindTexture(CHECKER);
			glEnable(GL_TEXTURE_2D);
			glColor4f(1, 1, 1, 1);
			glBegin(GL_QUADS);
				glTexCoord2f(0, 0);
				glVertex2f(0, 0);
				glTexCoord2f(width/8, 0);
				glVertex2f(width, 0);
				glTexCoord2f(width/8, height/8);
				glVertex2f(width, height);
				glTexCoord2f(0, height/8);
				glVertex2f(0, height);
			glEnd();
			
			client.getTextureManager().bindTexture(atlas);
			glDisable(GL_POLYGON_STIPPLE);
			
			glTranslatef(panX, panY, 0);
			
			int atlasWidth = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
			int atlasHeight = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);
			int atlasMaxLevel = glGetTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL);
			
			glDisable(GL_TEXTURE_2D);
			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			glColor4f(1f, 1f, 1f, 0.15f);
			glBegin(GL_QUADS);
				glVertex2f(0, 0);
				glVertex2f(atlasWidth, 0);
				glVertex2f(atlasWidth, atlasHeight);
				glVertex2f(0, atlasHeight);
			glEnd();
			
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, level);
			
			glEnable(GL_TEXTURE_2D);
			glColor4f(1, 1, 1, 1);
			glBegin(GL_QUADS);
				glTexCoord2f(0, 0);
				glVertex2f(0, 0);
				glTexCoord2f(1, 0);
				glVertex2f(atlasWidth, 0);
				glTexCoord2f(1, 1);
				glVertex2f(atlasWidth, atlasHeight);
				glTexCoord2f(0, 1);
				glVertex2f(0, atlasHeight);
			glEnd();
			
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
			glPopMatrix();
		glPopAttrib();
		
		mouseX -= panX;
		mouseY -= panY;
		
		
		SpriteAtlasTexture sat = getAtlas();
		renderTooltip(matrices, Lists.<Text>newArrayList(
				new LiteralText(atlas.toString()),
				new LiteralText("§7"+atlasWidth+"×"+atlasHeight+"×"+(atlasMaxLevel+1)+" @"+(level+1)+" §f|§7 "+FabRefl.Client.getSprites(sat).size()+" sprites")
			), -9, 15);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		List<Sprite> sprites = Lists.newArrayList();
		for (Sprite s : FabRefl.Client.getSprites(sat).values()) {
			int x = FabRefl.Client.getX(s);
			int y = FabRefl.Client.getY(s);
			int w = s.getWidth();
			int h = s.getHeight();
			if (mouseX >= x && mouseX < x+w && mouseY >= y && mouseY < y+h) {
				sprites.add(s);
			}
		}
		GlStateManager.disableCull();
		GlStateManager.disableTexture();
		for (Sprite s : sprites) {
			int x = FabRefl.Client.getX(s);
			int y = FabRefl.Client.getY(s);
			int w = s.getWidth();
			int h = s.getHeight();
			GlStateManager.color4f(1, 0, 0, 0.2f);
			glBegin(GL_QUADS);
				glVertex2f(panX+x, panY+y);
				glVertex2f(panX+x+w, panY+y);
				glVertex2f(panX+x+w, panY+y+h);
				glVertex2f(panX+x, panY+y+h);
			glEnd();
		}
		GlStateManager.enableTexture();
		if (sprites.isEmpty()) {
			if (mouseX >= 0 && mouseY >= 0 && mouseX < atlasWidth && mouseY < atlasHeight) {
				renderTooltip(matrices, Lists.<Text>newArrayList(
						new LiteralText("<nothing>"),
						new LiteralText("§7"+mouseX+", "+mouseY)
					), (int)(mouseX+panX), (int)(mouseY+panY));
			}
		} else if (sprites.size() == 1) {
			Sprite s = sprites.get(0);
			int x = FabRefl.Client.getX(s);
			int y = FabRefl.Client.getY(s);
			int w = s.getWidth();
			int h = s.getHeight();
			Identifier tex = new Identifier(s.getId().getNamespace(), "textures/"+s.getId().getPath()+".png");
			String src;
			if (s.getClass() == Sprite.class) {
				try {
					src = client.getResourceManager().getResource(tex).getResourcePackName();
				} catch (Throwable t) {
					t.printStackTrace();
					src = "??";
				}
			} else {
				src = "Custom Sprite subclass "+s.getClass().getName();
			}
			renderTooltip(matrices, Lists.<Text>newArrayList(
				new LiteralText(s.getId().toString()),
				new LiteralText("§7At "+x+","+y+" "+w+"×"+h+"×"+s.getFrameCount()+" @"+FabRefl.Client.getFrameIndex(s)+"."+FabRefl.Client.getFrameTicks(s)),
				new LiteralText("§7From §f"+src)
			), (int)(mouseX+panX), (int)(mouseY+panY));
		} else {
			List<Text> li = Lists.newArrayList(
				new LiteralText("§c\u26A0 MULTIPLE SPRITES \u26A0")
			);
			for (Sprite s : sprites) {
				int x = FabRefl.Client.getX(s);
				int y = FabRefl.Client.getY(s);
				int w = s.getWidth();
				int h = s.getHeight();
				li.add(new LiteralText(s.getId().toString()));
				li.add(new LiteralText("§7At "+x+","+y+" "+w+"×"+h+"×"+s.getFrameCount()+" @"+FabRefl.Client.getFrameIndex(s)+"."+FabRefl.Client.getFrameTicks(s)));
			}
			renderTooltip(matrices, li, (int)(mouseX+panX), (int)(mouseY+panY));
		}
	}
	
	@Override
	public void renderTooltip(MatrixStack matrices, List<Text> lines, int x, int y) {
		List<OrderedText> ordered = Lists.newArrayList();
		for (Text t : lines) {
			ordered.addAll(textRenderer.wrapLines(t, 240));
		}
		renderOrderedTooltip(matrices, ordered, x, y);
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
