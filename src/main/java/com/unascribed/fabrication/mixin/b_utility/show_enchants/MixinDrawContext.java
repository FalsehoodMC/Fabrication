package com.unascribed.fabrication.mixin.b_utility.show_enchants;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.EnchantmentHelperHelper;

import com.google.common.collect.Lists;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;

@Mixin(DrawContext.class)
@EligibleIf(anyConfigAvailable={"*.books_show_enchants", "*.tools_show_important_enchant"}, envMatches=Env.CLIENT)
public abstract class MixinDrawContext {

	//remove color, spaces and unicode private use area characters #682
	private static Pattern fabrication$enchantFilterPattern = Pattern.compile("(§[0-9A-FK-ORa-fk-or])|([\ue000-\uf8ff ])");

	@Shadow
	@Final
	private MatrixStack matrices;

	@Shadow
	public abstract int drawText(TextRenderer textRenderer, @Nullable String text, int x, int y, int color, boolean shadow);

	@FabInject(at=@At("TAIL"), method="drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V")
	public void renderGuiItemOverlay(TextRenderer renderer, ItemStack stack, int x, int y, String countLabel, CallbackInfo ci) {
		if (stack == null) return;
		if (FabConf.isEnabled("*.books_show_enchants") && stack.getItem() == Items.ENCHANTED_BOOK) {
			ItemEnchantmentsComponent enchants = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
			if (enchants == null || enchants.isEmpty()) return;
			List<RegistryEntry<Enchantment>> valid = Lists.newArrayList(enchants.getEnchantments());
			int j = (int)((System.currentTimeMillis()/1000)%valid.size());
			RegistryEntry<Enchantment> display = valid.get(j);
			String translated = fabrication$enchantFilterPattern.matcher(display.value().description().getString()).replaceAll("");
			if (display.isIn(EnchantmentTags.CURSE)) {
				String curseOfBinding = fabrication$enchantFilterPattern.matcher(I18n.translate("enchantment.minecraft.binding_curse")).replaceAll("");
				String curseOfVanishing = fabrication$enchantFilterPattern.matcher(I18n.translate("enchantment.minecraft.vanishing_curse")).replaceAll("");
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
			this.drawText(renderer, firstCodepoint, x, y+6+3, display.isIn(EnchantmentTags.CURSE) ? 0xFFFF5555 : display.isIn(EnchantmentTags.TREASURE) ? 0xFF55FFFF : 0xFFFFFFFF, true);
			matrices.pop();
		}
		if (FabConf.isEnabled("*.tools_show_important_enchant")) {
			RegistryEntry<Enchantment> display = null;
			DynamicRegistryManager registries = MinecraftClient.getInstance().world.getRegistryManager();
			if (EnchantmentHelperHelper.getLevel(registries, Enchantments.SILK_TOUCH, stack) > 0) {
				display = EnchantmentHelperHelper.getEntry(registries, Enchantments.SILK_TOUCH).orElseThrow();
			} else if (EnchantmentHelperHelper.getLevel(registries, Enchantments.FORTUNE, stack) > 0) {
				display = EnchantmentHelperHelper.getEntry(registries, Enchantments.FORTUNE).orElseThrow();
			} else if (EnchantmentHelperHelper.getLevel(registries, Enchantments.RIPTIDE, stack) > 0) {
				display = EnchantmentHelperHelper.getEntry(registries, Enchantments.RIPTIDE).orElseThrow();
			}
			if (display != null) {
				String translated = fabrication$enchantFilterPattern.matcher(display.value().description().getString()).replaceAll("");
				String firstCodepoint = new String(Character.toChars(translated.codePoints().findFirst().getAsInt()));
				matrices.push();
				matrices.translate(0, 0, 200);
				this.drawText(renderer, firstCodepoint, x, y, 0xFFFF55FF, true);
				matrices.pop();
			}
		}
	}

}
