import com.google.gson.JsonObject;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class FabFeaturesPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.task("fabGenFeatures", task -> {
			try {
				FeaturesFileParser ffp = new FeaturesFileParser(project, project.getLogger(), Arrays.asList(Files.readString(project.file("features.yml").toPath()).split("\r?\n")), "features.yml");
				JsonObject output = ffp.toJson();
				Files.writeString(project.file("src/main/resources/features.json").toPath(), output.toString());
				Files.writeString(project.file("src/main/resources/default_features_config.ini").toPath(),
					FeatureConfigTransformer.transform(Arrays.asList(Files.readString(project.file("src/main/resources/default_features_config.ini.tmpl").toPath()).split("\r?\n")), output)
				);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}
}
