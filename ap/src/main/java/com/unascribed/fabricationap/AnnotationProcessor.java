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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
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
				Set<String> fields = new HashSet<>();
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
							try {
								Class<?> cl;
								try {
									cl = Class.forName(ad, false, this.getClass().getClassLoader());
								} catch (ClassNotFoundException ignore) {
									int dot = ad.lastIndexOf('.');
									cl = Class.forName(ad.substring(0, dot)+"$"+ad.substring(dot+1), false, this.getClass().getClassLoader());
								}
								while (cl != null) {
									mixin.add(cl.getName());
									for (Class<?> cla : cl.getInterfaces()) mixin.add(cla.getName());
									try {
										for (Method m : cl.getMethods()) mixin.add(m.getDeclaringClass().getName());
									} catch (VerifyError er) {
										//doesn't seam to actually matter?
										//processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Fabrication Annotation Processor VerifyError");
										//er.printStackTrace();
									}
									cl = cl.getSuperclass();
								}
							} catch (Exception ex) {
								processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "failed to reflect class " + ad + "\n" + e);
								ex.printStackTrace();
							}
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
								String ad = o.toString().replace("\"", "");
								StringBuilder strb = new StringBuilder();
								strb.append(ad);
								Set<String> add = new HashSet<>();
								int col = ad.indexOf(';');
								int dot = ad.indexOf('.');
								if (col == -1 || dot < col && dot != -1) col = dot;
								try {
									Class<?> cl = Class.forName(ad.substring(ad.charAt(0) == 'L' ? 1 : 0, col).replace('/', '.'), false, this.getClass().getClassLoader());
									while (cl != null) {
										add.add(cl.getName());
										for (Class<?> cla : cl.getInterfaces()) add.add(cla.getName());
										for (Field f : cl.getFields()) add.add(f.getDeclaringClass().getName());
										for (Method m : cl.getMethods()) add.add(m.getDeclaringClass().getName());
										cl = cl.getSuperclass();
									}
								} catch (Exception ex) {
									processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "failed to reflect class " + ad + "\n" + e);
									ex.printStackTrace();
								}
								for (String s : add){
									strb.append(' ');
									strb.append(s);
								}
								targets.add(strb.toString());
							}
						}
					}
				}
				for (Set<String> l : new Set[] {mixin, methods, targets}){
					boolean first = true;
					for (String s : l){
						if (first) first = false;
						else bldr.append('\t');
						bldr.append(s);
					}
					bldr.append('\n');
				}
			}
		}
		if (!bldr.isEmpty()) {
			try {
				FileWriter fw = new FileWriter("build/tmp/fabToRefMap");
				fw.append(bldr);
				fw.close();
			} catch (IOException e) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "failed to write fabToRefMap \n" + e);
				e.printStackTrace();
			}
		}
		return true;
	}
}
