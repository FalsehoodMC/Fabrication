package com.unascribed.fabrication.mixin.b_utility.legacy_command_syntax;

import com.google.common.base.CharMatcher;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.logic.LegacyIDs;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.support.injection.HijackReturn;
import com.unascribed.fabrication.util.forgery_nonsense.ForgeryNbt;
import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStringReader.class)
@EligibleIf(configAvailable="*.legacy_command_syntax")
public class MixinItemStringReader {

	@Shadow
	private NbtCompound nbt;

	private NbtCompound fabrication$legacyDamageNbt = null;

	@Hijack(target="Lnet/minecraft/registry/RegistryWrapper;getOptional(Lnet/minecraft/registry/RegistryKey;)Ljava/util/Optional;",
			method="readItem()V")
	public HijackReturn fabrication$legacyDamageGetOrEmpty(RegistryWrapper<Item> subject, RegistryKey<Item> rid) {
		fabrication$legacyDamageNbt = null;
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
				if (i.isDamageable() && metaAsDamage) {
					fabrication$legacyDamageNbt = ForgeryNbt.getCompound();
					fabrication$legacyDamageNbt.putInt("Damage", metaI);
				}
				return new HijackReturn(subject.getOptional(RegistryKey.of(RegistryKeys.ITEM, LegacyIDs.lookup_id(numIdI, metaI))));
			}
		}
		return null;
	}

	@FabInject(at=@At("RETURN"), method="consume()V")
	public void consume(CallbackInfo ci) {
		if (fabrication$legacyDamageNbt != null) {
			if (nbt == null) {
				nbt = fabrication$legacyDamageNbt;
			} else {
				nbt.copyFrom(fabrication$legacyDamageNbt);
			}
		}
	}

}
