package com.unascribed.fabrication.mixin.b_utility.legacy_command_syntax;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.support.injection.HijackReturn;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
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
public class MixinEntitySummonArgumentType {
	private static final ThreadLocal<Boolean> fabrication$isNotEntityArgument = ThreadLocal.withInitial(() -> true);

	@FabInject(at=@At("TAIL"), method="<init>(Lnet/minecraft/command/CommandRegistryAccess;Lnet/minecraft/registry/RegistryKey;Lcom/mojang/serialization/Codec;)V")
	public void legacyCommandInput(CommandRegistryAccess access, RegistryKey key, Codec codec, CallbackInfo ci) {
		if (key == RegistryKeys.ENTITY_TYPE) {
			fabrication$isNotEntityArgument.set(false);
		}
	}

	@Hijack(method="parseAsNbt(Lcom/mojang/brigadier/StringReader;)Lnet/minecraft/nbt/NbtElement;",
			target="Lnet/minecraft/util/Identifier;fromCommandInput(Lcom/mojang/brigadier/StringReader;)Lnet/minecraft/util/Identifier;")
	private static HijackReturn legacyCommandInput(StringReader sr) {
		if (fabrication$isNotEntityArgument.get()) return null;
		if (!FabConf.isEnabled("*.legacy_command_syntax")) return null;
		char peek = sr.peek();
		if (peek >= 'A' && peek <= 'Z') {
			int start = sr.getCursor();
			while (sr.canRead() && fabrication$isCharValid(sr.peek())) {
				sr.skip();
			}
			if (!sr.canRead()) {
				return new HijackReturn(
						Identifier.of("minecraft", sr.getString().substring(start, sr.getCursor())
						.replaceAll("([a-z])([A-Z])", "$1_$2")
						.toLowerCase(Locale.ROOT)));
			}
		}
		return null;
	}

	@Unique
	private static boolean fabrication$isCharValid(char c) {
		return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
	}

}
