package com.unascribed.fabrication.mixin.e_mechanics.obsidian_tears;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.SpecialEligibility;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

@Mixin(ItemModels.class)
@EligibleIf(configAvailable="*.obsidian_tears", envMatches=Env.CLIENT)
@FailOn(invertedSpecialConditions=SpecialEligibility.NOT_FORGE)
public abstract class MixinItemModels {

	@Unique
	private BakedModel fabrication$obsidianTearsModel = null;

	@Shadow
	public abstract BakedModelManager getModelManager();

	@FabInject(at=@At("HEAD"), method="getModel(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;",
			cancellable=true, require=0)
	public void getModel(ItemStack stack, CallbackInfoReturnable<BakedModel> ci) {
		if (!FabConf.isEnabled("*.obsidian_tears")) return;
		if (stack.getItem() == Items.POTION && stack.hasNbt() && stack.getNbt().getBoolean("fabrication:ObsidianTears") && fabrication$obsidianTearsModel != null) {
			ci.setReturnValue(fabrication$obsidianTearsModel);
		}
	}

	@FabInject(at=@At("TAIL"), method="reloadModels()V", require=0)
	public void reloadModels(CallbackInfo ci) {
		fabrication$obsidianTearsModel = getModelManager().getModel(new ModelIdentifier(new Identifier("fabrication", "obsidian_tears"), "inventory"));
	}

}
