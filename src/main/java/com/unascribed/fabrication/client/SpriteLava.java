package com.unascribed.fabrication.client;

import com.unascribed.fabrication.features.FeatureOldLava;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.math.MathHelper;

public class SpriteLava extends Sprite
{
	protected float[] field_76876_g = new float[256];
	protected float[] field_76878_h = new float[256];
	protected float[] field_76879_i = new float[256];
	protected float[] field_76877_j = new float[256];

	public SpriteLava(SpriteAtlasTexture spriteAtlasTexture,
			Info info, int maxLevel, int atlasWidth, int atlasHeight, int x,
			int y) {
		super(spriteAtlasTexture, info, maxLevel, atlasWidth, atlasHeight, x, y,
				new NativeImage(16, 16, false));
	}


	@Override
	public void tickAnimation() {
		// I cannot be bothered to deobfuscate this the rest of the way.
		
		int var2;
		float var3;
		int var5;
		int var6;
		int var7;
		int var8;
		int var9;

		for (int var1 = 0; var1 < 16; ++var1)
		{
			for (var2 = 0; var2 < 16; ++var2)
			{
				var3 = 0.0F;
				int var4 = (int)(MathHelper.sin(var2 * (float)Math.PI * 2.0F / 16.0F) * 1.2F);
				var5 = (int)(MathHelper.sin(var1 * (float)Math.PI * 2.0F / 16.0F) * 1.2F);

				for (var6 = var1 - 1; var6 <= var1 + 1; ++var6)
				{
					for (var7 = var2 - 1; var7 <= var2 + 1; ++var7)
					{
						var8 = var6 + var4 & 15;
						var9 = var7 + var5 & 15;
						var3 += this.field_76876_g[var8 + var9 * 16];
					}
				}

				this.field_76878_h[var1 + var2 * 16] = var3 / 10.0F + (this.field_76879_i[(var1 & 15) + (var2 & 15) * 16] + this.field_76879_i[(var1 + 1 & 15) + (var2 & 15) * 16] + this.field_76879_i[(var1 + 1 & 15) + (var2 + 1 & 15) * 16] + this.field_76879_i[(var1 & 15) + (var2 + 1 & 15) * 16]) / 4.0F * 0.8F;
				this.field_76879_i[var1 + var2 * 16] += this.field_76877_j[var1 + var2 * 16] * 0.01F;

				if (this.field_76879_i[var1 + var2 * 16] < 0.0F)
				{
					this.field_76879_i[var1 + var2 * 16] = 0.0F;
				}

				this.field_76877_j[var1 + var2 * 16] -= 0.06F;

				if (Math.random() < 0.005D)
				{
					this.field_76877_j[var1 + var2 * 16] = 1.5F;
				}
			}
		}

		float[] var11 = this.field_76878_h;
		this.field_76878_h = this.field_76876_g;
		this.field_76876_g = var11;

		for (var2 = 0; var2 < 256; ++var2)
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

			images[0].setPixelColor(var2%16, var2/16, (var5) | (var6 << 8) | (var7 << 16) | 0xFF000000);
		}
		FeatureOldLava.mip(images);
		upload();
	}
}
