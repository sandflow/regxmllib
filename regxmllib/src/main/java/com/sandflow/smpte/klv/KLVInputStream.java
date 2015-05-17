/*
 * Copyright (c) 2014, Pierre-Anthony Lemieux (pal@sandflow.com)
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
package com.sandflow.smpte.klv;

import com.sandflow.smpte.klv.exceptions.KLVException;
import static com.sandflow.smpte.klv.exceptions.KLVException.MAX_LENGTH_EXCEEED;
import com.sandflow.smpte.util.UL;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * KLVInputStream allows KLV data structures to be read from an InputStream
 */
public class KLVInputStream extends DataInputStream {

    /**
     * @param is InputStream to read from
     */
    public KLVInputStream(InputStream is) {
        super(is);
    }

    /**
     * Reads a single UL.
     * 
     * @return UL
     * @throws IOException
     * @throws EOFException 
     */
    public UL readUL() throws IOException, EOFException {
        byte[] ul = new byte[16];

        if (read(ul) < ul.length) {
            throw new EOFException();
        }

        return new UL(ul);
    }

    /**
     * Reads a single BER-encoded length. The maximum length of the encoded length is 8 bytes.
     * 
     * @return Length
     * @throws EOFException
     * @throws IOException
     * @throws KLVException 
     */
    public long readBERLength() throws EOFException, IOException, KLVException {

        long val = 0;

        int b = read();

        if (b <= 0) {
            throw new EOFException();
        }
        
        if ((b & 0x80) == 0) {
           return b;
        }

        int bersz =  (b & 0x0f);

        if (bersz > 8) {
            throw new KLVException(MAX_LENGTH_EXCEEED);
        }

        byte[] octets = new byte[bersz];

        if (read(octets) < bersz) {
            throw new EOFException();
        }

        for (int i = 0; i < bersz; i++) {
            int tmp = (((int) octets[i]) & 0xFF);
                val = (val << 8) + tmp;
                
                if (val > Integer.MAX_VALUE) {
                    throw new KLVException(MAX_LENGTH_EXCEEED);
                }
        }

        return val;
    }

    /**
     * Reads a single KLV triplet.
     * 
     * @return KLV Triplet
     * @throws IOException
     * @throws EOFException
     * @throws KLVException 
     */
    public Triplet readTriplet() throws IOException, EOFException, KLVException {
        UL ul = readUL();

        long len = readBERLength();

        if (len > Integer.MAX_VALUE) {
            throw new KLVException(MAX_LENGTH_EXCEEED);
        }

        byte[] value = new byte[(int) len];

        if (len != read(value)) {
            throw new EOFException("EOF reached while reading Value.");
        }

        return new MemoryTriplet(ul, value);
    }

    public long readUnsignedInt() throws IOException, EOFException {
        return ((long) readInt()) & 0xFFFF;
    }
}