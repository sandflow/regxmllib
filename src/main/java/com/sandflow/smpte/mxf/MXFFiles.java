/*
 * Copyright (c) 2016, pal
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.sandflow.smpte.mxf;

import com.sandflow.smpte.klv.KLVInputStream;
import com.sandflow.smpte.klv.Triplet;
import com.sandflow.smpte.klv.exceptions.KLVException;
import com.sandflow.smpte.util.UL;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;

/**
 * Utilities for processing MXF files
 * @author pal
 */
public class MXFFiles {

    /**
     * Seeks to the footer partition, assuming the current position of the
     * channel is within the run-in (SMPTE ST 377-1 Section 6.5), the footer partition
     * offset is listed in the Header Partition Pack or a Random Index Pack is
     * present.
     *
     * @param mxffile Channel containing an MXF file
     * @return Offset of the Footer Partition, or -1 if a Footer Partition was not found
     * @throws IOException
     * @throws com.sandflow.smpte.klv.exceptions.KLVException
     */
    public static long seekFooterPartition(SeekableByteChannel mxffile) throws IOException, KLVException {
        long headeroffset = seekHeaderPartition(mxffile);
        KLVInputStream kis = new KLVInputStream(Channels.newInputStream(mxffile));
        Triplet t = kis.readTriplet();
        if (t == null) {
            return -1;
        }
        PartitionPack pp = PartitionPack.fromTriplet(t);
        if (pp == null) {
            return -1;
        }
        if (pp.getFooterPartition() != 0) {
            mxffile.position(headeroffset + pp.getFooterPartition());
            return mxffile.position();
        }
        
        /* look for the RIP start */
        
        mxffile.position(mxffile.size() - 4);
        
        ByteBuffer bytes = ByteBuffer.allocate(4);
        
        if (mxffile.read(bytes) != bytes.limit()) {
            return -1;
        }
        
        /* move to start of RIP */
        
        mxffile.position(mxffile.size() - bytes.getInt(0));
        
        /* read RIP */
        
        kis = new KLVInputStream(Channels.newInputStream(mxffile));
        
        t = kis.readTriplet();
        
        if (t == null) {
            return -1;
        }
        RandomIndexPack rip = RandomIndexPack.fromTriplet(t);
        if (rip == null) {
            return -1;
        }
        mxffile.position(rip.getOffsets().get(rip.getOffsets().size() - 1).getOffset());
        return mxffile.position();
    }

    /**
     * Seeks to the first byte of the Header partition, assuming the current position of the
     * channel is within the run-in (SMPTE ST 377-1 Section 6.5)
     *
     * @param mxffile Channel containing an MXF file
     * @return Offset of the first byte of the Header Partition, or -1 if
     * the Header Partition was not found
     * @throws IOException
     */
    public static long seekHeaderPartition(SeekableByteChannel mxffile) throws IOException {
        ByteBuffer ulbytes = ByteBuffer.allocate(16);
        long offset = mxffile.position();
        while (mxffile.read(ulbytes) == ulbytes.limit() && offset <= 65536) {
            UL ul = new UL(ulbytes.array());
            if (ul.equalsWithMask(PartitionPack.getKey(), 65248 /* first eleven bytes minus the version byte */ )) {
                mxffile.position(offset);
                return offset;
            }
            mxffile.position(++offset);
        }
        return -1;
    }
    
}
