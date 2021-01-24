package com.unascribed.fabrication.support.injection;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.struct.RedirectInjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo.AnnotationType;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo.HandlerPrefix;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

@AnnotationType(Redirect.class)
@HandlerPrefix("redirect")
public class FailsoftRedirectInjectionInfo extends RedirectInjectionInfo {

	public FailsoftRedirectInjectionInfo(MixinTargetContext mixin, MethodNode method, AnnotationNode annotation) {
		super(mixin, method, annotation);
	}
	
	@Override
	public void postInject() {
		if (Failsoft.postInject(this, mixin, getDescription(), getDynamicInfo() + getMessages())) {
			super.postInject();
		}
	}

}
