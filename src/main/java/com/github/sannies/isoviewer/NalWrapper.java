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

import java.nio.ByteBuffer;

/**
* Created by sannies on 27.10.2014.
*/
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
                return sb.toString();
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
