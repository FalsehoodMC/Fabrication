package com.unascribed.fabricationap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("com.unascribed.fabrication.support.FabReflField")
public class FabReflAnnotationProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (!annotations.isEmpty()) {
			try {
				FileWriter fw = new FileWriter("build/tmp/fabReflToMap");
				for (TypeElement te: annotations) {
					for (VariableElement e : ElementFilter.fieldsIn(roundEnv.getElementsAnnotatedWith(te))) {
						String str = e.getConstantValue().toString();
						fw.append(str)
								.append(' ')
								.append(str.substring(str.charAt(0)=='L'?1:0, str.indexOf(';')).replace('/', '.'))
								.append('\n');
					}
				}
				fw.close();
			} catch (IOException e) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "failed to write fabReflToMap\n" + e);
				e.printStackTrace();
			}
		}
		return true;
	}
}
