package com.unascribed.fabrication.util;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.world.ClientWorld;

public class DepthSuspendParticle extends SpriteBillboardParticle {

	public DepthSuspendParticle(ClientWorld world, double x, double y, double z) {
		super(world, x, y, z);
		float brightness = this.random.nextFloat() * 0.1F + 0.2F;
		this.colorRed = brightness;
		this.colorGreen = brightness;
		this.colorBlue = brightness;
		this.setBoundingBoxSpacing(0.02F, 0.02F);
		//source specified nextFloat() * 0.6F + 0.5F, which is too big since extending SpriteBillboardParticle
		this.scale(this.random.nextFloat() * 0.2F);
		this.velocityX = (Math.random() * 2.0D - 1.0D) * 0.4F;
		this.velocityY = (Math.random() * 2.0D - 1.0D) * 0.4F;
		this.velocityZ = (Math.random() * 2.0D - 1.0D) * 0.4F;
		double d1 = (Math.random() + Math.random() + 1) * 0.15;
		double d2 = Math.sqrt(this.velocityX * this.velocityX + this.velocityY * this.velocityY + this.velocityZ * this.velocityZ);
		this.velocityX = this.velocityX / d2 * d1 * 0.00799999940D;
		this.velocityY = (this.velocityY / d2 * d1 * 0.4000000059604645D + 0.10000000149011612D) * 0.019999999552965164D;
		this.velocityZ = this.velocityZ / d2 * d1 * 0.00799999940D;
		this.maxAge = (int)(20.0D / (Math.random() * 0.8D + 0.2D));
		this.collidesWithWorld = false;

		//definitely hacky, but i give up trying to understand how to add a single white pixel
		this.setSprite(MinecraftClient.getInstance().getBlockRenderManager().getModels().getSprite(Blocks.WHITE_WOOL.getDefaultState()));
	}
	public void tick(){
		if (this.age++ >= this.maxAge) {
			this.markDead();
			return;
		}
		this.prevPosX = this.x;
		this.prevPosY = this.y;
		this.prevPosZ = this.z;
		this.move(this.velocityX, this.velocityY, this.velocityZ);
		this.velocityX *= 0.99;
		this.velocityY *= 0.99;
		this.velocityZ *= 0.99;
	}
	public ParticleTextureSheet getType() {
		return ParticleTextureSheet.TERRAIN_SHEET;
	}


	protected float getMinU() {
		return this.sprite.getFrameU(0);
	}

	protected float getMaxU() {
		return this.sprite.getFrameU(1);
	}

	protected float getMinV() {
		return this.sprite.getFrameV(0);
	}

	protected float getMaxV() {
		return this.sprite.getFrameV(1);
	}


}
