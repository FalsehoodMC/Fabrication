package com.unascribed.fabrication.support.injection;

import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.support.MixinErrorHandler;
import org.objectweb.asm.tree.AnnotationNode;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.code.Injector;

import java.lang.reflect.Field;
import java.util.List;

public class FabMixinInjector {

	public static void remap(String mixinOrigin, AnnotationNode annotation){
		int ann_i = annotation.values.indexOf("method");
		if (ann_i != -1) {
			Object list = annotation.values.get(ann_i + 1);
			if (list instanceof List<?>) {
				ann_i = ((List<?>) list).size();
				for (int i = 0; i<ann_i; i++) {
					Object val = ((List<?>) list).get(i);
					if (val instanceof String) {
						((List) list).set(i, FabRefMap.methodMap(mixinOrigin, (String) val));
					}
				}
			}
		}
		ann_i = annotation.values.indexOf("at");
		if (ann_i != -1) {
			Object maybeList = annotation.values.get(ann_i + 1);
			if (maybeList instanceof List<?>) {
				ann_i = ((List<?>) maybeList).size();
				for (int i = 0; i<ann_i; i++) {
					Object val = ((List<?>) maybeList).get(i);
					if (val instanceof AnnotationNode) {
						remapAt(mixinOrigin, (AnnotationNode)val);
					}
				}
			} else if (maybeList instanceof AnnotationNode) {
				remapAt(mixinOrigin, (AnnotationNode)maybeList);
			}
		}
	}
	private static void remapAt(String mixinOrigin, AnnotationNode annotation) {
		int ann_i = annotation.values.indexOf("target");
		if (ann_i++ != -1) {
			Object val = annotation.values.get(ann_i);
			if (val instanceof String) {
				annotation.values.set(ann_i, FabRefMap.targetMap(mixinOrigin, (String) val));
			}
		}
	}

	public static Injector doctorAnnotation(String annotationType, Injector injector) {
		try {
			Field at = Injector.class.getDeclaredField("annotationType");
			at.setAccessible(true);
			at.set(injector, annotationType);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		return injector;
	}

	public static void handleErrorProactively(String targetClassName, Throwable th, IMixinInfo mixin, IMixinErrorHandler.ErrorAction action) {
		action = MixinErrorHandler.onError(th, mixin, action, "postApply");
		// don't throw the exception *at all* to avoid a stack-unwind causing other mixins to
		// cascade-fail or for frame recomputation to not occur
		if (action == IMixinErrorHandler.ErrorAction.ERROR) {
			throw new RuntimeException(th);
		} else if (action == IMixinErrorHandler.ErrorAction.WARN) {
			FabLog.warn("Mixin application failed", th);
		}
	}
}
