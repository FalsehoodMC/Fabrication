package com.unascribed.fabrication.mixin.b_utility.legacy_command_syntax;

import com.google.common.base.CharMatcher;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.logic.LegacyIDs;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.support.injection.HijackReturn;
import com.unascribed.fabrication.util.ItemStringReaderReaderReader;
import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStringReader.Reader.class)
@EligibleIf(configAvailable="*.legacy_command_syntax")
public class MixinItemStringReaderReader implements ItemStringReaderReaderReader {

	private Integer fabrication$legacyDamage = null;

	@Hijack(target="Lnet/minecraft/registry/RegistryWrapper$Impl;getOptional(Lnet/minecraft/registry/RegistryKey;)Ljava/util/Optional;",
			method="readItem()V")
	public HijackReturn fabrication$legacyDamageGetOrEmpty(RegistryWrapper.Impl<Item> subject, RegistryKey<Item> rid) {
		if (FabConf.isEnabled("*.legacy_command_syntax")) {
			String numId;
			String meta;
			Identifier id = rid.getValue();
			if (id.getNamespace().equals("minecraft")) {
				if (!id.getPath().isEmpty() && CharMatcher.digit().matchesAllOf(id.getPath())) {
					numId = id.getPath();
					meta = "0";
				} else {
					numId = null;
					meta = null;
				}
			} else {
				if (!id.getNamespace().isEmpty() && !id.getPath().isEmpty() && CharMatcher.digit().matchesAllOf(id.getNamespace()) && CharMatcher.digit().matchesAllOf(id.getPath())) {
					numId = id.getNamespace();
					meta = id.getPath();
				} else {
					numId = null;
					meta = null;
				}
			}
			if (numId != null) {
				int numIdI = Integer.parseInt(numId);
				int metaI = Integer.parseInt(meta);
				boolean metaAsDamage = false;
				Item i = LegacyIDs.lookup(numIdI, metaI);
				if (i == null) {
					i = LegacyIDs.lookup(numIdI, 0);
					metaAsDamage = true;
					if (i == null) {
						return HijackReturn.OPTIONAL_EMPTY;
					}
				}
				if (i.getComponents().contains(DataComponentTypes.MAX_DAMAGE) && metaAsDamage) {
					fabrication$legacyDamage = metaI;
				}
				return new HijackReturn(subject.getOptional(RegistryKey.of(RegistryKeys.ITEM, LegacyIDs.lookup_id(numIdI, metaI))));
			}
		}
		return null;
	}

	@Override
	public Integer fabrication$getLegacyDamage() {
		return fabrication$legacyDamage;
	}
}
