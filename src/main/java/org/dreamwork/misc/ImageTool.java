package org.dreamwork.misc;

import org.apache.log4j.Logger;
import org.dreamwork.util.FileInfo;
import org.dreamwork.util.StringUtil;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * Created by seth.yang on 2017/4/17
 */
public class ImageTool {
    private static final Logger logger = Logger.getLogger (ImageTool.class);
    public static final Color TRANSPARENT = new Color (0xff, 0xff, 0xff, 0);

    public static Dimension getImageSize (Path image) throws IOException {
        long now = 0;
        if (logger.isDebugEnabled ()) {
            now = System.currentTimeMillis ();
        }
        File file = image.toFile ();
        String ext = FileInfo.getExtension (file.getCanonicalPath ());
        if (StringUtil.isEmpty (ext)) {
            throw new IOException ("unsupported file");
        }

        ext = ext.toLowerCase ().trim ();

        ImageReader reader;
        Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix (ext);
        if (iter.hasNext ()) {
            reader = iter.next ();
        } else {
            throw new IOException ("Unsupported image: " + image);
        }

        try (ImageInputStream iis = new FileImageInputStream (file)) {
            reader.setInput (iis);
            int index  = reader.getMinIndex ();
            int width  = reader.getWidth (index);
            int height = reader.getHeight (index);
            if (logger.isDebugEnabled ()) {
                long offset = System.currentTimeMillis () - now;
                System.out.println (image + " " + offset);
            }
            return new Dimension (width, height);
        }
    }

    public static void fitTo (BufferedImage image, OutputStream out, String format, int width, int height) throws IOException {
        int w = image.getWidth (), h = image.getHeight ();

        double scale_width = ((double) width) / w,
                scale_height = ((double) height) / h;
        double scale = Math.min (scale_width, scale_height);

        w = (int) (w * scale);
        h = (int) (h * scale);

        int type = image.getType ();
        BufferedImage target = new BufferedImage (w, h, type);
        Graphics2D g = (Graphics2D) target.getGraphics ();

        g.scale (scale, scale);

        g.drawImage (image, 0, 0, null);
        ImageIO.write (target, format, out);
    }
}