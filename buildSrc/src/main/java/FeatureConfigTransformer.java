import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

public class FeatureConfigTransformer {
	public static String transform(List<String> lines, JsonObject data){
		StringBuilder out = new StringBuilder();
		int lineNum = 0;
		for (String line : lines) {
			lineNum++;
			String trim = line.trim();
			if (";!!;".equals(trim)) {
				for(Map.Entry<String, JsonElement> entry : data.entrySet()){
					String k = entry.getKey();
					JsonObject datum = entry.getValue().getAsJsonObject();
					int leadingTabs = 0;
					if (k.indexOf('.') != -1) {
						leadingTabs = 1;
						if (!datum.get("parent").isJsonNull()) {
							leadingTabs++;
							if (datum.get("meta").getAsBoolean()) {
								continue;
							}
						}
					} else if (datum.get("meta").getAsBoolean()) {
						continue;
					}
					String val = "unset";
					String desc = datum.get("desc").getAsString();
					String sides_friendly = null;
					switch (datum.get("sides").getAsString()) {
						case "irrelevant": break;
						case "either": sides_friendly = "Server or Client"; break;
						case "client_only": sides_friendly = "Client Only"; break;
						case "server_only": sides_friendly = "Server Only"; break;
						case "server_only_with_client_helper": sides_friendly = "Server & Client (Client Optional)"; break;
						case "server_and_client": sides_friendly = "Server & Client"; break;
					}
					if (sides_friendly != null) {
						desc = sides_friendly+"\n\n"+desc;
					}
					if (k.startsWith("general.category.")) {
						switch (k) {
							case "general.category.fixes":
							case "general.category.utility":
							case "general.category.tweaks":
								val = "true";
								break;
							default:
								val = "false";
								break;
						}
					} else if (k.startsWith("general.")) {
						val = "false";
					}
					desc = foldLine(desc, 78-(leadingTabs*8));
					if (datum != null){
						if (!datum.get("media").isJsonNull() || !datum.get("extra_media").isJsonNull() || !datum.get("link_url").isJsonNull()) desc += '\n';
						if (!datum.get("media").isJsonNull()) {
							desc += '\n'+datum.get("media_text").getAsString()+": "+datum.get("media").getAsString();
						}
						if (!datum.get("extra_media").isJsonNull()) {
							desc += '\n'+datum.get("extra_media_text").getAsString()+": "+datum.get("extra_media").getAsString();
						}
						if (!datum.get("link_url").isJsonNull()) {
							desc += '\n'+datum.get("link_text").getAsString()+": "+datum.get("link_url").getAsString();
						}
					}
					String tabs = "";
					for (int i=0; i<leadingTabs; i++) {
						tabs += '\t';
					}
					desc = tabs+"; "+desc;
					out.append(desc.replaceAll("\r?\n", "\r\n"+tabs+"; ")).append("\r\n");
					if (k.indexOf('.') != -1) {
						out.append(tabs).append(k.substring(k.indexOf('.')+1)).append("=").append(val).append("\r\n\r\n");
					} else {
						out.append(tabs).append("[").append(k).append("]\r\n");
					}
				}
			} else {
				out.append(line);
				out.append("\r\n");
			}
		}
		return out.toString();
	}
	public static String foldLine(String input, int length){
		String[] lines = input.split(" ");
		if (input.length() < length || lines.length == 0) return input + "\r\n";
		StringBuilder out = new StringBuilder();
		StringBuilder line = new StringBuilder(lines[0]).append(' ');
		int i = 1;
		while(true) {
			if (line.length() + lines[i].length() < length) {
				line.append(lines[i]).append(' ');
				if (++i>=lines.length) {
					out.append(line.append("\r\n"));
					break;
				}
			} else {
				out.append(line.append("\r\n"));
				line = new StringBuilder(lines[i]).append(' ');
				if (++i>=lines.length) {
					out.append(line);
					break;
				}
			}
		}
		return out.toString();
	}
}
