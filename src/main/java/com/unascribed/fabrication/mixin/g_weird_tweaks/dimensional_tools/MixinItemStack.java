package com.unascribed.fabrication.mixin.g_weird_tweaks.dimensional_tools;

import com.unascribed.fabrication.support.DimensionalTools;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
@EligibleIf(configAvailable="*.dimensional_tools")
public abstract class MixinItemStack {

	@Inject(at=@At("RETURN"), method="getTooltip(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/client/item/TooltipContext;)Ljava/util/List;")
	public void getTooltip(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir) {
		if (!MixinConfigPlugin.isEnabled("*.dimensional_tools")) return;
		ItemStack item = (ItemStack)(Object)this;
		if (!item.isEmpty() && (item.hasNbt() && item.getNbt().contains("fabrication:PartialDamage"))) {
			List<Text> lines = cir.getReturnValue();
			for (int i = 0; i < lines.size(); i++) {
				Object t = lines.get(i);
				double part = item.getNbt().getDouble("fabrication:PartialDamage");
				if (t instanceof TranslatableText) {
					if (((TranslatableText) t).getKey().equals("item.durability")) {
						lines.set(i, new TranslatableText("item.durability",
								DimensionalTools.format.format((item.getMaxDamage() - item.getDamage())+(1-part)), item.getMaxDamage()));
					}
				}
			}
		}
	}
}
