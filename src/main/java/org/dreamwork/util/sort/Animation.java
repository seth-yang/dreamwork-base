package org.dreamwork.util.sort;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: seth
 * Date: 2009-9-28
 * Time: 16:30:02
 */
public class Animation implements Runnable{
    private DataPanel panel;

    public Animation (DataPanel panel) {
        this.panel = panel;
    }

    public void run () {
        SwingUtilities.invokeLater (new Runnable() {
            public void run () {
                panel.repaint ();
            }
        });
    }

    public void setPovit (int povit) {
        panel.setPovit (povit);
    }

    public void setFrom (int from) {
        panel.setFrom (from);
    }
}