package com.unascribed.fabrication.client;

import com.unascribed.fabrication.features.FeatureOldLava;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureTickListener;
import net.minecraft.util.math.MathHelper;

public class SpriteLava extends Sprite implements TextureTickListener {
	protected int SPRITE_SIZE;
	protected int SPRITE_SIZEMINUSONE;
	protected int SPRITE_SIZESQUARED;
	protected float[] field_76876_g;
	protected float[] field_76878_h;
	protected float[] field_76879_i;
	protected float[] field_76877_j;

	public SpriteLava(SpriteAtlasTexture spriteAtlasTexture,
			Info info, int maxLevel, int atlasWidth, int atlasHeight, int x,
			int y, NativeImage image) {
		super(spriteAtlasTexture, info, maxLevel, atlasWidth, atlasHeight, x, y, image);
		SPRITE_SIZE = info.getWidth();
		SPRITE_SIZEMINUSONE = SPRITE_SIZE - 1;
		SPRITE_SIZESQUARED = SPRITE_SIZE * SPRITE_SIZE;

		field_76876_g = new float[SPRITE_SIZESQUARED];
		field_76878_h = new float[SPRITE_SIZESQUARED];
		field_76879_i = new float[SPRITE_SIZESQUARED];
		field_76877_j = new float[SPRITE_SIZESQUARED];
	}


	@Override
	public void tick() {
		tickAnimation();
		FeatureOldLava.mip(images);
		upload();
	}

	public void tickAnimation() {
		// I cannot be bothered to deobfuscate this the rest of the way.

		int var2;
		float var3;
		int var5;
		int var6;
		int var7;
		int var8;
		int var9;

		for (int var1 = 0; var1 < SPRITE_SIZE; ++var1)
		{
			for (var2 = 0; var2 < SPRITE_SIZE; ++var2)
			{
				var3 = 0.0F;
				int var4 = (int)(MathHelper.sin(var2 * (float)Math.PI * 2.0F / 16.0F) * 1.2F);
				var5 = (int)(MathHelper.sin(var1 * (float)Math.PI * 2.0F / 16.0F) * 1.2F);

				for (var6 = var1 - 1; var6 <= var1 + 1; ++var6)
				{
					for (var7 = var2 - 1; var7 <= var2 + 1; ++var7)
					{
						var8 = var6 + var4 & SPRITE_SIZEMINUSONE;
						var9 = var7 + var5 & SPRITE_SIZEMINUSONE;
						var3 += this.field_76876_g[var8 + var9 * SPRITE_SIZE];
					}
				}

				this.field_76878_h[var1 + var2 * SPRITE_SIZE] = var3 / 10.0F + (this.field_76879_i[(var1 + 0 & SPRITE_SIZEMINUSONE) + (var2 + 0 & SPRITE_SIZEMINUSONE) * SPRITE_SIZE] + this.field_76879_i[(var1 + 1 & SPRITE_SIZEMINUSONE) + (var2 + 0 & SPRITE_SIZEMINUSONE) * SPRITE_SIZE] + this.field_76879_i[(var1 + 1 & SPRITE_SIZEMINUSONE) + (var2 + 1 & SPRITE_SIZEMINUSONE) * SPRITE_SIZE] + this.field_76879_i[(var1 + 0 & SPRITE_SIZEMINUSONE) + (var2 + 1 & SPRITE_SIZEMINUSONE) * SPRITE_SIZE]) / 4.0F * 0.8F;
				this.field_76879_i[var1 + var2 * SPRITE_SIZE] += this.field_76877_j[var1 + var2 * SPRITE_SIZE] * 0.01F;

				if (this.field_76879_i[var1 + var2 * SPRITE_SIZE] < 0.0F)
				{
					this.field_76879_i[var1 + var2 * SPRITE_SIZE] = 0.0F;
				}

				this.field_76877_j[var1 + var2 * SPRITE_SIZE] -= 0.06F;

				if (Math.random() < 0.005D)
				{
					this.field_76877_j[var1 + var2 * SPRITE_SIZE] = 1.5F;
				}
			}
		}

		float[] var11 = this.field_76878_h;
		this.field_76878_h = this.field_76876_g;
		this.field_76876_g = var11;

		for (var2 = 0; var2 < SPRITE_SIZESQUARED; ++var2)
		{
			var3 = this.field_76876_g[var2] * 2.0F;

			if (var3 > 1.0F)
			{
				var3 = 1.0F;
			}

			if (var3 < 0.0F)
			{
				var3 = 0.0F;
			}

			var5 = (int)(var3 * 100.0F + 155.0F);
			var6 = (int)(var3 * var3 * 255.0F);
			var7 = (int)(var3 * var3 * var3 * var3 * 128.0F);

			images[0].setColor(var2%SPRITE_SIZE, var2/SPRITE_SIZE, (var5) | (var6 << 8) | (var7 << 16) | 0xFF000000);
		}
	}
}
