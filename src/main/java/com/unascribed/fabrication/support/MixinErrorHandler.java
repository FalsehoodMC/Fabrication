package com.unascribed.fabrication.support;

import java.util.Set;

import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import com.unascribed.fabrication.Analytics;
import com.unascribed.fabrication.FabLog;

import com.google.common.base.Joiner;

public class MixinErrorHandler implements IMixinErrorHandler {

	public static final MixinErrorHandler INST = new MixinErrorHandler();
	
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
			Analytics.submit("mixin_failure",
					"targets", Joiner.on(",").join(mixin.getTargetClasses()),
					"message", th.getMessage(),
					"mixin", mixin.getClassName());
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
		}
		return action;
	}

}
