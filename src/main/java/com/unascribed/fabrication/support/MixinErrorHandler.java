package com.unascribed.fabrication.support;

import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import com.unascribed.fabrication.FabLog;

import com.google.common.base.Joiner;

public class MixinErrorHandler implements IMixinErrorHandler {

	@Override
	public ErrorAction onPrepareError(IMixinConfig config, Throwable th, IMixinInfo mixin, ErrorAction action) {
		if (mixin.getClassName().startsWith("com.unascribed.fabrication.")) {
			if (action == ErrorAction.ERROR) {
				try {
					Class<?> clazz = Class.forName(mixin.getClassName());
					EligibleIf eligible = clazz.getAnnotation(EligibleIf.class);
					if (eligible != null) {
						if (!eligible.configEnabled().isEmpty()) {
							String opt = eligible.configEnabled();
							FabLog.warn("Mixin "+mixin.getClassName()+" failed to prepare! Force-disabling "+opt);
							MixinConfigPlugin.addFailure(opt);
							return ErrorAction.NONE;
						} else if (eligible.anyConfigEnabled().length > 0) {
							FabLog.warn("Mixin "+mixin.getClassName()+" failed to prepare! Force-disabling "+Joiner.on(", ").join(eligible.anyConfigEnabled()));
							for (String opt : eligible.anyConfigEnabled()) {
								MixinConfigPlugin.addFailure(opt);
							}
							return ErrorAction.NONE;
						}
					}
				} catch (ClassNotFoundException e) {
				}
			}
		}
		return action;
	}

	@Override
	public ErrorAction onApplyError(String targetClassName, Throwable th, IMixinInfo mixin, ErrorAction action) {
		if (mixin.getClassName().startsWith("com.unascribed.fabrication.")) {
			if (action == ErrorAction.ERROR) {
				try {
					Class<?> clazz = Class.forName(mixin.getClassName());
					EligibleIf eligible = clazz.getAnnotation(EligibleIf.class);
					if (eligible != null) {
						if (!eligible.configEnabled().isEmpty()) {
							String opt = eligible.configEnabled();
							FabLog.warn("Mixin "+mixin.getClassName()+" failed to apply! Force-disabling "+opt);
							MixinConfigPlugin.addFailure(opt);
							return ErrorAction.NONE;
						} else if (eligible.anyConfigEnabled().length > 0) {
							FabLog.warn("Mixin "+mixin.getClassName()+" failed to apply! Force-disabling "+Joiner.on(", ").join(eligible.anyConfigEnabled()));
							for (String opt : eligible.anyConfigEnabled()) {
								MixinConfigPlugin.addFailure(opt);
							}
							return ErrorAction.NONE;
						}
					}
				} catch (ClassNotFoundException e) {
				}
			}
		}
		return action;
	}

}
