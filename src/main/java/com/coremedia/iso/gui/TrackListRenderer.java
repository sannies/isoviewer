package com.coremedia.iso.gui;

import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.fragment.TrackFragmentBox;

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
        final long trackId;
        if (value instanceof TrackBox) {
            TrackBox trackBox = ((TrackBox) value);
            trackId = trackBox.getTrackHeaderBox().getTrackId();
        } else {
            TrackFragmentBox traf = ((TrackFragmentBox) value);
            trackId = traf.getTrackFragmentHeaderBox().getTrackId();
        }
        value = "Track " + trackId;
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        return this;
    }
}
