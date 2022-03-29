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
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"com.unascribed.fabrication.support.injection.ModifyReturn", "com.unascribed.fabrication.support.injection.Hijack"})
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
					if (!"org.spongepowered.asm.mixin.Mixin".equals(am.getAnnotationType().toString())) continue;
					for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> ae : am.getElementValues().entrySet()){
						Name key = ae.getKey().getSimpleName();
						Object l = ae.getValue().getValue();
						if (!((key.contentEquals("value") || key.contentEquals("targets")) && l instanceof List<?>)) continue;
						for (Object o : (List<?>)l) {
							String co = o.toString();
							String ad = o instanceof String ? co : co.substring(0, co.length()-6);
							mixin.add(ad);
							try {
								Class<?> cl = Class.forName(ad, false, this.getClass().getClassLoader());
								//Loops quite a few too many times but eh it's a build only operation
								while (cl != null) {
									mixin.add(cl.getName());
									for (Class<?> cla : cl.getInterfaces()) mixin.add(cla.getName());
									for (Method m : cl.getMethods()) mixin.add(m.getDeclaringClass().getName());
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
						Name key = ae.getKey().getSimpleName();
						Object l = ae.getValue().getValue();
						if (!(l instanceof List<?>)) continue;
						for (Object o : (List<?>)l){
							if (key.contentEquals("method")){
								methods.add(o.toString().replace("\"", ""));
							} else if (key.contentEquals("target")) {
								String ad = o.toString().replace("\"", "");
								StringBuilder strb = new StringBuilder();
								strb.append(ad);
								Set<String> add = new HashSet<>();
								int col = ad.indexOf(';');
								int dot = ad.indexOf('.');
								if (col == -1 || dot < col && dot != -1) col = dot;
								try {
									Class<?> cl = Class.forName(ad.substring(ad.charAt(0) == 'L' ? 1 : 0, col).replace('/', '.'), false, this.getClass().getClassLoader());
									//Loops quite a few too many times but eh it's a build only operation
									while (cl != null) {
										add.add(cl.getName());
										for (Class<?> cla : cl.getInterfaces()) add.add(cla.getName());
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
