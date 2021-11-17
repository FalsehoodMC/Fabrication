package com.unascribed.fabrication.support.injection;

import java.lang.reflect.Field;

import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.MixinEnvironment.Option;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler.ErrorAction;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.support.MixinErrorHandler_THIS_ERROR_HANDLER_IS_FOR_SOFT_FAILURE_IN_FABRICATION_ITSELF_AND_DOES_NOT_IMPLY_FABRICATION_IS_RESPONSIBLE_FOR_THE_BELOW_ERROR;

public class Failsoft {

	// prevents pointless issue reports
	public static RuntimeException hideOurselves(RuntimeException t) {
		try {
			StackTraceElement[] stackTrace = t.getStackTrace();
			StackTraceElement[] newStackTrace = t.getStackTrace();
			if (doctorStack(stackTrace, newStackTrace)) {
				return new RuntimeException(t.getMessage(), t.getCause(), true, true) {
					{
						setStackTrace(newStackTrace);
						for (Throwable s : t.getSuppressed()) {
							addSuppressed(s);
						}
					}
					@Override
					public String toString() {
						return t.toString();
					}
				};
			}
		} catch (Throwable ignore) {}
		return t;
	}
	public static Error hideOurselves(Error t) {
		try {
			StackTraceElement[] stackTrace = t.getStackTrace();
			StackTraceElement[] newStackTrace = t.getStackTrace();
			if (doctorStack(stackTrace, newStackTrace)) {
				return new Error(t.getMessage(), t.getCause(), true, true) {
					{
						setStackTrace(newStackTrace);
						for (Throwable s : t.getSuppressed()) {
							addSuppressed(s);
						}
					}
					@Override
					public String toString() {
						return t.toString();
					}
				};
			}
		} catch (Throwable ignore) {}
		return t;
	}

	private static boolean doctorStack(StackTraceElement[] stackTrace, StackTraceElement[] newStackTrace) {
		boolean needClone = false;
		for (int i = 0; i < stackTrace.length; i++) {
			StackTraceElement ste = stackTrace[i];
			if (ste.getClassName().startsWith("com.unascribed.fabrication.support.injection.")) {
				newStackTrace[i] = new StackTraceElement("org.spongepowered.asm.mixin.injection.struct.InjectionInfo", "postInject$failsoft", "gist.github.com/517e2d6d4c6a75303721b7e2e995a9f8", -1);
				needClone = true;
			}
		}
		return needClone;
	}
	// identical to InjectionInfo::postInject, but with usages of InjectionError replaced
	public static boolean postInject(InjectionInfo ii, MixinTargetContext mixin, String description, String extraInfo) {
		if (!mixin.getClassName().startsWith("com.unascribed.fabrication.mixin.")) {
			return true;
		}
		int expectedCallbackCount = pluck(InjectionInfo.class, ii, "expectedCallbackCount");
		int targetCount = pluck(InjectionInfo.class, ii, "targetCount");
		int injectedCallbackCount = pluck(InjectionInfo.class, ii, "injectedCallbackCount");
		int requiredCallbackCount = pluck(InjectionInfo.class, ii, "requiredCallbackCount");
		int maxCallbackCount = pluck(InjectionInfo.class, ii, "maxCallbackCount");

		for (MethodNode method : (Iterable<MethodNode>)pluck(InjectionInfo.class, ii, "injectedMethods")) {
			ii.getClassNode().methods.add(method);
		}

//		String description = ii.getDescription();
		String refMapStatus = mixin.getReferenceMapper().getStatus();
//		String extraInfo = ii.getDynamicInfo() + ii.getMessages();
		if ((mixin.getOption(Option.DEBUG_INJECTORS) && ii.getInjectedCallbackCount() < expectedCallbackCount)) {
			handleApplyErrorProactively(mixin.getTargetClassInfo().getClassName(), new InvalidInjectionException(ii.getContext(),
					String.format("Injection validation failed: %s %s%s in %s expected %d invocation(s) but %d succeeded. Scanned %d target(s). %s%s",
							description, ii.getMethodName(), ii.getMethod().desc, mixin, expectedCallbackCount, ii.getInjectedCallbackCount(),
							targetCount, refMapStatus, extraInfo)), mixin.getMixin(), ErrorAction.ERROR);
		} else if (injectedCallbackCount < requiredCallbackCount) {
			handleApplyErrorProactively(mixin.getTargetClassInfo().getClassName(),  new InvalidInjectionException(ii.getContext(),
					String.format("Critical injection failure: %s %s%s in %s failed injection check, (%d/%d) succeeded. Scanned %d target(s). %s%s",
							description, ii.getMethodName(), ii.getMethod().desc, mixin, ii.getInjectedCallbackCount(), requiredCallbackCount,
							targetCount, refMapStatus, extraInfo)), mixin.getMixin(), ErrorAction.ERROR);
		} else if (injectedCallbackCount > maxCallbackCount) {
			handleApplyErrorProactively(mixin.getTargetClassInfo().getClassName(),  new InvalidInjectionException(ii.getContext(),
					String.format("Critical injection failure: %s %s%s in %s failed injection check, %d succeeded of %d allowed.%s",
							description, ii.getMethodName(), ii.getMethod().desc, mixin, ii.getInjectedCallbackCount(), maxCallbackCount, extraInfo)), mixin.getMixin(), ErrorAction.ERROR);
		}
		return false;
	}

	private static <T extends Throwable> void handleApplyErrorProactively(String targetClassName, T th, IMixinInfo mixin, ErrorAction action) throws T {
		action = MixinErrorHandler_THIS_ERROR_HANDLER_IS_FOR_SOFT_FAILURE_IN_FABRICATION_ITSELF_AND_DOES_NOT_IMPLY_FABRICATION_IS_RESPONSIBLE_FOR_THE_BELOW_ERROR.INST.onApplyError(targetClassName, th, mixin, action);
		// don't throw the exception *at all* to avoid a stack-unwind causing other mixins to
		// cascade-fail or for frame recomputation to not occur
		if (action == ErrorAction.ERROR) {
			throw th;
		} else if (action == ErrorAction.WARN) {
			FabLog.warn("Mixin application failed", th);
		}
	}

	private static <T, C> T pluck(Class<? super C> clazz, C c, String field) {
		Field f = null;
		Class<?> cursor = clazz;
		while (f == null && cursor != null) {
			try {
				f = cursor.getDeclaredField(field);
			} catch (NoSuchFieldException ignore) {}
			cursor = cursor.getSuperclass();
		}
		if (f == null) throw new NoSuchFieldError(field);
		f.setAccessible(true);
		try {
			return (T)f.get(c);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}



}
