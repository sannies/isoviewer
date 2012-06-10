package com.coremedia.iso.gui;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.fragment.TrackFragmentBox;
import com.coremedia.iso.boxes.fragment.TrackFragmentHeaderBox;

import javax.swing.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class TrackListModel extends AbstractListModel {
    IsoFile isoFile;
    MovieBox movieBox;
    List<TrackFragmentBox> trackFragmentBoxList;
    Set<Long> trackIds = new HashSet<Long>();

    public TrackListModel(IsoFile isoFile) {
        this.isoFile = isoFile;
        final List<MovieBox> movieBoxList = isoFile.getBoxes(MovieBox.class);
        if (movieBoxList.isEmpty()) {
            trackFragmentBoxList = isoFile.getBoxes(TrackFragmentBox.class, true);
            if (trackFragmentBoxList != null) {
                for (TrackFragmentBox trackFragmentBox : trackFragmentBoxList) {
                    trackIds.add(trackFragmentBox.getTrackFragmentHeaderBox().getTrackId());
                }
            }
        } else {
            movieBox = movieBoxList.get(0);
            final long[] trackNumbers = movieBox.getTrackNumbers();
            for (long trackNumber : trackNumbers) {
                trackIds.add(trackNumber);
            }
        }
    }

    public int getSize() {
        if (movieBox == null) {
            return trackIds.size();
        } else {
            return movieBox.getBoxes(TrackBox.class).size();
        }
    }

    public Object getElementAt(int index) {
        if (movieBox != null) {
            return movieBox.getBoxes(TrackBox.class).get(index);
        } else {
            for (TrackFragmentBox trackFragmentBox : trackFragmentBoxList) {
                //todo we always return the first one - that's not perfect
                final TrackFragmentHeaderBox trackFragmentHeaderBox = trackFragmentBox.getTrackFragmentHeaderBox();
                if (trackFragmentHeaderBox.getTrackId() == trackIds.toArray(new Long[trackIds.size()])[index]) {
                    return trackFragmentBox;
                }
            }
        }
        return null;
    }

}
