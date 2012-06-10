package com.coremedia.iso.gui;

import com.coremedia.iso.boxes.h264.AvcConfigurationBox;
import com.coremedia.iso.boxes.mdat.SampleList;
import com.coremedia.iso.boxes.sampleentry.SampleEntry;

import javax.swing.AbstractListModel;
import java.nio.ByteBuffer;

/**
 *
 */
public class SampleListModel extends AbstractListModel {
    SampleList list;
    long trackId;
    SampleEntry se;
    private AvcConfigurationBox.AVCDecoderConfigurationRecord avcD;


    public SampleListModel(SampleList list, long trackId, SampleEntry se, AvcConfigurationBox.AVCDecoderConfigurationRecord avcD) {
        this.list = list;
        this.trackId = trackId;
        this.se = se;
        this.avcD = avcD;
    }

    public long getTrackId() {
        return trackId;
    }

    public int getSize() {
        return list.size();
    }

    public Object getElementAt(int index) {
        ByteBuffer bb  = list.get(index);
        long offset = list.getOffsetKeys()[index];
        return new Entry(bb, offset, trackId, se, avcD);
    }

    public static class Entry {
        public Entry(ByteBuffer sample, long offset, long trackId, SampleEntry se, AvcConfigurationBox.AVCDecoderConfigurationRecord avcD) {
            this.sample = sample;
            this.offset = offset;
            this.trackId = trackId;
            this.se = se;
            this.avcD = avcD;
        }

        ByteBuffer sample;
        long offset;
        long trackId;
        SampleEntry se;
        AvcConfigurationBox.AVCDecoderConfigurationRecord avcD;
    }
}