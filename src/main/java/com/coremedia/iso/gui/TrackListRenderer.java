package com.coremedia.iso.gui;

import com.coremedia.iso.boxes.TrackBox;

import javax.swing.*;
import java.awt.*;

/**
 *
 */
public class TrackListRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        TrackBox trackBox = ((TrackBox) value);
        value = "Track " + trackBox.getTrackHeaderBox().getTrackId();
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        return this;
    }
}
