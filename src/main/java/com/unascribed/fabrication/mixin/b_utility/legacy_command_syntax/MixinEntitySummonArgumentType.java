package com.unascribed.fabrication.mixin.b_utility.legacy_command_syntax;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.IsEntityArg;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.support.injection.HijackReturn;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(RegistryEntryArgumentType.class)
@EligibleIf(configAvailable="*.legacy_command_syntax")
public class MixinEntitySummonArgumentType implements IsEntityArg {
	@Unique
	private boolean fabrication$isNotEntityArgument = true;

	@FabInject(at=@At("TAIL"), method="<init>(Lnet/minecraft/command/CommandRegistryAccess;Lnet/minecraft/registry/RegistryKey;Lcom/mojang/serialization/Codec;)V")
	public void legacyCommandInput(CommandRegistryAccess access, RegistryKey key, Codec codec, CallbackInfo ci) {
		if (key == RegistryKeys.ENTITY_TYPE) {
			fabrication$isNotEntityArgument = false;
		}
	}

	@Hijack(method="parse(Lcom/mojang/brigadier/StringReader;)Lnet/minecraft/registry/entry/RegistryEntry;",
			target="Lnet/minecraft/command/argument/RegistryEntryArgumentType;parseAsNbt(Lcom/mojang/brigadier/StringReader;)Lnet/minecraft/nbt/NbtElement;")
	private HijackReturn fabriaciotn$legacyCommandInputInject(StringReader sr) {
		if (((IsEntityArg)this).fabriaciot$esat$isNotEntityArg()) return null;
		if (!FabConf.isEnabled("*.legacy_command_syntax")) return null;
		int i = sr.getCursor();
		try {
			NbtElement nbtElement = (new StringNbtReader(sr)).parseElement();
			if (!sr.canRead() || sr.peek() == ' ') {
				return null;
			} else {
				sr.setCursor(i);
				Identifier identifier = fabriaciotn$legacyCommandInput(sr);
				if (identifier != null && (!sr.canRead() || sr.peek() == ' ')) {
					return new HijackReturn(NbtString.of(identifier.toString()));
				} else {
					sr.setCursor(i);
				}
			}
		} catch (Exception e) {
			sr.setCursor(i);
		}
		return null;
	}
	@Unique
	private static Identifier fabriaciotn$legacyCommandInput(StringReader sr) {
		char peek = sr.peek();
		if (peek >= 'A' && peek <= 'Z') {
			int start = sr.getCursor();
			while (sr.canRead() && fabrication$isCharValid(sr.peek())) {
				sr.skip();
			}
			if (!sr.canRead()) {
				return
						Identifier.of("minecraft", sr.getString().substring(start, sr.getCursor())
						.replaceAll("([a-z])([A-Z])", "$1_$2")
						.toLowerCase(Locale.ROOT));
			}
		}
		return null;
	}

	@Unique
	private static boolean fabrication$isCharValid(char c) {
		return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
	}

	@Override
	public boolean fabriaciot$esat$isNotEntityArg() {
		return fabrication$isNotEntityArgument;
	}
}
