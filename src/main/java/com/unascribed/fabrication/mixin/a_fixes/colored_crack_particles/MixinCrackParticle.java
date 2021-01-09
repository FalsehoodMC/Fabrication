package com.unascribed.fabrication.mixin.a_fixes.colored_crack_particles;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.FabRefl;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.CrackParticle;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.ItemStack;

@Mixin(CrackParticle.class)
@EligibleIf(configEnabled="*.colored_crack_particles")
public abstract class MixinCrackParticle extends SpriteBillboardParticle {

	protected MixinCrackParticle(ClientWorld arg, double d, double e, double f) {
		super(arg, d, e, f);
	}

	@Inject(at=@At("TAIL"), method="<init>(Lnet/minecraft/client/world/ClientWorld;DDDLnet/minecraft/item/ItemStack;)V")
	public void construct(ClientWorld world, double x, double y, double z, ItemStack stack, CallbackInfo ci) {
		int c = FabRefl.Client.getItemColors(MinecraftClient.getInstance()).getColorMultiplier(stack, 0);
		setColor(((c>>16)&0xFF)/255f, ((c>>8)&0xFF)/255f, (c&0xFF)/255f);
	}
	
}
