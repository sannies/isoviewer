package com.coremedia.iso.gui;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.TrackBox;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.List;

/**
 *
 */
public class TrackListModel extends AbstractListModel {
    IsoFile isoFile;

    public TrackListModel(IsoFile isoFile) {
        this.isoFile = isoFile;
    }

    public int getSize() {
        List<MovieBox> movieBoxList = isoFile.getBoxes(MovieBox.class);
        if (movieBoxList.isEmpty()) {
            return 0;
        } else {
            return movieBoxList.get(0).getBoxes(TrackBox.class).size();
        }
    }

    public Object getElementAt(int index) {
        List<MovieBox> movieBoxList = isoFile.getBoxes(MovieBox.class);
        return movieBoxList.get(0).getBoxes(TrackBox.class).get(index);

    }

}
