/*
Copyright (c) 2011 Stanislav Vitvitskiy

Permission is hereby granted, free of charge, to any person obtaining a copy of this
software and associated documentation files (the "Software"), to deal in the Software
without restriction, including without limitation the rights to use, copy, modify,
merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.coremedia.iso.gui;


import com.coremedia.iso.IsoTypeReader;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

/**
 * Input stream that automatically unwraps NAL units and allows for NAL unit
 * navigation
 *
 * @author Stanislav Vitvitskiy
 */
public class IsoSampleNALUnitReader {
    private final ByteBuffer src;
    private int nalLengthSize = 4;

    public IsoSampleNALUnitReader(ByteBuffer src, int nalLengthSize) throws IOException {
        this.src = src;
        this.nalLengthSize = nalLengthSize;
    }

    public ByteBuffer nextNALUnit() throws IOException {
        if (src.remaining() < 5) {
            return null;
        }

        long nalLength;
        if (src.remaining() >= nalLengthSize) {

            if (nalLengthSize == 1) {
                nalLength = IsoTypeReader.readUInt8(src);
            } else if (nalLengthSize == 2) {
                nalLength = IsoTypeReader.readUInt16(src);
            } else if (nalLengthSize == 3) {
                nalLength = IsoTypeReader.readUInt24(src);
            } else if (nalLengthSize == 4) {
                nalLength = IsoTypeReader.readUInt32(src);
            } else {
                throw new IOException("Unknown NAL Length isze ");
            }

            if (nalLength == 0) {
                return null;
            }
            ByteBuffer nal = src.slice();
            nal.limit(l2i(nalLength));
            src.position(src.position() + l2i(nalLength));
            return nal;
        } else {
            throw new RuntimeException("remaining bytes less than nalLengthSize found in sample. should not be here.");
        }
    }
}