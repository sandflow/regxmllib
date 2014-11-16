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

public class UL {

    private final static Pattern URN_PATTERN = Pattern.compile("urn:smpte:ul:[a-fA-F0-9]{8}\\.[a-fA-F0-9]{8}\\.[a-fA-F0-9]{8}\\.[a-fA-F0-9]{8}");
    private final static Pattern DOTVALUE_PATTERN = Pattern.compile("[a-fA-F0-9]{2}(\\.[a-fA-F0-9]{2}){15}");
    private final static int CATEGORY_DESIGNATOR_BYTE = 4;
    private final static int REGISTRY_DESIGNATOR_BYTE = 5;

    public static UL fromURN(String val) {
        
        /* TODO: should this throw an exception */

        byte[] ul = new byte[16];

        if (URN_PATTERN.matcher(val).matches()) {
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    ul[4 * i + j] = (byte) Integer.parseUnsignedInt(val.substring(13 + i * 9 + 2 * j, 13 + i * 9 + 2 * j + 2), 16);
                }
            }

            return new UL(ul);

        } else {

            return null;

        }

    }

    public static UL fromDotValue(String val) {
        
        /* TODO: should this throw an exception */

        byte[] ul = new byte[16];

        if (DOTVALUE_PATTERN.matcher(val).matches()) {
            for (int i = 0; i < 16; i++) {
                ul[i] = (byte) Integer.parseUnsignedInt(val.substring(3 * i, 3 * i + 2), 16);
            }

            return new UL(ul);

        } else {

            return null;

        }

    }

    private byte[] value;

    public boolean isGroup() {
        return getValueOctet(CATEGORY_DESIGNATOR_BYTE) == 2;
    }

    public boolean isLocalSet() {
        /* TODO: compare also SMPTE designator */

        return getValueOctet(CATEGORY_DESIGNATOR_BYTE) == 2 && (getValueOctet(REGISTRY_DESIGNATOR_BYTE) & 7) == 3;
    }

    public int getRegistryDesignator() {
        return getValueOctet(REGISTRY_DESIGNATOR_BYTE);
    }

    private UL() {
        this.value = new byte[16];
    }

    public UL(byte[] ul) {
        this.value = java.util.Arrays.copyOf(ul, 16);
    }

    public boolean equalsIgnoreVersion(UL ul) {
        for (int i = 0; i < 7; i++) {
            if (this.value[i] != ul.value[i]) {
                return false;
            }
        }

        for (int i = 8; i < 16; i++) {
            if (this.value[i] != ul.value[i]) {
                return false;
            }
        }

        return true;
    }

    public boolean equals(UL ul, int bytemask) {

        for (int i = 0; i < 15; i++) {
            if ((bytemask & 0x8000) != 0 && this.value[i] != ul.value[i]) {
                return false;
            }

            bytemask = bytemask << 1;
        }

        return true;
    }

    public boolean equals(UL ul) {
        return Arrays.equals(ul.value, this.value);
    }

    public byte[] getValue() {
        return value;
    }

    public byte getValueOctet(int i) {
        return value[i];
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
        final UL other = (UL) obj;
        if (!Arrays.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }
    
    

    @Override
    public String toString() {

        return String.format("urn:smpte:ul:%02x%02x%02x%02x.%02x%02x%02x%02x.%02x%02x%02x%02x.%02x%02x%02x%02x",
                value[0],
                value[1],
                value[2],
                value[3],
                value[4],
                value[5],
                value[6],
                value[7],
                value[8],
                value[9],
                value[10],
                value[11],
                value[12],
                value[13],
                value[14],
                value[15]
        );
    }

    public boolean isClass14() {
        return getValueOctet(8) == 14;
    }
    
    public boolean isClass15() {
        return getValueOctet(8) == 15;
    }
    
        public boolean isClass13() {
        return getValueOctet(8) == 13;
    }

}
