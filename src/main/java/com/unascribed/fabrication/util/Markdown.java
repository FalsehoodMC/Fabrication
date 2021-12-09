package com.unascribed.fabrication.util;

import java.util.regex.Pattern;

public class Markdown {

	public static final Pattern strike = Pattern.compile("~~(.+)~~");
	public static final Pattern under = Pattern.compile("~(.+)~");
	public static final Pattern bold = Pattern.compile("[_*][_*](.+)[_*][_*]");
	public static final Pattern italic = Pattern.compile("[_*](.+)[_*]");
	public static final Pattern dedup = Pattern.compile("(§r)+");

	public static String convert(String in){
		return dedup.matcher(italic.matcher(bold.matcher(under.matcher(strike.matcher(in).replaceAll("§m$1§r")).replaceAll("§n$1§r")).replaceAll("§l$1§r")).replaceAll("§o$1§r")).replaceAll("§r");
	}
}
