package com.unascribed.fabrication.support.injection;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo.AnnotationType;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo.HandlerPrefix;
import org.spongepowered.asm.mixin.injection.struct.ModifyConstantInjectionInfo;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

@AnnotationType(FabModifyConst.class)
@HandlerPrefix("constant")
public class FabModifyConstInjectionInfo extends ModifyConstantInjectionInfo {

	public FabModifyConstInjectionInfo(MixinTargetContext mixin, MethodNode method, AnnotationNode annotation) {
		super(mixin, method, annotation);
	}

	@Override
	public void postInject() {
		if (Failsoft.postInject(this, mixin, getDescription(), getDynamicInfo() + getMessages())) {
			try {
				super.postInject();
			} catch (Error e) {
				throw Failsoft.hideOurselves(e);
			} catch (RuntimeException e) {
				throw Failsoft.hideOurselves(e);
			}
		}
	}
	@Override
	public void readAnnotation() {
		if (this.annotation != null) {
			FabMixinInjector.remap(mixin.getMixin().getClassName(), this.annotation);
		}
		super.readAnnotation();
	}

	@Override
	public Injector parseInjector(AnnotationNode injectAnnotation) {
		return FabMixinInjector.doctorAnnotation("@FabModifyConstant", new FabModifyConstInjection(this));
	}

}
