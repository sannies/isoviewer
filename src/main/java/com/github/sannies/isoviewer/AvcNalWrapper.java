/*
 * Copyright 2014 Sebastian Annies
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.sannies.isoviewer;

import com.googlecode.mp4parser.authoring.tracks.h264.H264NalUnitTypes;
import com.googlecode.mp4parser.h264.read.CAVLCReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

/**
 * Created by sannies on 27.10.2014.
 */
public class AvcNalWrapper {
    ByteBuffer data;
    private int nal_ref_idc;
    private int nal_unit_type;
    private int slice_type = -1;

    public AvcNalWrapper(ByteBuffer data) {
        this.data = data;
        int type = data.get();
        this.nal_ref_idc = (type >> 5) & 3;
        this.nal_unit_type = type & 0x1f;
        byte[] d = new byte[2];
        try {
            data.position(1);
            data.get(d);

            CAVLCReader reader = new CAVLCReader(new ByteArrayInputStream(d));
            int first_mb_in_slice = reader.readUE("SliceHeader: first_mb_in_slice");
            slice_type = reader.readUE("SliceHeader: slice_type");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (Field f : H264NalUnitTypes.class.getFields()) {
            try {
                if (nal_unit_type == f.getInt(null)) {
                    sb.append(f.getName() + "[" + nal_unit_type + "]");
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException();
            }
        }


        sb.append("{slice_type:").append("" + slice_type).append(",idc:").append(nal_ref_idc).append(",size:").append(data.limit());
        sb.append('}');
//            sb.append("{data=").append(data);
//            sb.append('}');
        return sb.toString();
    }
}
