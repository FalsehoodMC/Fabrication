package com.unascribed.fabrication.logic;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FakeMixin;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.command.CommandSource;

@FakeMixin(CommandSource.class)
public class CommandSourceInterfaceHack {

	@ModifyReturn(method="forEachMatching(Ljava/lang/Iterable;Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Consumer;)V", target="Ljava/lang/String;equals(Ljava/lang/Object;)Z")
	public static boolean commandSourceSkipNamespaceCheck(boolean old) {
		return FabConf.isEnabled("*.lenient_command_suggestions") || old;
	}

}
