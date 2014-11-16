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
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
public class UUID {

    private byte[] value;

    public UUID(byte[] uuid) {
        this.value = java.util.Arrays.copyOf(uuid, 16);
    }

    public byte[] getValue() {
        return this.value;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Arrays.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UUID other = (UUID) obj;
        if (!Arrays.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

    private final static Pattern URN_PATTERN = Pattern.compile("urn:uuid:[a-fA-F0-9]{8}-(?:[a-fA-F0-9]{4}-){3}[a-fA-F0-9]{12}");

    public static UUID fromURN(String val) {

        /* TODO: should this throw an exception */
        byte[] uuid = new byte[16];

        if (URN_PATTERN.matcher(val).matches()) {

            
        int inoff = 0;
        int outoff = 9;

        for (int j = 0; j < 4; j++) {

            uuid[inoff++] = (byte) Integer.parseUnsignedInt(val.substring(outoff, outoff + 2), 16);
            
            outoff += 2;
            
        }

        for (int i = 0; i < 3; i++) {

            outoff++;

            for (int j = 0; j < 2; j++) {

                uuid[inoff++] = (byte) Integer.parseUnsignedInt(val.substring(outoff, outoff + 2), 16);
                outoff += 2;
            }
        }

        outoff++;

        for (int j = 0; j < 6; j++) {

                uuid[inoff++] = (byte) Integer.parseUnsignedInt(val.substring(outoff, outoff + 2), 16);
                outoff += 2;
        }
            
            

            return new UUID(uuid);

        } else {

            return null;

        }

    }

    final static char[] HEXMAP = "0123456789abcdef".toCharArray();
    final static char[] URNTEMPLATE = "urn:uuid:3e0993c0-66e0-11e4-9803-0800200c9a66".toCharArray();

    @Override
    public String toString() {

        char[] out = Arrays.copyOf(URNTEMPLATE, URNTEMPLATE.length);

        int inoff = 0;
        int outoff = 9;

        for (int j = 0; j < 4; j++) {

            int v = value[inoff++] & 0xFF;
            out[outoff++] = HEXMAP[v >>> 4];
            out[outoff++] = HEXMAP[v & 0x0F];

        }

        for (int i = 0; i < 3; i++) {

            outoff++;

            for (int j = 0; j < 2; j++) {

                int v = value[inoff++] & 0xFF;
                out[outoff++] = HEXMAP[v >>> 4];
                out[outoff++] = HEXMAP[v & 0x0F];

            }
        }

        outoff++;

        for (int j = 0; j < 6; j++) {

            int v = value[inoff++] & 0xFF;
            out[outoff++] = HEXMAP[v >>> 4];
            out[outoff++] = HEXMAP[v & 0x0F];

        }

        return new String(out);
    }

}
