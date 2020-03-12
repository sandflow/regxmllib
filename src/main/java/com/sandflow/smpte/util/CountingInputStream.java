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
package com.sandflow.smpte.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Counts the number of bytes read from an InputStream
 */
public class CountingInputStream extends InputStream {
    
    long count = 0;
    long markCount = 0;
    InputStream is;
    
    /**
     * Instantiates a CountingInputStream
     * @param is InputStream from which data will be read
     */
    public CountingInputStream(InputStream is) {
        this.is = is;
    }

    @Override
    public synchronized void mark(int i) {
        markCount = count;
        is.mark(i);
    }

    @Override
    public long skip(long l) throws IOException {
        long sb = is.skip(l);
        if (sb >= 0) count += sb;
        return sb;
    }

    @Override
    public int read(byte[] bytes, int i, int i1) throws IOException {
        int sb = is.read(bytes, i, i1);
        if (sb >= 0) count += sb;
        return sb;
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        int sb = is.read(bytes);
        if (sb >= 0) count += sb;
        return sb;
    }

    @Override
    public int read() throws IOException {
        int sb = is.read();
        if (sb >= 0) count += 1;
        return sb;
    }

    @Override
    public boolean markSupported() {
        return is.markSupported();
    }

    @Override
    public synchronized void reset() throws IOException {
        count = markCount;
        is.reset();
    }

    @Override
    public void close() throws IOException {
        is.close();
    }

    @Override
    public int available() throws IOException {
        return is.available();
    }
    
    /**
     * @return Returns the number of bytes read since the object was created or
     * resetCount was called
     */
    public long getCount() {
        return count;
    }
    
    /**
     * Resets the number of bytes read to zero.
     */
    public void resetCount() {
        count = 0;
    }
    
}
