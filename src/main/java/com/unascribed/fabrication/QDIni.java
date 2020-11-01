/*
 * Spinor
 * Copyright (c) 2020 Una Thompson
 *
 * This Source Code Form is subject to the terms of the Spinor Public
 * License, v. 2020.04. If a copy of the SPL was not distributed with
 * this file, You can obtain one at http://spinor.im/SPL/2020.04/.
 */

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
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

/**
 * A quick-and-dirty INI parser.
 */
public class QDIni {
	public interface IniTransformer {
		String transformLine(String path, String line);
		String transformValueComment(String key, String value, String comment);
		String transformValue(String key, String value);
	}

	private static final Splitter EQUALS_SPLITTER = Splitter.on('=').trimResults().limit(2);
	
	private QDIni() {}
	
	public static Map<String, String> load(String s) {
		try {
			return load(new StringReader(s));
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}
	
	public static Map<String, String> load(File f) throws IOException {
		try(InputStream in = new FileInputStream(f)) {
			return load(in);
		}
	}
	
	public static Map<String, String> load(InputStream in) throws IOException {
		return load(new InputStreamReader(in, Charsets.UTF_8));
	}
	
	public static Map<String, String> load(Reader r) throws IOException {
		return loadAndTransform(r, null, null);
	}

	public static Map<String, String> loadAndTransform(Reader r, IniTransformer transformer, Writer w) throws IOException {
		BufferedReader br = r instanceof BufferedReader ? (BufferedReader)r : new BufferedReader(r);
		Map<String, String> rtrn = Maps.newLinkedHashMap();
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
				if (line.contains(";")) {
					trunc = line.substring(0, line.indexOf(';'));
				}
				trunc = trunc.trim();
				if (trunc.endsWith("]")) {
					path = trunc.substring(1, trunc.length()-1);
					if (path.contains("[") || path.contains("]")) {
						throw new IOException("Malformed section header at line "+lineNum);
					}
					if (!(path.isEmpty() || path.endsWith(":") || path.endsWith("."))) {
						path += ".";
					}
				} else {
					throw new IOException("Malformed section header at line "+lineNum);
				}
			} else if (line.contains("=")) {
				String comment = null;
				if (trunc.contains(";")) {
					comment = trunc.substring(trunc.indexOf(';')+1);
					trunc = trunc.substring(0, trunc.indexOf(';'));
				}
				trunc = trunc.trim();
				Iterator<String> splitter = EQUALS_SPLITTER.split(trunc).iterator();
				String key = path+splitter.next();
				String value = splitter.next();
				if (transformer != null) {
					String newValue = transformer.transformValue(key, value);
					String newComment = transformer.transformValueComment(key, value, comment);
					if (!Objects.equal(value, newValue)) {
						line = line.replaceFirst("=(\\s*)\\Q"+value+"\\E", "=$1"+newValue);
					}
					if (!Objects.equal(comment, newComment)) {
						line = (line.contains(";") ? line.substring(0, line.indexOf(';')+1) : line+" ;")+newComment;
					}
				}
				rtrn.put(key, value);
			} else {
				throw new IOException("Couldn't find a section, comment, or key-value assigment at line "+lineNum);
			}
			if (w != null) {
				w.write(line);
				w.write("\r\n");
			}
			lineNum++;
		}
		return rtrn;
	}
}
