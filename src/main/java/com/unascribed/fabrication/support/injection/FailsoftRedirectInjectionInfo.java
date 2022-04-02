package com.unascribed.fabrication.support.injection;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo.AnnotationType;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo.HandlerPrefix;
import org.spongepowered.asm.mixin.injection.struct.RedirectInjectionInfo;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

import java.util.HashMap;
import java.util.Map;

@AnnotationType(Redirect.class)
@HandlerPrefix("redirect")
public class FailsoftRedirectInjectionInfo extends RedirectInjectionInfo {

	public static final Map<String, String> fabrication$allExistingRedirects = new HashMap<>();

	public FailsoftRedirectInjectionInfo(MixinTargetContext mixin, MethodNode method, AnnotationNode annotation) {
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
	public void prepare() {
		int at = annotation.values.indexOf("at");
		if (at != -1 && at < annotation.values.size()){
			Object atNode = annotation.values.get(at+1);
			if (atNode instanceof AnnotationNode) {
				AnnotationNode an = (AnnotationNode)atNode;
				int ani = an.values.indexOf("target");
				if (ani != -1 && ani < an.values.size()){
					Object target = an.values.get(ani+1);
					if (target instanceof String) fabrication$allExistingRedirects.put(this.method.name, (String) target);
				}
			}
		}
		super.prepare();
	}

}
