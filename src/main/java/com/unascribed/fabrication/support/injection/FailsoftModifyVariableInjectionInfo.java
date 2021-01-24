package com.unascribed.fabrication.support.injection;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.struct.ModifyVariableInjectionInfo;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo.AnnotationType;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo.HandlerPrefix;

@AnnotationType(ModifyVariable.class)
@HandlerPrefix("localvar")
public class FailsoftModifyVariableInjectionInfo extends ModifyVariableInjectionInfo {

	public FailsoftModifyVariableInjectionInfo(MixinTargetContext mixin, MethodNode method, AnnotationNode annotation) {
		super(mixin, method, annotation);
	}
	
	@Override
	public void postInject() {
		if (Failsoft.postInject(this, mixin, getDescription(), getDynamicInfo() + getMessages())) {
			super.postInject();
		}
	}

}
