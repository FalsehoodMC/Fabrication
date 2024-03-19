package com.unascribed.fabrication.mixin.b_utility.show_enchants;

import java.util.List;
import java.util.regex.Pattern;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.registry.Registries;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import com.google.common.collect.Lists;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

@Mixin(DrawContext.class)
@EligibleIf(anyConfigAvailable={"*.books_show_enchants", "*.tools_show_important_enchant"}, envMatches=Env.CLIENT)
public abstract class MixinDrawContext {

	//remove color, spaces and unicode private use area characters #682
	private static Pattern fabrication$enchantFilterPattern = Pattern.compile("(\u00A7[0-9A-FK-ORa-fk-or])|([\ue000-\uf8ff ])");

	@Shadow
	@Final
	private MatrixStack matrices;

	@Shadow
	public abstract int drawText(TextRenderer textRenderer, @Nullable String text, int x, int y, int color, boolean shadow);

	@FabInject(at=@At("TAIL"), method="drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V")
	public void renderGuiItemOverlay(TextRenderer renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci) {
		if (stack == null) return;
		if (stack.getItem() == Items.ENCHANTED_BOOK && FabConf.isEnabled("*.books_show_enchants")) {
			NbtList tag = EnchantedBookItem.getEnchantmentNbt(stack);
			List<Enchantment> valid = Lists.newArrayList();
			for (int i = 0; i < tag.size(); i++) {
				NbtCompound ct = tag.getCompound(i);
				Identifier id = Identifier.tryParse(ct.getString("id"));
				if (id != null) {
					Enchantment e = Registries.ENCHANTMENT.get(id);
					if (e != null) {
						valid.add(e);
					}
				}
			}
			if (valid.isEmpty()) return;
			int j = (int)((System.currentTimeMillis()/1000)%valid.size());
			Enchantment display = valid.get(j);
			String translated = fabrication$enchantFilterPattern.matcher(I18n.translate(display.getTranslationKey())).replaceAll("");
			if (display.isCursed()) {
				String curseOfBinding = fabrication$enchantFilterPattern.matcher(I18n.translate(Enchantments.BINDING_CURSE.getTranslationKey())).replaceAll("");
				String curseOfVanishing = fabrication$enchantFilterPattern.matcher(I18n.translate(Enchantments.VANISHING_CURSE.getTranslationKey())).replaceAll("");
				//				boolean suffix = false;
				String curseOf = StringUtils.getCommonPrefix(curseOfBinding, curseOfVanishing);
				//				if (curseOf.isEmpty()) {
				//					// try suffix instead
				//					curseOf = reverse(StringUtils.getCommonPrefix(reverse(curseOfBinding), reverse(curseOfVanishing)));
				//					suffix = true;
				//				}
				if (!curseOf.isEmpty()) {
					//					if (suffix) {
					//						if (translated.endsWith(curseOf)) {
					//							translated = translated.substring(0, translated.length()-curseOf.length());
					//						}
					//					} else {
					if (translated.startsWith(curseOf)) {
						translated = translated.substring(curseOf.length());
					}
					//					}
				}
			}
			String firstCodepoint = new String(Character.toChars(translated.codePoints().findFirst().getAsInt()));
			matrices.push();
			matrices.translate(0, 0, 200);
			this.drawText(renderer, firstCodepoint, x, y+6+3, display.isCursed() ? 0xFFFF5555 : display.isTreasure() ? 0xFF55FFFF : 0xFFFFFFFF, true);
			matrices.pop();
		}
		if (FabConf.isEnabled("*.tools_show_important_enchant")) {
			Enchantment display = null;
			if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, stack) > 0) {
				display = Enchantments.SILK_TOUCH;
			} else if (EnchantmentHelper.getLevel(Enchantments.FORTUNE, stack) > 0) {
				display = Enchantments.FORTUNE;
			} else if (EnchantmentHelper.getLevel(Enchantments.RIPTIDE, stack) > 0) {
				display = Enchantments.RIPTIDE;
			}
			if (display != null) {
				String translated = fabrication$enchantFilterPattern.matcher(I18n.translate(display.getTranslationKey())).replaceAll("");
				String firstCodepoint = new String(Character.toChars(translated.codePoints().findFirst().getAsInt()));
				matrices.push();
				matrices.translate(0, 0, 200);
				this.drawText(renderer, firstCodepoint, x, y, 0xFFFF55FF, true);
				matrices.pop();
			}
		}
	}

}