package com.unascribed.fabricationap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes({
		"com.unascribed.fabrication.support.injection.ModifyReturn",
		"com.unascribed.fabrication.support.injection.Hijack",
		"com.unascribed.fabrication.support.injection.FabModifyConst",
		"com.unascribed.fabrication.support.injection.FabInject",
		"com.unascribed.fabrication.support.injection.FabModifyArg",
		"com.unascribed.fabrication.support.injection.FabModifyArgs",
		"com.unascribed.fabrication.support.injection.FabModifyVariable",
		"com.unascribed.fabrication.support.injection.ModifyGetField"
})
public class AnnotationProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		StringBuilder bldr = new StringBuilder();
		for (TypeElement te: annotations) {
			for (Element e : roundEnv.getElementsAnnotatedWith(te)) {
				Element source = e.getEnclosingElement();
				bldr.append(source);
				bldr.append('\n');
				Set<String> mixin = new HashSet<>();
				Set<String> methods = new HashSet<>();
				Set<String> targets = new HashSet<>();
				for (AnnotationMirror am : source.getAnnotationMirrors()) {
					if (!(
							"org.spongepowered.asm.mixin.Mixin".equals(am.getAnnotationType().toString())
							|| "com.unascribed.fabrication.support.injection.FakeMixin".equals(am.getAnnotationType().toString())
					)) continue;
					for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> ae : am.getElementValues().entrySet()){
						Name key = ae.getKey().getSimpleName();
						Object l = ae.getValue().getValue();
						boolean isTargets = key.contentEquals("targets");
						if (!((key.contentEquals("value") || isTargets) && l instanceof List<?>)) continue;
						for (Object o : (List<?>)l) {
							String co = o.toString();
							String ad = isTargets || o instanceof String ? co : co.substring(0, co.length()-6);
							if (ad.charAt(0) == '"') {
								ad = ad.substring(1, ad.charAt(ad.length()-1) == '"' ? ad.length()-1 : ad.length());
							}
							if (ad.startsWith("com.mrcrayfish") || ad.startsWith("svenhjol")) continue;
							mixin.add(ad);
						}
					}
				}
				for (AnnotationMirror am : e.getAnnotationMirrors()) {
					if (!getSupportedAnnotationTypes().contains(am.getAnnotationType().toString())) continue;
					for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> ae : am.getElementValues().entrySet()) {
						String key = ae.getKey().getSimpleName().toString();
						Object l = ae.getValue().getValue();
						if (key.equals("at")) {
							List<String> atTargets = new ArrayList<>();
							if (l instanceof List<?>) {
								for (Object o : (List<?>) l) {
									if (!(o instanceof AnnotationMirror)) continue;
									for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> aei : ((AnnotationMirror) o).getElementValues().entrySet()) {
										if (aei.getKey().getSimpleName().contentEquals("target")) {
											atTargets.add(aei.getValue().getValue().toString());
										}
									}
								}
							} else if (l instanceof AnnotationMirror) {
								for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> aei : ((AnnotationMirror) l).getElementValues().entrySet()) {
									if (aei.getKey().getSimpleName().contentEquals("target")) {
										atTargets.add(aei.getValue().getValue().toString());
									}
								}
							}
							if (atTargets.isEmpty()) continue;
							key = "target";
							l = atTargets;
						}
						if (!(l instanceof List<?>)) continue;
						for (Object o : (List<?>)l){
							if (key.equals("method")){
								methods.add(o.toString().replace("\"", ""));
							} else if (key.equals("target")) {
								targets.add(o.toString().replace("\"", ""));
							}
						}
					}
				}
				Main.append(mixin, bldr);
				Main.append(methods, bldr);
				Main.append(targets, bldr);
			}
		}
		if (!bldr.isEmpty()) {
			try {
				FileWriter fw = new FileWriter("build/tmp/fabToRefMapPre");
				fw.append(bldr);
				fw.close();
			} catch (IOException e) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "failed to write fabToRefMapPre \n" + e);
				e.printStackTrace();
			}
		}
		return true;
	}
}
