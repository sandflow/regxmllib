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

import com.sandflow.smpte.util.AUID;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.InvalidParameterException;

/**
 * In-memory KLV Triplet
 */
public class MemoryTriplet implements Triplet {

    final private AUID key;
    final private byte[] value;

    /**
     * Creates a Triplet from a Key and an array of bytes as the Value
     * @param key Triplet Key
     * @param value Triplet Value
     */
    public MemoryTriplet(AUID key, byte[] value) {
        
        if (key == null || value == null) throw new InvalidParameterException("Triplet muse have key and value.");
        
        this.key = key;
        this.value = value;
    }

    @Override
    public AUID getKey() {
        return key;
    }

    @Override
    public long getLength() {
        return value.length;
    }

    @Override
    public byte[] getValue() {
        return value;
    }

    @Override
    public InputStream getValueAsStream() {
        return new ByteArrayInputStream(value);
    }
    
}
