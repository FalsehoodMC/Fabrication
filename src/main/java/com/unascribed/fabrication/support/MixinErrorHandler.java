package com.unascribed.fabrication.support;

import java.util.Set;

import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException;

import com.unascribed.fabrication.FabLog;

import com.google.common.base.Joiner;

public class MixinErrorHandler implements IMixinErrorHandler {

	@Override
	public ErrorAction onPrepareError(IMixinConfig config, Throwable th, IMixinInfo mixin, ErrorAction action) {
		return onError(th, mixin, action, "prepare");
	}

	@Override
	public ErrorAction onApplyError(String targetClassName, Throwable th, IMixinInfo mixin, ErrorAction action) {
		return onError(th, mixin, action, "apply");
	}
		
	public ErrorAction onError(Throwable th, IMixinInfo mixin, ErrorAction action, String verb) {
		if (mixin.getClassName().startsWith("com.unascribed.fabrication.")) {
			if (action == ErrorAction.ERROR) {
				Set<String> keys = MixinConfigPlugin.getConfigKeysForDiscoveredClass(mixin.getClassName());
				if (!keys.isEmpty()) {
					FabLog.warn("Mixin "+mixin.getClassName()+" failed to "+verb+"! Force-disabling "+Joiner.on(", ").join(keys));
					for (String opt : keys) {
						MixinConfigPlugin.addFailure(opt);
					}
					return ErrorAction.NONE;
				}
			}
		} else if (th instanceof InvalidInjectionException && th.getMessage().contains("expected") && !MixinConfigPlugin.ORIGINAL_DEBUG_INJECTORS) {
			// this may be our fault due to forcing DEBUG_INJECTORS on, so demote it
			return ErrorAction.WARN;
		}
		return action;
	}

}
