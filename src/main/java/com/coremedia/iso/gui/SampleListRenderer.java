package com.coremedia.iso.gui;

import com.coremedia.iso.boxes.h264.AvcConfigurationBox;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import java.awt.Component;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Knows how to display certain types of samples.
 */
public class SampleListRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        SampleListModel.Entry sampleListEntry = (SampleListModel.Entry) value;

        value = "Sample " + (index + 1) + "@" + sampleListEntry.offset + " - " + sampleListEntry.sample.limit() + "bytes";
        if (sampleListEntry.se != null &&
                ("avc1".equals(sampleListEntry.se.getType()) || "mp4v".equals(sampleListEntry.se.getType()))) {
            try {
                IsoSampleNALUnitReader isoSampleNALUnitReader = new IsoSampleNALUnitReader(sampleListEntry.sample,
                        sampleListEntry.se.getBoxes(AvcConfigurationBox.class).get(0).getLengthSizeMinusOne() + 1);
                ArrayList<ByteBuffer> nals = new ArrayList<ByteBuffer>();

                do {
                    ByteBuffer nal = isoSampleNALUnitReader.nextNALUnit();
                    if (nal == null) {
                        break;
                    }
                    nals.add(nal);
                } while (true);

                value = "Sample " + (index + 1) + "@" + sampleListEntry.offset + " - " + sampleListEntry.sample.limit() + "bytes " + nals;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        return this;
    }
}