package com.coremedia.iso.gui;

import com.coremedia.iso.boxes.h264.AvcConfigurationBox;
import com.coremedia.iso.boxes.mdat.SampleList;
import com.coremedia.iso.boxes.sampleentry.AbstractSampleEntry;
import com.googlecode.mp4parser.authoring.Sample;

import javax.swing.AbstractListModel;
import java.nio.ByteBuffer;

/**
 *
 */
public class SampleListModel extends AbstractListModel {
    SampleList list;
    long trackId;
    AbstractSampleEntry se;
    private AvcConfigurationBox.AVCDecoderConfigurationRecord avcD;


    public SampleListModel(SampleList list, long trackId, AbstractSampleEntry se, AvcConfigurationBox.AVCDecoderConfigurationRecord avcD) {
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
        return new Entry(list.get(index), trackId, se, avcD);
    }

    public static class Entry {
        public Entry(Sample sample, long trackId, AbstractSampleEntry se, AvcConfigurationBox.AVCDecoderConfigurationRecord avcD) {
            this.sample = sample;
            this.trackId = trackId;
            this.se = se;
            this.avcD = avcD;
        }

        Sample sample;
        long trackId;
        AbstractSampleEntry se;
        AvcConfigurationBox.AVCDecoderConfigurationRecord avcD;

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("Entry");
            sb.append("{sample=").append(sample);
            sb.append('}');
            return sb.toString();
        }
    }

}