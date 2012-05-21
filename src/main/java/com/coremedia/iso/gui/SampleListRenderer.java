package com.coremedia.iso.gui;

import com.coremedia.iso.boxes.h264.AvcConfigurationBox;
import com.coremedia.iso.boxes.sampleentry.SampleEntry;
import com.coremedia.iso.boxes.sampleentry.VisualSampleEntry;

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
        final SampleEntry se = sampleListEntry.se;
        if (se != null && se instanceof VisualSampleEntry &&
                ("avc1".equals(se.getType()) || "mp4v".equals(se.getType()) || ("encv".equals(se.getType()) && ((VisualSampleEntry) se).getCompressorname().contains("AVC")))) {
            try {
                IsoSampleNALUnitReader isoSampleNALUnitReader = new IsoSampleNALUnitReader(sampleListEntry.sample,
                        se.getBoxes(AvcConfigurationBox.class).get(0).getLengthSizeMinusOne() + 1);
                ArrayList<NalWrapper> nals = new ArrayList<NalWrapper>();

                do {
                    ByteBuffer nal = isoSampleNALUnitReader.nextNALUnit();
                    if (nal == null) {
                        break;
                    }
                    nals.add(new NalWrapper(nal));
                } while (true);

                value = "Sample " + (index + 1) + "@" + sampleListEntry.offset + " - " + sampleListEntry.sample.limit() + "bytes " + nals;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        return this;
    }

    public class NalWrapper {
        ByteBuffer data;

        public NalWrapper(ByteBuffer data) {
            this.data = data;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("NalWrapper");
            sb.append("{").append(printNal(data));
            sb.append('}');
//            sb.append("{data=").append(data);
//            sb.append('}');
            return sb.toString();
        }
    }
    public String printNal(ByteBuffer nal) {
        int type = nal.get();
        int nal_ref_idc = (type >> 5) & 3;
        int nal_unit_type = type & 0x1f;
        return "Type: " + nal_unit_type + " ref_idc: " + nal_ref_idc + " (size " + nal.limit() + 1 + ")";

    }
}