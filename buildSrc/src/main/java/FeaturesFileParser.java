import org.gradle.api.Project;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeaturesFileParser {
	String curKey = null;
	JsonObject cur = new JsonObject();
	int lineNum = 0;
	String multilineKey = null;
	String multilineBuf = "";
	String multilineBufRaw = "";
	List<JsonObject> data = new LinkedList<>();
	String projectVersion;
	@Inject
	public FeaturesFileParser() {

	}

	public FeaturesFileParser(Project project, Logger logger, List<String> text, String file) throws IOException {
		this.projectVersion = project.getVersion().toString();
		for (String line : text){
			lineNum++;
			if (line.indexOf('@') == 0) {
				String[] split = line.split(" ");
				switch (split[0]) {
					case "@include":
						File f = project.file(split[1]);
						if (f.isFile()) {
							data.addAll(new FeaturesFileParser(project, logger, Files.readAllLines(f.toPath()), split[1]).data);
						} else if (f.isDirectory()) {
							for (File fi : f.listFiles()) {
								data.addAll(new FeaturesFileParser(project, logger, Files.readAllLines(fi.toPath()), fi.toString()).data);
							}
						}
						break;
					default:
						logger.log(LogLevel.ERROR, "At line "+lineNum+" in "+file+": Unknown at-directive "+split[0]+". Ignoring");
				}
				continue;
			}
			if (line.startsWith("#")) continue;
			String trim = line.trim();
			int leadingTabs = 0;
			for (int i = 0; i < line.length(); i++) {
				if (line.charAt(i) == '\t') {
					leadingTabs++;
				} else {
					break;
				}
			}
			if (leadingTabs == 0) {
				if (trim.length() > 0) {
					if (curKey != null) {
						commit();
					}
					if (trim.lastIndexOf(":") == trim.length()-1) {
						// yaml syntax compatibility
						trim = trim.substring(0, trim.length()-1);
					}
					curKey = trim;
				} else {
					if (multilineKey != null) {
						multilineBuf += "\n";
						multilineBufRaw += "\n";
					}
				}
			} else if (curKey == null) {
				logger.log(LogLevel.ERROR, "At line "+lineNum+" in "+file+": Got an indented line before a key definition. Ignoring");
				continue;
			} else {
				if (leadingTabs == 1) {
					if (multilineKey != null) {
						commitMultiline();
					}
					int colonIdx = trim.indexOf(':');
					if (colonIdx == -1) {
						logger.log(LogLevel.ERROR, "At line "+lineNum+" in "+file+": Got a single-indented line with no colon. Ignoring");
						continue;
					}
					String k = trim.substring(0, colonIdx).trim();
					if (!defaults(curKey, new JsonObject()).has(k)) {
						logger.log(LogLevel.ERROR, "At line "+lineNum+" in "+file+": Got an unknown key "+k+". Ignoring");
						continue;
					}
					String  v = trim.substring(colonIdx+1).trim();
					if ("".equals(v)) {
						multilineKey = k;
					} else {
						switch (k) {
							case "needs":
								String[] sArr = v.split(" ");
								JsonArray arr = new JsonArray(sArr.length);
								for (String str : sArr) arr.add(str);
								cur.add(k, arr);
								break;
							case "endorsed": case "hidden": case "section": case "meta":
								cur.addProperty(k, "true".equals(v));
								break;
							default:
								cur.addProperty(k, v);
								break;
						}
					}
				} else if (leadingTabs >= 2) {
					if (multilineKey != null) {
						multilineBufRaw += line.substring(2)+"\n";
						if (trim.length() == 0) {
							// paragraph separator
							multilineBuf += "\n\n";
						} else {
							multilineBuf += line.substring(2)+" ";
							if (line.endsWith("  ")) multilineBuf += "\n";
						}
					}
				}
			}
		}
		if (curKey != null) commit();
	}

	void commitMultiline() {
		if (multilineKey != null) {
			cur.add(multilineKey, new JsonPrimitive(multilineBuf.trim().replaceAll(" +\\n","\n")));
			cur.add(multilineKey+"_raw", new JsonPrimitive(multilineBufRaw.trim()));
		}
		multilineKey = null;
		multilineBuf = "";
		multilineBufRaw = "";
	}

	JsonObject defaults(String curKey, JsonObject cur){
		JsonObject ret = new JsonObject();
		ret.addProperty("name", curKey);
		ret.add("short_name", cur.get("name"));
		ret.addProperty("meta", false);
		ret.addProperty("section", false);
		ret.addProperty("hidden", false);
		ret.addProperty("extra", false);
		ret.add("since", JsonNull.INSTANCE);
		ret.addProperty("since_code", 0);
		ret.addProperty("sides", "irrelevant");
		ret.add("needs", new JsonArray());
		ret.add("parent", JsonNull.INSTANCE);
		ret.add("media", JsonNull.INSTANCE);
		ret.addProperty("media_text", (cur.has("media") ? (cur.get("media").getAsString().trim().endsWith(".mp4") ? "Demonstration video" : "Demonstration image") : null));
		ret.add("extra_media", JsonNull.INSTANCE);
		ret.addProperty("extra_media_text", (cur.has("extra_media") ? (cur.get("extra_media").getAsString().trim().endsWith(".mp4") ? "Demonstration video" : "Demonstration image") : null));
		ret.add("link_url", JsonNull.INSTANCE);
		ret.addProperty("link_text", (cur.has("link_url") ? "See also" : null));
		String short_desc = null;
		if (cur.has("desc")){
			Matcher matcher = Pattern.compile("\\.( |\\n|$)").matcher(cur.get("desc").getAsString());
			if (matcher.find()) {
				short_desc = cur.get("desc").getAsString().substring(0, matcher.start());
			} else {
				short_desc =  cur.get("desc").getAsString();
			}
		}
		ret.addProperty("short_desc", short_desc);
		ret.add("desc", JsonNull.INSTANCE);
		ret.add("desc_raw", JsonNull.INSTANCE);
		ret.addProperty("brand_new", cur.has("since") && projectVersion.equals(cur.get("since").getAsString()));
		ret.add("fscript", JsonNull.INSTANCE);
		ret.add("fscript_default", JsonNull.INSTANCE);
		ret.add("extra_fscript", JsonNull.INSTANCE);
		ret.add("extend", JsonNull.INSTANCE);
		ret.addProperty("new", false);

		for(Map.Entry<String, JsonElement> entry : cur.entrySet()) {
			ret.add(entry.getKey(), entry.getValue());
		}

		return ret;
	}
	void commit() {
		commitMultiline();
		cur = defaults(curKey, cur);
		cur.addProperty("key", curKey);
		if (curKey.contains(".extra.")) {
			cur.addProperty("extra", true);
		}
		if (cur.has("section") && cur.get("section").getAsBoolean()) {
			createCategory();
		}
		data.add(cur);
		curKey = null;
		cur = new JsonObject();
	}

	void createCategory() {
		if (cur.has("key") && "general".equals(cur.get("key").getAsString())) return;
		JsonObject curDupe = cur.deepCopy();
		curDupe.addProperty("key", "general.category." + cur.get("key").getAsString());
		curDupe.addProperty("desc", "Enable all features in " + cur.get("name").getAsString() + "\n" + (cur.has("desc") && !cur.get("desc").isJsonNull() ? cur.get("desc").getAsString() : ""));
		curDupe.addProperty("section", false);
		data.add(curDupe);
	}

	JsonObject toJson(){
		List<String> sections = List.of("general", "fixes", "utility", "tweaks", "minor_mechanics", "mechanics", "balance", "weird_tweaks", "woina", "unsafe", "pedantry", "experiments");
		data.sort((a, b) -> {
			String sectionA = a.get("key").getAsString().indexOf('.') == -1 ? a.get("key").getAsString() : a.get("key").getAsString().substring(0, a.get("key").getAsString().indexOf('.'));
			String sectionB = b.get("key").getAsString().indexOf('.') == -1 ? b.get("key").getAsString() : b.get("key").getAsString().substring(0, b.get("key").getAsString().indexOf('.'));
			if (sectionA.equals(sectionB) && sectionA.equals(a.get("key").getAsString())) return -1;
			if (sectionA.equals(sectionB) && sectionB.equals(b.get("key").getAsString())) return 1;
			if (!sectionA.equals(sectionB)) return sections.indexOf(sectionA) - sections.indexOf(sectionB);
			if (a.get("meta").getAsBoolean() != b.get("meta").getAsBoolean()) return a.get("meta").getAsBoolean() ? -1 : 1;
			return a.get("key").getAsString().compareTo(b.get("key").getAsString());
		});
		Set<String> _allVersions = new HashSet<>();
		for (JsonObject d : data) {
			if (d.get("since").isJsonNull()) continue;
			_allVersions.add(d.get("since").getAsString());
		}
		List<String> allVersions = new LinkedList<>(_allVersions);
		allVersions.sort((v1, v2) -> {
			List<Integer> longer = dismantleVersion(v1);
			List<Integer> shorter = dismantleVersion(v2);
			boolean firstLonger = longer.size() > shorter.size();
			if (!firstLonger) {
				List<Integer> l2 = shorter;
				shorter = longer;
				longer = l2;
			}
			int i = 0;
			for (; i<shorter.size(); i++){
				int comp = Integer.compare(longer.get(i), shorter.get(i));
				if (comp != 0) return firstLonger ? comp : -comp;
			}
			for (; i<longer.size(); i++){
				if (longer.get(i)>0) return firstLonger ? 1 : -1;
			}
			return 0;
		});

		for (JsonObject d : data) {
			if (d.get("since").isJsonNull()) continue;
			int i = allVersions.indexOf(d.get("since").getAsString());
			d.addProperty("since_code", i);
			d.addProperty("new", i+1>=allVersions.size());
		}

		JsonObject dataObj = new JsonObject();
		for (JsonObject d : data) {
			String key = d.get("key").getAsString();
			d.remove("key");
			dataObj.add(key, d);
		}

		return dataObj;
	}
	static List<Integer> dismantleVersion(String version){
		List<Integer> out = new LinkedList<>();
		out.add(0);
		version.chars().forEach(chr->{
			if (chr >= '0' && chr <= '9') {
				out.set(out.size()-1, out.get(out.size()-1)*10 + (chr-'0'));
			} else if (out.get(out.size()-1) > 0) {
				out.add(0);
			}
		});
		return out;
	}
}
