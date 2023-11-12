package com.unascribed.fabrication.mixin.a_fixes.colored_crack_particles;

import com.unascribed.fabrication.FabConf;
import net.minecraft.item.PotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.FabRefl;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.CrackParticle;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;

@Mixin(CrackParticle.class)
@EligibleIf(configAvailable="*.colored_crack_particles", envMatches=Env.CLIENT)
public abstract class MixinCrackParticle extends SpriteBillboardParticle {

	protected MixinCrackParticle(ClientWorld arg, double d, double e, double f) {
		super(arg, d, e, f);
	}

	@FabInject(at=@At("TAIL"), method="<init>(Lnet/minecraft/client/world/ClientWorld;DDDLnet/minecraft/item/ItemStack;)V")
	public void construct(ClientWorld world, double x, double y, double z, ItemStack stack, CallbackInfo ci) {
		if (!FabConf.isEnabled("*.colored_crack_particles")) return;
		if (stack.getItem() instanceof PotionItem) return;
		int c = FabRefl.Client.getItemColors(MinecraftClient.getInstance()).getColorMultiplier(stack, 0);
		setColor(((c>>16)&0xFF)/255f, ((c>>8)&0xFF)/255f, (c&0xFF)/255f);
	}

}
