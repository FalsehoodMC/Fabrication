package com.unascribed.fabrication.mixin.b_utility.lenient_command_suggestions;

import com.unascribed.fabrication.logic.CommandSourceInterfaceHack;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FakeMixinHack;
import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CommandSource.class)
@EligibleIf(configAvailable="*.lenient_command_suggestions")
@FakeMixinHack(CommandSourceInterfaceHack.class)
public interface MixinCommandSource {
}
