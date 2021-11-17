package com.unascribed.fabrication.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.minecraft.client.texture.NativeImage;

public class Grayscale extends InputStream {
	final InputStream stream;
	final IOException err;
	public Grayscale(InputStream original) {
		InputStream rtrn = null;
		IOException er = null;
		BufferedInputStream in = new BufferedInputStream(original);
		try {
			NativeImage image = NativeImage.read(in);
			for(int h = 0; h < image.getHeight() ; h++) {
				for(int w = 0; w < image.getWidth() ; w++) {
					int color = image.getPixelColor(w, h);
					int gray = ((color>>16 & 255) + (color>>8 & 255) + (color & 255))/3;
					image.setPixelColor(w, h,(color & 0xff000000) | gray << 16 | gray << 8 | gray);
				}
			}
			rtrn = new ByteArrayInputStream(image.getBytes());
		} catch (IOException e) {
			er = e;
		}
		err = er;
		stream = rtrn;
	}
	@Override
	public int read() throws IOException {
		if (err != null) throw err;
		return stream.read();
	}
}
