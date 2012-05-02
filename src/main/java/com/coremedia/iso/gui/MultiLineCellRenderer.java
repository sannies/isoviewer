package com.coremedia.iso.gui;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;

public class MultiLineCellRenderer implements ListCellRenderer {

    private JPanel p;
    private JTextArea ta;

    public MultiLineCellRenderer() {
        p = new JPanel();
        p.setLayout(new BorderLayout());


        // text
        ta = new JTextArea();
        ta.setLineWrap(false);
        ta.setWrapStyleWord(true);
        p.add(ta, BorderLayout.CENTER);
    }


    public Component getListCellRendererComponent(final JList list,
                                                  final Object value, final int index, final boolean isSelected,
                                                  final boolean hasFocus) {

        ta.setText((String) value);
        int width = list.getWidth();
        // this is just to lure the ta's internal sizing mechanism into action
        if (width > 0)
            ta.setSize(width, Short.MAX_VALUE);
        return p;

    }
}