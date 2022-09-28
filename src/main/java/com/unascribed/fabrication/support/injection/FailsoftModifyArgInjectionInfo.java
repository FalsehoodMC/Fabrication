package com.unascribed.fabrication.support.injection;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo.AnnotationType;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo.HandlerPrefix;
import org.spongepowered.asm.mixin.injection.struct.ModifyArgInjectionInfo;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

@AnnotationType(FabModifyArg.class)
@HandlerPrefix("modify")
public class FailsoftModifyArgInjectionInfo extends ModifyArgInjectionInfo {

	public FailsoftModifyArgInjectionInfo(MixinTargetContext mixin, MethodNode method, AnnotationNode annotation) {
		super(mixin, method, annotation);
	}

	@Override
	public void postInject() {
		try {
			super.postInject();
		} catch (Throwable e) {
			FabMixinInjector.handleErrorProactively(mixin.getTargetClassInfo().getClassName(), e, mixin.getMixin(), IMixinErrorHandler.ErrorAction.ERROR);
		}
	}
	@Override
	public void readAnnotation() {
		if (this.annotation != null) {
			FabMixinInjector.remap(mixin.getMixin().getClassName(), this.annotation);
		}
		try {
			super.readAnnotation();
		} catch (Throwable e) {
			FabMixinInjector.handleErrorProactively(mixin.getTargetClassInfo().getClassName(), e, mixin.getMixin(), IMixinErrorHandler.ErrorAction.ERROR);
		}
	}

	@Override
	public Injector parseInjector(AnnotationNode injectAnnotation) {
		return FabMixinInjector.doctorAnnotation("@FabModifyArg", super.parseInjector(injectAnnotation));
	}
}
