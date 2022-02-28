package com.unascribed.fabrication.mixin.b_utility.legacy_command_syntax;

import java.util.Optional;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.logic.LegacyIDs;
import com.unascribed.fabrication.support.EligibleIf;

import com.google.common.base.CharMatcher;

import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;

@Mixin(ItemStringReader.class)
@EligibleIf(configAvailable="*.legacy_command_syntax")
public class MixinItemStringReader {

	@Shadow
	private NbtCompound nbt;

	private NbtCompound fabrication$legacyDamageNbt = null;

	@Redirect(at=@At(value="INVOKE", target="net/minecraft/util/registry/DefaultedRegistry.getOrEmpty(Lnet/minecraft/util/Identifier;)Ljava/util/Optional;"),
			method="readItem()V")
	public Optional<Item> getOrEmpty(DefaultedRegistry<Item> subject, Identifier id) {
		fabrication$legacyDamageNbt = null;
		if (FabConf.isEnabled("*.legacy_command_syntax")) {
			String numId;
			String meta;
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
						return Optional.empty();
					}
				}
				if (i.isDamageable() && metaAsDamage) {
					fabrication$legacyDamageNbt = new NbtCompound();
					fabrication$legacyDamageNbt.putInt("Damage", metaI);
				}
				return Optional.of(i);
			}
		}
		return subject.getOrEmpty(id);
	}

	@Inject(at=@At("RETURN"), method="consume()Lnet/minecraft/command/argument/ItemStringReader;")
	public void consume(CallbackInfoReturnable<ItemStringReader> ci) {
		if (fabrication$legacyDamageNbt != null) {
			if (nbt == null) {
				nbt = fabrication$legacyDamageNbt;
			} else {
				nbt.copyFrom(fabrication$legacyDamageNbt);
			}
		}
	}

}
