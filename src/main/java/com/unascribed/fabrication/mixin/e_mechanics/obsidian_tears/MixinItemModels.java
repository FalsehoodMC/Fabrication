package com.unascribed.fabrication.mixin.e_mechanics.obsidian_tears;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

@Mixin(ItemModels.class)
@EligibleIf(configEnabled="*.obsidian_tears", envMatches=Env.CLIENT)
public abstract class MixinItemModels {
	
	@Unique
	private BakedModel fabrication$obsidianTearsModel = null;

	@Shadow
	public abstract BakedModelManager getModelManager();
	
	@Inject(at=@At("HEAD"), method="getModel(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;",
			cancellable=true)
	public void getModel(ItemStack stack, CallbackInfoReturnable<BakedModel> ci) {
		if (!RuntimeChecks.check("*.obsidian_tears")) return;
		if (stack.getItem() == Items.POTION && stack.hasTag() && stack.getTag().getBoolean("fabrication:ObsidianTears")) {
			ci.setReturnValue(fabrication$obsidianTearsModel);
		}
	}
	
	@Inject(at=@At("TAIL"), method="reloadModels()V")
	public void reloadModels(CallbackInfo ci) {
		fabrication$obsidianTearsModel = getModelManager().getModel(new ModelIdentifier(new Identifier("fabrication", "obsidian_tears"), "inventory"));
	}
	
}
