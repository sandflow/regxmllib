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

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Represents a SMPTE UMID as specified in SMPTE ST 330
 */
public class UMID {

    private final static Pattern URN_PATTERN = Pattern.compile("urn:smpte:umid:[a-fA-F0-9]{8}(?:\\.[a-fA-F0-9]{8}){7}");

    /**
     * Creates a UMID from a URN
     * (urn:smpte:umid:060A2B34.01010105.01010D20.13000000.D2C9036C.8F195343.AB7014D2.D718BFDA)
     *
     * @param urn URN-representation of the UMID
     * @return UMID, or null if invalide URN
     */
    public static UMID fromURN(String urn) {

        byte[] umid = new byte[32];

        if (URN_PATTERN.matcher(urn).matches()) {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 4; j++) {
                    umid[4 * i + j] = (byte) Integer.parseInt(urn.substring(15 + i * 9 + 2 * j, 15 + i * 9 + 2 * j + 2), 16);
                }
            }

            return new UMID(umid);

        } else {

            return null;

        }

    }

    private byte[] value;

    private UMID() {
        this.value = new byte[32];
    }

    /**
     * Instantiates a UMID from a sequence of 32 bytes
     *
     * @param umid Sequence of 32 bytes
     */
    public UMID(byte[] umid) {
        this.value = java.util.Arrays.copyOf(umid, 32);
    }

    /**
     * Returns the sequence of bytes that make up the UMID (as specified in SMPTE ST 330)
     * 
     * @return sequence of 32 bytes
     */
    public byte[] getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UMID other = (UMID) obj;
        if (!Arrays.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

    final static char[] HEXMAP = "0123456789abcdef".toCharArray();
    final static char[] URNTEMPLATE = "urn:smpte:umid:060A2B34.01010105.01010D20.13000000.D2C9036C.8F195343.AB7014D2.D718BFDA".toCharArray();

    @Override
    public String toString() {

        char[] out = Arrays.copyOf(URNTEMPLATE, URNTEMPLATE.length);

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 4; j++) {

                int v = value[4 * i + j] & 0xFF;
                out[15 + 9 * i + 2 * j] = HEXMAP[v >>> 4];
                out[15 + 9 * i + 2 * j + 1] = HEXMAP[v & 0x0F];

            }
        }

        return new String(out);
    }

}
