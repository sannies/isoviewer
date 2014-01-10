package com.coremedia.iso.gui;

import com.coremedia.iso.boxes.h264.AvcConfigurationBox;
import com.coremedia.iso.boxes.sampleentry.AbstractSampleEntry;
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
    private long failedTrackId;
    private boolean bruteforceAvc = true;

    @Override
    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        SampleListModel.Entry sampleListEntry = (SampleListModel.Entry) value;

        value = "Sample " + (index + 1) + "@" + sampleListEntry + " - " + sampleListEntry.sample.getSize() + "bytes";
        final AbstractSampleEntry se = sampleListEntry.se;
        if (se != null && se instanceof VisualSampleEntry &&
                !se.getBoxes(AvcConfigurationBox.class).isEmpty()) {
            try {
                final int nalLengthSize = se.getBoxes(AvcConfigurationBox.class).get(0).getLengthSizeMinusOne() + 1;
                ArrayList<NalWrapper> nals = getNals(sampleListEntry, nalLengthSize);

                value = value + " " + nals;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (sampleListEntry.avcD != null) {
            try {
                final int nalLengthSize = sampleListEntry.avcD.lengthSizeMinusOne + 1;
                ArrayList<NalWrapper> nals = getNals(sampleListEntry, nalLengthSize);

                value = value + " " + nals;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (bruteforceAvc && se == null && sampleListEntry.trackId != failedTrackId) {
            //try default nal length of 4
            try {
                System.out.println("No AVC SampleEntry found, trying to parse sample as AVC with default NAL length of 4bytes.");
                ArrayList<NalWrapper> nals = getNals(sampleListEntry, 4);

                value = value + " " + nals;
            } catch (Exception e) {
                failedTrackId = sampleListEntry.trackId;
                System.err.println("Didn't work. Won't try again with trackId " + failedTrackId);
                //e.printStackTrace();
            }
        }

        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        return this;
    }

    private ArrayList<NalWrapper> getNals(SampleListModel.Entry sampleListEntry, int nalLengthSize) throws IOException {
        IsoSampleNALUnitReader isoSampleNALUnitReader = new IsoSampleNALUnitReader(sampleListEntry.sample, nalLengthSize);
        ArrayList<NalWrapper> nals = new ArrayList<NalWrapper>();

        do {
            ByteBuffer nal = isoSampleNALUnitReader.nextNALUnit();
            if (nal == null) {
                break;
            }
            nals.add(new NalWrapper(nal));
        } while (true);

        return nals;
    }

    public class NalWrapper {
        ByteBuffer data;
        private int nal_ref_idc;
        private int nal_unit_type;

        public NalWrapper(ByteBuffer data) {
            this.data = data;
            int type = data.get();
            this.nal_ref_idc = (type >> 5) & 3;
            this.nal_unit_type = type & 0x1f;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            switch (nal_unit_type) {
                case 1:
                    sb.append("NonIDR");
                    break;
                case 2:
                    sb.append("Part.A");
                    break;
                case 3:
                    sb.append("Part.B");
                    break;
                case 4:
                    sb.append("Part.C");
                    break;
                case 5:
                    sb.append("IDR");
                    break;
                case 6:
                    sb.append("SEI");
                    break;
                case 7:
                    sb.append("SPS");
                    break;
                case 8:
                    sb.append("PPS");
                    break;
                case 9:
                    sb.append("AUD");
                    break;
                case 10:
                    sb.append("EndOfSeq");
                    break;
                case 11:
                    sb.append("EndOfStr");
                    break;
            }
            sb.append("{").append("type:").append(nal_unit_type).append(",idc:").append(nal_ref_idc).append(",size:").append(data.limit());
            sb.append('}');
//            sb.append("{data=").append(data);
//            sb.append('}');
            return sb.toString();
        }
    }
}