package com.unascribed.fabricationap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class Main {

	public static void main(String[] args) throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Main.class.getClassLoader();
		File file = new File("../build/tmp/fabToRefMapPre");
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
		StringBuilder out = new StringBuilder();
		while (reader.ready()) {
			String source = reader.readLine();
			if (!reader.ready()) break;
			String[] mixins = reader.readLine().split("\t");
			if (!reader.ready()) break;
			String methods = reader.readLine();
			if (!reader.ready()) break;
			String[] targets = reader.readLine().split("\t");

			Set<String> outMixins = new HashSet<>();
			Set<String> outTargets = new HashSet<>();
			for (String ad : mixins) {
				try {
					Class<?> cl;
					try {
						cl = Class.forName(ad, false, classLoader);
					} catch (ClassNotFoundException ignore) {
						int dot = ad.lastIndexOf('.');
						try {
							cl = Class.forName(ad.substring(0, dot) + "$" + ad.substring(dot + 1), false, classLoader);
						} catch (StringIndexOutOfBoundsException ingore) {
							continue;
						}
					}
					while (cl != null) {
						outMixins.add(cl.getName());
						for (Class<?> cla : cl.getInterfaces()) outMixins.add(cla.getName());
						try {
							for (Method m : cl.getMethods()) outMixins.add(m.getDeclaringClass().getName());
						} catch (VerifyError er) {
							//doesn't appear to matter?
						}
						cl = cl.getSuperclass();
					}
				} catch (Exception ex) {
					System.out.print("failed to reflect class " + ad + "\n" + ex);
					ex.printStackTrace();
					//TODO this is new so throwing is probably a good idea
					throw ex;
				}
			}
			for (String ad : targets) {
				StringBuilder strb = new StringBuilder();
				strb.append(ad);
				Set<String> add = new HashSet<>();
				int col = ad.indexOf(';');
				int dot = ad.indexOf('.');
				if (col == -1 || dot < col && dot != -1) col = dot;
				try {
					Class<?> cl;
					try {
						cl = Class.forName(ad.substring(ad.charAt(0) == 'L' ? 1 : 0, col).replace('/', '.'), false, classLoader);
					} catch (StringIndexOutOfBoundsException ingore) {
						continue;
					}
					while (cl != null) {
						add.add(cl.getName());
						for (Class<?> cla : cl.getInterfaces()) add.add(cla.getName());
						for (Field f : cl.getFields()) add.add(f.getDeclaringClass().getName());
						for (Method m : cl.getMethods()) add.add(m.getDeclaringClass().getName());
						cl = cl.getSuperclass();
					}
				} catch (Exception ex) {
					System.out.print("failed to reflect class " + ad + "\n" + ex);
					ex.printStackTrace();
					//TODO this is new so throwing is probably a good idea
					throw ex;
				}
				for (String s : add){
					strb.append(' ');
					strb.append(s);
				}
				outTargets.add(strb.toString());
			}

			out.append(source).append('\n');
			Main.append(outMixins, out);
			out.append(methods).append('\n');
			Main.append(outTargets, out);
		}
		if (!out.isEmpty()) {
			try {
				FileWriter fw = new FileWriter("../build/tmp/fabToRefMap");
				fw.write(out.toString());
				fw.close();
			} catch (IOException ex) {
				System.out.println("failed to write fabToRefMap \n" + ex);
				ex.printStackTrace();
				//TODO this is new so throwing is probably a good idea
				throw ex;
			}
		}
		Class.forName("net.minecraft.client.util.Window");
	}
	public static void append(Set<String> l, StringBuilder bldr) {
		boolean first = true;
		for (String s : l){
			if (first) first = false;
			else bldr.append('\t');
			bldr.append(s);
		}
		bldr.append('\n');
	}
}
