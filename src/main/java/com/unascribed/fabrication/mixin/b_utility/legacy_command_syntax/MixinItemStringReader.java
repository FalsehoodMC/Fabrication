package com.unascribed.fabrication.mixin.b_utility.legacy_command_syntax;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.util.ItemStringReaderReaderReader;
import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStringReader.class)
@EligibleIf(configAvailable="*.legacy_command_syntax")
public class MixinItemStringReader {

	private Integer fabrication$legacyDamage = null;

	@Hijack(method="consume(Lcom/mojang/brigadier/StringReader;Lnet/minecraft/command/argument/ItemStringReader$Callbacks;)V", target="Lnet/minecraft/command/argument/ItemStringReader$Reader;read()V")
	public boolean consume(ItemStringReader.Reader subject) {
		try {
			subject.read();
			fabrication$legacyDamage = ((ItemStringReaderReaderReader) subject).fabrication$getLegacyDamage();
		} catch (CommandSyntaxException ignore) {}
		return true;
	}

	// TODO: @ModifyReturnValue would work better
	@FabInject(at=@At("RETURN"), method="consume(Lcom/mojang/brigadier/StringReader;)Lnet/minecraft/command/argument/ItemStringReader$ItemResult;")
	public void consume(StringReader reader, CallbackInfoReturnable<ItemStringReader.ItemResult> cir) {
		if (fabrication$legacyDamage != null) {
			ItemStringReader.ItemResult result = cir.getReturnValue();
			ComponentChanges.AddedRemovedPair addedRemovedPair = result.components().toAddedRemovedPair();
			ComponentChanges.Builder builder = ComponentChanges.builder();
			for (Component added : addedRemovedPair.added()) {
				if (!DataComponentTypes.DAMAGE.equals(added.type())) {
					builder.add(added.type(), added.value()); // TODO: is this bad?
				}
			}
			for (ComponentType<?> removed : addedRemovedPair.removed()) {
				if (!DataComponentTypes.DAMAGE.equals(removed)) {
					builder.remove(removed);
				}
			}
			builder.add(DataComponentTypes.DAMAGE, fabrication$legacyDamage);
			cir.setReturnValue(new ItemStringReader.ItemResult(result.item(), builder.build()));
		}
	}

}
