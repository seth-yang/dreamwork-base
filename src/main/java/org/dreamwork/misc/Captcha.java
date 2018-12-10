package org.dreamwork.misc;

import javax.imageio.ImageIO;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 13-12-6
 * Time: 上午10:49
 */
public class Captcha {
    private int width = 80, height = 20, length = 4;
    private CaptchaType type = CaptchaType.Alpha_Chars;
    private char[] content;
    private boolean randomLines = true, rotateChars = true, scaleChars = true;

    private Random random = new Random (System.currentTimeMillis ());

    public Captcha () {}

    public Captcha (int width, int height, int length) {
        this (width, height, length, CaptchaType.Alpha_Chars);
    }

    public Captcha (int width, int height, int length, CaptchaType type) {
        this.setWidth (width);
        this.setHeight (height);
        this.setLength (length);
        this.setType (type);
    }

    public int getWidth () {
        return width;
    }

    public void setWidth (int width) {
        this.width = width;
    }

    public int getHeight () {
        return height;
    }

    public void setHeight (int height) {
        this.height = height;
    }

    public int getLength () {
        return length;
    }

    public void setLength (int length) {
        this.length = length;
    }

    public CaptchaType getType () {
        return type;
    }

    public void setType (CaptchaType type) {
        this.type = type;
        if (type == CaptchaType.Formula) {
            randomLines = rotateChars = scaleChars = false;
        }
    }

    public boolean isRandomLines () {
        return randomLines;
    }

    public void setRandomLines (boolean randomLines) {
        this.randomLines = randomLines;
    }

    public boolean isRotateChars () {
        return rotateChars;
    }

    public void setRotateChars (boolean rotateChars) {
        this.rotateChars = rotateChars;
    }

    public boolean isScaleChars () {
        return scaleChars;
    }

    public void setScaleChars (boolean scaleChars) {
        this.scaleChars = scaleChars;
    }

    public Color randomColor (int frontColor, int backgroundColor) {
        if (frontColor > 255) frontColor = 200;
        if (backgroundColor > 255) backgroundColor = 255;
        int r = random.nextInt (backgroundColor - frontColor),
            g = random.nextInt (backgroundColor - frontColor),
            b = random.nextInt (backgroundColor - frontColor);
        return new Color (r, g, b);
    }

    public String generateContent () {
        switch (type) {
            case Number_Only :
            case Alpha_Chars:
                content = new char[length];
                int wide = type == CaptchaType.Number_Only ? 1 : 2;
                for (int i = 0; i < length; i ++) {
                    content [i] = generate (wide);
                }
                break;
            case Formula:
                generateFormula ();
                break;
        }
        return new String (content);
    }

    public String getContent () {
        return content != null ? new String (content) : null;
    }

    public int getFormulaResult () {
        if (type != CaptchaType.Formula)
            throw new IllegalStateException ("Not formula captcha");

        if (content == null)
            throw new IllegalStateException ("Formula is not ready");

        try {
            String formula = new String (content);
            ScriptEngineManager manager = new ScriptEngineManager ();
            ScriptEngine engine = manager.getEngineByName ("javascript");
            Number number = (Number) engine.eval ("(" + formula + ")");
            return number.intValue ();
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
    }

    public void writeImage (OutputStream out) throws IOException {
        BufferedImage image = new BufferedImage (width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = image.createGraphics ();
        int fontSize = (int) (height * .9);
        Font font = new Font ("DialogInput", Font.BOLD, fontSize);
        g.setFont (font);
        FontMetrics fm = g.getFontMetrics ();
        int delta = fm.getAscent () / 2;
        Color bgColor = randomColor (0, 255);
        Color fgColor = new Color (255 - bgColor.getRed (), 255 - bgColor.getGreen (), 255 - bgColor.getBlue ());
        g.setColor (bgColor);
        g.fillRect (0, 0, width, height);
        if (isRandomLines ())
            randomLines (g);
        AffineTransform old = g.getTransform ();

        int gap = (width - fm.charsWidth (content, 0, content.length)) / (length + 2);

        int x = gap, y = height / 2;
        g.setColor (fgColor);
        for (int i = 0; i < length; i ++) {
            char c = content [i];
            AffineTransform transform = new AffineTransform ();
            transform.translate (x, y);
            if (rotateChars) {
                int sign = Math.random () < .5 ? -1 : 1;
                int degree = random.nextInt (30);
                transform.rotate (sign * degree * Math.PI / 180, 0, 0);
            }
            if (scaleChars) {
                float scale = Math.max (random.nextFloat (), .8f);
                transform.scale (scale, scale);
            }
            g.transform (transform);
            g.setXORMode (bgColor);
            g.drawString (String.valueOf (c), 0, delta);
            g.setTransform (old);

            x += fm.charWidth (c) + gap;
        }
        ImageIO.write (image, "jpg", out);
        g.dispose ();
    }

    private char generate (int wide) {
        int i = random.nextInt (wide);
        switch (i) {
            case 0 :
                return (char) (random.nextInt (10) + '0');
            default :
                return (char) (random.nextInt (26) + 'A');
        }
    }

    private void randomLines (Graphics2D g) {
        int count = 100 + random.nextInt (50);
        for (int i = 0; i < count; i ++) {
            g.setColor (randomColor (0, 255));
            int x1 = random.nextInt (width), y1 = random.nextInt (height);
            int x2 = x1 + random.nextInt (12), y2 = y1 + random.nextInt (12);
            g.drawLine (x1, y1, x2, y2);
        }
    }

    private void generateFormula () {
        int a = random.nextInt (19) + 1;
        int b = random.nextInt (19) + 1;
        char operator = Math.random () < .5 ? '-' : '+';
        int x = Math.max (a, b), y = Math.min (a, b);
        StringBuilder builder = new StringBuilder ();
        builder.append (x).append (operator).append (y);
        content = builder.toString ().toCharArray ();
        length = content.length;
    }
}