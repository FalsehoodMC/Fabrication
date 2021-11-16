package com.unascribed.fabrication.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class Grayscale extends InputStream {
    final InputStream stream;
    final IOException err;
    public Grayscale(InputStream original) {
        InputStream rtrn = null;
        IOException er = null;
        BufferedInputStream in = new BufferedInputStream(original);
        try {
            BufferedImage image = ImageIO.read(in);
            for(int h = 0; h < image.getHeight() ; h++) {
                for(int w = 0; w < image.getWidth() ; w++) {
                    int color = image.getRGB(w, h);
                    int gray = ((color>>16 & 255) + (color>>8 & 255) + (color & 255))/3;
                    image.setRGB(w, h,(color & 0xff000000) | gray << 16 | gray << 8 | gray);
                }
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "png", os);
            rtrn = new ByteArrayInputStream(os.toByteArray());
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
