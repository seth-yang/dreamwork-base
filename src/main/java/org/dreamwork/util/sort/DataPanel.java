package org.dreamwork.util.sort;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: seth
 * Date: 2009-9-28
 * Time: 13:32:24
 */
public class DataPanel extends JComponent {
    private IElement[] data;
    private IElement max;
    private int povit, from;

    public int getPovit () {
        return povit;
    }

    public void setPovit (int povit) {
        this.povit = povit;
    }

    public int getFrom () {
        return from;
    }

    public void setFrom (int peer) {
        this.from = peer;
    }

    public DataPanel (IElement... data) {
        this.data = data;
        findMaxElement ();
    }

    private void findMaxElement () {
        if (data == null || data.length == 0) return;
        max = data[0];
        for (int i = 1; i < data.length; i ++) {
            if (max.compareTo (data [i]) < 0) max = data [i];
        }
    }

    private Stroke bold = new BasicStroke (2);
    private Stroke normal = new BasicStroke (1);

    @Override
    public void paint (Graphics g) {
        Dimension d = getSize ();
        Graphics2D g2 = (Graphics2D) g;
        double scale = d.getWidth () / max.getValue ();
        for (int i = 0; i < data.length; i ++) {
            IElement e = data [i];
            int x = (int) (scale * e.getValue ());
            int y = (i * 3) + 1;
            if (i == povit) {
                g2.setColor (Color.red);
                g2.setStroke (bold);
            } else if (i == from) {
                g2.setColor (Color.blue);
                g2.setStroke (bold);
            } else {
                g2.setColor (Color.black);
                g2.setStroke (normal);
            }
            g2.drawLine (0, y, x, y);
        }
    }
}