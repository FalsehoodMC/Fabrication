package com.unascribed.fabrication.support.injection;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo.AnnotationType;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo.HandlerPrefix;
import org.spongepowered.asm.mixin.injection.struct.ModifyConstantInjectionInfo;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

import java.util.List;

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
			String mixinOrigin = mixin.getMixin().getClassName();
			int mthd_i = annotation.values.indexOf("method");
			if (mthd_i != -1) {
				Object list = annotation.values.get(mthd_i + 1);
				if (list instanceof List<?>) {
					mthd_i = ((List<?>) list).size();
					for (int i = 0; i < mthd_i; i++) {
						Object val = ((List<?>) list).get(i);
						if (val instanceof String) {
							((List) list).set(i, FabRefMap.methodMap(mixinOrigin, (String) val));
						}
					}
				}
			}
		}
		super.readAnnotation();
	}

	@Override
	public Injector parseInjector(AnnotationNode injectAnnotation) {
		return new FabMixinInjector(this, "@FabModifyConstant", new FabModifyConstInjection(this));
	}

}
