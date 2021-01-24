package com.unascribed.fabrication.support.injection;

import java.lang.reflect.Field;

import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.MixinEnvironment.Option;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler.ErrorAction;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.support.MixinErrorHandler;

public class Failsoft {

	// identical to InjectionInfo::postInject, but with usages of InjectionError replaced
	public static boolean postInject(InjectionInfo ii, MixinTargetContext mixin, String description, String extraInfo) {
		if (!mixin.getClassName().startsWith("com.unascribed.fabrication.mixin.")) {
			return true;
		}
		int expectedCallbackCount = pluck(ii, "expectedCallbackCount");
		int targetCount = pluck(ii, "targetCount");
		int injectedCallbackCount = pluck(ii, "injectedCallbackCount");
		int requiredCallbackCount = pluck(ii, "requiredCallbackCount");
		int maxCallbackCount = pluck(ii, "maxCallbackCount");
		
		for (MethodNode method : (Iterable<MethodNode>)pluck(ii, "injectedMethods")) {
			ii.getClassNode().methods.add(method);
		}

//		String description = ii.getDescription();
		String refMapStatus = mixin.getReferenceMapper().getStatus();
//		String extraInfo = ii.getDynamicInfo() + ii.getMessages();
		if ((mixin.getOption(Option.DEBUG_INJECTORS) && ii.getInjectedCallbackCount() < expectedCallbackCount)) {
			handleApplyErrorProactively(mixin.getTargetClassInfo().getClassName(), new InvalidInjectionException(ii,
					String.format("Injection validation failed: %s %s%s in %s expected %d invocation(s) but %d succeeded. Scanned %d target(s). %s%s",
							description, ii.getMethodName(), ii.getMethod().desc, mixin, expectedCallbackCount, ii.getInjectedCallbackCount(),
							targetCount, refMapStatus, extraInfo)), mixin.getMixin(), ErrorAction.ERROR);
		} else if (injectedCallbackCount < requiredCallbackCount) {
			handleApplyErrorProactively(mixin.getTargetClassInfo().getClassName(),  new InvalidInjectionException(ii,
					String.format("Critical injection failure: %s %s%s in %s failed injection check, (%d/%d) succeeded. Scanned %d target(s). %s%s",
							description, ii.getMethodName(), ii.getMethod().desc, mixin, ii.getInjectedCallbackCount(), requiredCallbackCount,
							targetCount, refMapStatus, extraInfo)), mixin.getMixin(), ErrorAction.ERROR);
		} else if (injectedCallbackCount > maxCallbackCount) {
			handleApplyErrorProactively(mixin.getTargetClassInfo().getClassName(),  new InvalidInjectionException(ii,
					String.format("Critical injection failure: %s %s%s in %s failed injection check, %d succeeded of %d allowed.%s",
							description, ii.getMethodName(), ii.getMethod().desc, mixin, ii.getInjectedCallbackCount(), maxCallbackCount, extraInfo)), mixin.getMixin(), ErrorAction.ERROR);
		}
		return false;
	}

	private static <T extends Throwable> void handleApplyErrorProactively(String targetClassName, T th, IMixinInfo mixin, ErrorAction action) throws T {
		action = MixinErrorHandler.INST.onApplyError(targetClassName, th, mixin, action);
		// don't throw the exception *at all* to avoid a stack-unwind causing other mixins to
		// cascade-fail or for frame recomputation to not occur
		if (action == ErrorAction.ERROR) {
			throw th;
		} else if (action == ErrorAction.WARN) {
			FabLog.warn("Mixin application failed", th);
		}
	}

	private static <T> T pluck(InjectionInfo ii, String field) {
		Field f = null;
		Class<?> clazz = ii.getClass();
		while (f == null && clazz != null) {
			try {
				f = clazz.getDeclaredField(field);
			} catch (NoSuchFieldException ignore) {}
			clazz = clazz.getSuperclass();
		}
		if (f == null) throw new NoSuchFieldError(field);
		f.setAccessible(true);
		try {
			return (T)f.get(ii);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}



}
