package com.github.sannies.isoviewer;

import javax.swing.*;
import java.awt.*;

public class IndexListRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        this.setText(String.format("%d: %s", index, this.getText()));
        return c;
    }
}