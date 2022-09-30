package com.unascribed.fabrication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A quick-and-dirty INI parser.
 */
public class QDIni {
	public static class QDIniException extends IllegalArgumentException {
		public QDIniException() {}
		public QDIniException(String message, Throwable cause) { super(message, cause); }
		public QDIniException(String s) { super(s); }
		public QDIniException(Throwable cause) { super(cause); }
	}
	public static class BadValueException extends QDIniException {
		public BadValueException() {}
		public BadValueException(String message, Throwable cause) { super(message, cause); }
		public BadValueException(String s) { super(s); }
		public BadValueException(Throwable cause) { super(cause); }
	}
	public static class SyntaxErrorException extends QDIniException {
		public SyntaxErrorException() {}
		public SyntaxErrorException(String message, Throwable cause) { super(message, cause); }
		public SyntaxErrorException(String s) { super(s); }
		public SyntaxErrorException(Throwable cause) { super(cause); }
	}

	public static class CompositeIniTransformer implements IniTransformer{
		final IniTransformer first;
		final IniTransformer second;
		CompositeIniTransformer(IniTransformer first, IniTransformer second) {
			this.first = first;
			this.second = second;
		}
		@Override
		public String transformLine(String path, String line) {
			return second.transformLine(path, first.transformLine(path, line));
		}

		@Override
		public String transformValueComment(String key, String value, String comment) {
			return second.transformValueComment(key, value, first.transformValueComment(key, value, comment));
		}

		@Override
		public String transformValue(String key, String value) {
			return second.transformValue(key, first.transformValue(key, value));
		}
	}
	public interface IniTransformer {
		static IniTransformer simpleValueIniTransformer(ValueIniTransformer transformer){
			return new IniTransformer() {
				@Override
				public String transformLine(String path, String line) {
					return line;
				}

				@Override
				public String transformValueComment(String key, String value, String comment) {
					return comment;
				}

				@Override
				public String transformValue(String key, String value) {
					return transformer.transformValue(key, value);
				}
			};
		}

		static IniTransformer simpleLineIniTransformer(ValueLineTransformer transformer){
			return new IniTransformer() {
				@Override
				public String transformLine(String path, String line) {
					return transformer.transformLine(path, line);
				}

				@Override
				public String transformValueComment(String key, String value, String comment) {
					return comment;
				}

				@Override
				public String transformValue(String key, String value) {
					return value;
				}
			};
		}
		default IniTransformer andThen(IniTransformer other) {
			return new CompositeIniTransformer(this, other);
		}

		String transformLine(String path, String line);
		String transformValueComment(String key, String value, String comment);
		String transformValue(String key, String value);
	}

	@FunctionalInterface
	public interface ValueIniTransformer{
		String transformValue(String key, String value);
	}

	@FunctionalInterface
	public interface ValueLineTransformer{
		String transformLine(String path, String line);
	}

	private static class BlameString {
		public final String value;
		public final String file;
		public final int line;

		private BlameString(String value) {
			this(value, null, -1);
		}

		private BlameString(String value, String file, int line) {
			this.value = value;
			this.file = file;
			this.line = line;
		}
		public String blame() {
			return line == -1 ? "<unknown>" : "line "+line+" in "+file;
		}
	}

	private final String prelude;
	private final Map<String, List<BlameString>> data;

	private Consumer<String> yapLog = FabLog::warn;

	private QDIni(String prelude, Map<String, List<BlameString>> data) {
		this.prelude = prelude;
		this.data = data;
	}

	/**
	 * Enables/Disables "yap" mode for parse failures in this config, where rather than throwing a
	 * BadValueException a warning string will be sent to this Consumer and an empty Optional
	 * returned to the caller of get*.
	 * <p>
	 * If yapLog is null, "yap" mode is turned off.
	 */
	public void setYapLog(Consumer<String> yapLog) {
		this.yapLog = yapLog;
	}

	public boolean containsKey(String key) {
		return data.containsKey(key) && !data.get(key).isEmpty();
	}

	public void put(String key, String value) {
		List<BlameString> li = new ArrayList<>();
		li.add(new BlameString(value));
		data.put(key, li);
	}

	public void put(String key, String... values) {
		List<BlameString> li = new ArrayList<>();
		for (String v : values) {
			li.add(new BlameString(v));
		}
		data.put(key, li);
	}

	public void put(String key, Iterable<String> values) {
		List<BlameString> li = new ArrayList<>();
		for (String v : values) {
			li.add(new BlameString(v));
		}
		data.put(key, li);
	}

	/**
	 * Return all defined values for the given key, or an empty list if it's not defined.
	 */
	public List<String> getAll(String key) {
		return unwrap(data.get(key));
	}

	private List<BlameString> getAllBlamed(String key) {
		return data.containsKey(key) ? data.get(key) : Collections.emptyList();
	}

	public String getBlame(String key) {
		return getBlamed(key).map(BlameString::blame).orElse("<unknown>");
	}

	public String getBlame(String key, int index) {
		if (containsKey(key)) {
			return getAllBlamed(key).get(index).blame();
		}
		return "<unknown>";
	}

	private List<String> unwrap(List<BlameString> list) {
		if (list == null) return Collections.emptyList();
		return new AbstractList<String>() {

			@Override
			public String get(int index) {
				return unwrap(list.get(index));
			}

			@Override
			public int size() {
				return list.size();
			}

		};
	}

	private String unwrap(BlameString bs) {
		if (bs == null) return null;
		return bs.value;
	}

	/**
	 * Return the last defined value for the given key.
	 */
	public Optional<String> get(String key) {
		return Optional.ofNullable(getLast(getAll(key)));
	}

	private Optional<BlameString> getBlamed(String key) {
		return Optional.ofNullable(getLast(getAllBlamed(key)));
	}

	public Optional<Integer> getInt(String key) throws BadValueException {
		return getParsed(key, Integer::parseInt, () -> "a whole number");
	}

	public Optional<Double> getDouble(String key) throws BadValueException {
		return getParsed(key, Double::parseDouble, () -> "a number");
	}

	public Optional<Boolean> getBoolean(String key) throws BadValueException {
		return getParsed(key, this::strictParseBoolean, () -> "true or false");
	}

	private boolean strictParseBoolean(String s) {
		switch (s.toLowerCase(Locale.ROOT)) {
			case "true": return true;
			case "false": return false;
			default: throw new IllegalArgumentException();
		}
	}

	public <E extends Enum<E>> Optional<E> getEnum(String key, Class<E> clazz) throws BadValueException {
		return getParsed(key, s -> Enum.valueOf(clazz, s.toUpperCase(Locale.ROOT)), () -> {
			StringBuilder sb = new StringBuilder("one of ");
			boolean first = true;
			for (E e : clazz.getEnumConstants()) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				sb.append(e.name().toLowerCase(Locale.ROOT));
			}
			return sb.toString();
		});
	}

	private <T> Optional<T> getParsed(String key, Function<String, ? extends T> parser, Supplier<String> error) throws BadValueException {
		Optional<String> s = get(key);
		if (!s.isPresent()) return Optional.empty();
		try {
			return Optional.of(parser.apply(s.get()));
		} catch (IllegalArgumentException e) {
			String msg = key+" must be "+error.get()+" (got "+s.get()+") at "+getBlame(key);
			if (yapLog != null) {
				yapLog.accept(msg);
				return Optional.empty();
			} else {
				throw new BadValueException(msg, e);
			}
		}
	}

	private <T> T getLast(List<T> list) {
		return list == null || list.isEmpty() ? null : list.get(list.size()-1);
	}

	public Set<String> keySet() {
		return data.keySet();
	}

	public Set<Map.Entry<String, List<String>>> entrySet() {
		return new AbstractSet<Map.Entry<String, List<String>>>() {

			@Override
			public Iterator<Map.Entry<String, List<String>>> iterator() {
				Iterator<Map.Entry<String, List<BlameString>>> delegate = data.entrySet().iterator();
				return new Iterator<Map.Entry<String, List<String>>>() {

					@Override
					public boolean hasNext() {
						return delegate.hasNext();
					}

					@Override
					public Map.Entry<String, List<String>> next() {
						Map.Entry<String, List<BlameString>> den = delegate.next();
						return new AbstractMap.SimpleImmutableEntry<>(den.getKey(), unwrap(den.getValue()));
					}
				};
			}

			@Override
			public int size() {
				return size();
			}

		};
	}

	public int size() {
		return data.size();
	}

	/**
	 * Lossily convert this QDIni's data back into an INI. Comments, section declarations, etc will
	 * be lost. If you want to modify an ini file rather than just do basic debugging, you should
	 * use {@link QDIni#loadAndTransform}.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("; Loaded from ");
		sb.append(prelude);
		sb.append("\r\n");
		for (Map.Entry<String, List<String>> en : entrySet()) {
			for (String v : en.getValue()) {
				sb.append(en.getKey());
				sb.append("=");
				sb.append(v);
				sb.append("\r\n");
			}
		}
		return sb.toString();
	}

	/**
	 * Merge the given QDIni's data with this QDIni's data, returning a new QDIni object. Keys
	 * defined in the given QDIni will have their values appended to this one's. For usages of
	 * {@link #get}, this is equivalent to an override.
	 */
	public QDIni merge(QDIni that) {
		Map<String, List<BlameString>> newData = new LinkedHashMap<>(Math.max(this.size(), that.size()));
		newData.putAll(data);
		for (Map.Entry<String, List<BlameString>> en : that.data.entrySet()) {
			if (newData.containsKey(en.getKey())) {
				List<BlameString> merged = new ArrayList<>(newData.get(en.getKey()).size()+en.getValue().size());
				merged.addAll(newData.get(en.getKey()));
				merged.addAll(en.getValue());
				newData.put(en.getKey(), Collections.unmodifiableList(merged));
			} else {
				newData.put(en.getKey(), en.getValue());
			}
		}
		return new QDIni(prelude+", merged with "+that.prelude, Collections.unmodifiableMap(newData));
	}

	/**
	 * Return a view of this QDIni's data, dropping multivalues and collapsing to a basic key-value
	 * mapping that returns the last defined value for any given key.
	 */
	public Map<String, String> flatten() {
		return new AbstractMap<String, String>() {

			@Override
			public String get(Object key) {
				return QDIni.this.get((String)key).orElse(null);
			}

			@Override
			public boolean containsKey(Object key) {
				return QDIni.this.containsKey((String)key);
			}

			@Override
			public Set<String> keySet() {
				return QDIni.this.keySet();
			}

			@Override
			public int size() {
				return QDIni.this.size();
			}

			@Override
			public Set<Entry<String, String>> entrySet() {
				return new AbstractSet<Map.Entry<String,String>>() {

					@Override
					public Iterator<Entry<String, String>> iterator() {
						Iterator<Entry<String, List<String>>> delegate = QDIni.this.entrySet().iterator();
						return new Iterator<Map.Entry<String,String>>() {

							@Override
							public boolean hasNext() {
								return delegate.hasNext();
							}

							@Override
							public Entry<String, String> next() {
								Entry<String, List<String>> den = delegate.next();
								return new SimpleImmutableEntry<>(den.getKey(), getLast(den.getValue()));
							}
						};
					}

					@Override
					public int size() {
						return size();
					}

				};
			}

		};
	}

	public static QDIni load(String fileName, String s) {
		try {
			return load(fileName, new StringReader(s));
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	public static QDIni load(File f) throws IOException {
		try (InputStream in = new FileInputStream(f)) {
			return load(f.getName(), in);
		}
	}

	public static QDIni load(Path p) throws IOException {
		try (InputStream in = Files.newInputStream(p)) {
			return load(p.getFileName().toString(), in);
		}
	}

	public static QDIni load(String fileName, InputStream in) throws IOException {
		return load(fileName, new InputStreamReader(in, StandardCharsets.UTF_8));
	}

	public static QDIni load(String fileName, Reader r) throws IOException {
		return loadAndTransform(fileName, r, null, null);
	}

	public static QDIni loadAndTransform(String fileName, String s, IniTransformer transformer, Writer w) {
		try {
			return loadAndTransform(fileName, new StringReader(s), transformer, w);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	public static QDIni loadAndTransform(File f, IniTransformer transformer, Writer w) throws IOException {
		try (InputStream in = new FileInputStream(f)) {
			return loadAndTransform(f.getName(), in, transformer, w);
		}
	}

	public static QDIni loadAndTransform(Path p, IniTransformer transformer, Writer w) throws IOException {
		try (InputStream in = Files.newInputStream(p)) {
			return loadAndTransform(p.getFileName().toString(), in, transformer, w);
		}
	}

	public static QDIni loadAndTransform(String fileName, InputStream in, IniTransformer transformer, Writer w) throws IOException {
		return loadAndTransform(fileName, new InputStreamReader(in, StandardCharsets.UTF_8), transformer, w);
	}
	public static QDIni loadAndTransform(String fileName, Reader r, IniTransformer transformer, Writer w) throws IOException, SyntaxErrorException {
		return loadAndTransform(fileName, r, transformer, w, true);
	}
	public static QDIni loadAndTransform(String fileName, Reader r, IniTransformer transformer, Writer w, boolean inValComments) throws IOException, SyntaxErrorException {
		BufferedReader br = r instanceof BufferedReader ? (BufferedReader)r : new BufferedReader(r);
		Map<String, List<BlameString>> data = new LinkedHashMap<>();
		int lineNum = 1;
		String path = "";
		while (true) {
			String line = br.readLine();
			if (transformer != null) {
				boolean eof = line == null;
				line = transformer.transformLine(path, line);
				if (line == null) {
					if (eof) {
						break;
					} else {
						continue;
					}
				}
				if (eof) {
					if (w != null) {
						w.write(line);
						w.write("\r\n");
					}
					break;
				}
			}
			if (line == null) break;
			String trunc = line.trim();
			if (trunc.startsWith(";") || trunc.isEmpty()) {
				if (w != null) {
					w.write(line);
					w.write("\r\n");
				}
				lineNum++;
				continue;
			}
			if (line.startsWith("[")) {
				if (line.contains(";") && inValComments) {
					trunc = line.substring(0, line.indexOf(';'));
				}
				trunc = trunc.trim();
				if (trunc.endsWith("]")) {
					path = trunc.substring(1, trunc.length()-1);
					if (path.contains("[") || path.contains("]")) {
						throw new SyntaxErrorException("Malformed section header at line "+lineNum+" in "+fileName);
					}
					if (!(path.isEmpty() || path.endsWith(":") || path.endsWith("."))) {
						path += ".";
					}
				} else {
					throw new SyntaxErrorException("Malformed section header at line "+lineNum+" in "+fileName);
				}
			} else if (line.contains("=")) {
				String comment = null;
				if (trunc.contains(";") && inValComments) {
					comment = trunc.substring(trunc.indexOf(';')+1);
					trunc = trunc.substring(0, trunc.indexOf(';'));
				}
				trunc = trunc.trim();
				int equals = trunc.indexOf('=');
				String key = path+trunc.substring(0, equals);
				String value = trunc.substring(equals+1);
				if (transformer != null) {
					String newValue = transformer.transformValue(key, value);
					String newComment = transformer.transformValueComment(key, value, comment);
					if (!Objects.equals(value, newValue)) {
						line = line.replaceFirst("=(\\s*)\\Q"+value+"\\E", "=$1"+newValue);
					}
					if (!Objects.equals(comment, newComment)) {
						line = (line.contains(";") ? line.substring(0, line.indexOf(';')+1) : line+" ;")+newComment;
					}
				}
				final int lineNumF = lineNum;
				data.compute(key, (k, l) -> {
					if (l == null) l = new ArrayList<>();
					l.add(new BlameString(value, fileName, lineNumF));
					return l;
				});
			} else {
				throw new SyntaxErrorException("Couldn't find a section, comment, or key-value assigment at line "+lineNum+" in "+fileName);
			}
			if (w != null) {
				w.write(line);
				w.write("\r\n");
			}
			lineNum++;
		}
		return new QDIni(fileName, data);
	}

}
