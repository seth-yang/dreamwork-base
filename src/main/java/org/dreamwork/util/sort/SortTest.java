package org.dreamwork.util.sort;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created with IntelliJ IDEA.
 * User: seth
 * Date: 13-2-18
 * Time: 下午12:27
 */
public class SortTest {
    public static void main (String[] args) throws Exception {
        IElement[] elements = new IElement [50];
        Integer[] values = new Integer [50];
        for (int i = 0; i < 50; i ++) {
            int value = (int) (Math.random () * 500);
            values [i] = value;
            elements [i] = new IntegerElement (value);
        }

        DataPanel panel = new DataPanel (elements);
        JPanel p = new JPanel (new FlowLayout (FlowLayout.RIGHT));

        JFrame frame = new JFrame ();
        frame.setSize (300, 530);
        frame.setDefaultCloseOperation (WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane ().add (panel);
        frame.getContentPane ().add (p, BorderLayout.SOUTH);

        JButton button = new JButton ("Starting sort...");
        p.add (button);

        final ISorter<Integer> sorter = new QuickSort<Integer> ();
        sorter.setElements (values);
        sorter.setRunner (new Animation (panel));

        button.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed (ActionEvent e) {
                sorter.sort ();
            }
        });

        frame.setVisible (true);
    }

    private static class IntegerElement implements IElement {
        private int value;

        public IntegerElement (int value) {
            this.value = value;
        }

        @Override
        public int getValue () {
            return value;
        }

        @Override
        public int compareTo (IElement o) {
            return Integer.valueOf (value).compareTo (o.getValue ());
        }
    }
}