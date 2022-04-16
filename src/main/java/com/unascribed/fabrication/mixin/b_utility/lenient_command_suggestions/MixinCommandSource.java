package com.unascribed.fabrication.mixin.b_utility.lenient_command_suggestions;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CommandSource.class)
@EligibleIf(configAvailable="*.lenient_command_suggestions")
public interface MixinCommandSource {

	@ModifyReturn(method="forEachMatching(Ljava/lang/Iterable;Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Consumer;)V", target="Ljava/lang/String;equals(Ljava/lang/Object;)Z")
	private static boolean fabrication$skipNamespaceCheck(boolean old) {
		return FabConf.isEnabled("*.lenient_command_suggestions") || old;
	}

}
