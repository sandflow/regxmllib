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
 * Represent a SMPTE Universal Label (SMPTE ST 298)
 */
public class UL {

    private final static Pattern URN_PATTERN = Pattern.compile("urn:smpte:ul:[a-fA-F0-9]{8}\\.[a-fA-F0-9]{8}\\.[a-fA-F0-9]{8}\\.[a-fA-F0-9]{8}");
    private final static Pattern DOTVALUE_PATTERN = Pattern.compile("[a-fA-F0-9]{2}(\\.[a-fA-F0-9]{2}){15}");
    private final static int CATEGORY_DESIGNATOR_BYTE = 4;
    private final static int REGISTRY_DESIGNATOR_BYTE = 5;

    /**
     * Creates a UL from a URN
     * (urn:smpte:ul:xxxxxxxx.xxxxxxxx.xxxxxxxx.xxxxxxxx)
     *
     * @param urn URN from which the UL will be created
     * @return UL, or null if the URN is invalid
     */
    public static UL fromURN(String urn) {

        byte[] ul = new byte[16];

        if (URN_PATTERN.matcher(urn).matches()) {
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    ul[4 * i + j] = (byte) Integer.parseInt(urn.substring(13 + i * 9 + 2 * j, 13 + i * 9 + 2 * j + 2), 16);
                }
            }

            return new UL(ul);

        } else {

            return null;

        }

    }

    /**
     * Creates a UL from a dot-value representation
     * (xx.xx.xx.xx.xx.xx.xx.xx.xx.xx.xx.xx.xx.xx.xx.xx)
     *
     * @param val String from which the UL will be created
     * @return UL UL, or null if the URN is invalid
     */
    public static UL fromDotValue(String val) {

        byte[] ul = new byte[16];

        if (DOTVALUE_PATTERN.matcher(val).matches()) {
            for (int i = 0; i < 16; i++) {
                ul[i] = (byte) Integer.parseInt(val.substring(3 * i, 3 * i + 2), 16);
            }

            return new UL(ul);

        } else {

            return null;

        }

    }

    private final byte[] value;

    /**
     * @return true if the UL is a Key for a KLV Group (see SMPTE ST 336)
     */
    public boolean isGroup() {
        return getValueOctet(CATEGORY_DESIGNATOR_BYTE) == 2;
    }

    /**
     * @return true if the UL is a Key for a KLV Local Set (see SMPTE ST 336)
     */
    public boolean isLocalSet() {
        return isGroup() && (getRegistryDesignator() & 7) == 3;
    }

    /**
     * @return The value of the Registry Designator byte of the UL
     */
    public int getRegistryDesignator() {
        return getValueOctet(REGISTRY_DESIGNATOR_BYTE);
    }

    private UL() {
        this.value = new byte[16];
    }

    /**
     * Instantiates a UL from a sequence of 16 bytes
     *
     * @param ul Sequence of 16 bytes
     */
    public UL(byte[] ul) {
        this.value = java.util.Arrays.copyOf(ul, 16);
    }

    /**
     * Compares this UL to another UL, ignoring the version byte
     *
     * @param ul Other UL to compare
     * @return true if the ULs are equal
     */
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
    
        /**
     * Compares this UL to another AUID, ignoring the version byte
     *
     * @param auid Other AUID to compare
     * @return true if the UL is equal to the AUID, ignoring the version byte
     */
    public boolean equalsIgnoreVersion(AUID auid) {
        return (auid.isUL() && this.equalsIgnoreVersion(auid.asUL()));
    }

    /**
     * @return The value of the Version byte of the UL
     */
    public byte getVersion() {
        return getValueOctet(7);
    }

    /**
     * Compares this UL to another UL, ignoring specific bytes based on a mask
     *
     * @param ul Other UL to compare
     * @param bytemask 16-bit mask, where byte[n] is ignored if bit[n] is 0,
     * with n = 0 is the LSB
     * @return true if the ULs are equal
     */
    public boolean equalsWithMask(UL ul, int bytemask) {

        for (int i = 0; i < 15; i++) {
            if ((bytemask & 0x8000) != 0 && this.value[i] != ul.value[i]) {
                return false;
            }

            bytemask = bytemask << 1;
        }

        return true;
    }
    
   /**
     * Compares this UL to another AUID, ignoring specific bytes based on a mask
     *
     * @param auid Other UL to compare
     * @param bytemask 16-bit mask, where byte[n] is ignored if bit[n] is 0,
     * with n = 0 is the LSB
     * @return true if the UL and the AUID are equal, ignoring specific bytes based on bytemask
     */
    public boolean equalsWithMask(AUID auid, int bytemask) {
        return auid.isUL() && this.equalsWithMask(auid.asUL(), bytemask);
    }

    /**
     * Compares this UL to another UL
     *
     * @param ul Other UL to compare
     * @return true if the ULs are equal
     */
    public boolean equals(UL ul) {
        return Arrays.equals(ul.value, this.value);
    }
    
    /**
     * Compares this UL to a AUID
     *
     * @param auid AUID to compare
     * @return true if the UL and AUID are equal
     */
    public boolean equals(AUID auid) {
        return auid.equals(this);
    }
    
    /**
     * Returns the sequence of bytes that make up the UL (in the order specified by ST 298 4122)
     * 
     * @return Sequence of 16 bytes
     */
    public byte[] getValue() {
        return value;
    }

    /**
     * Returns the nth octet of the UL, indexed at 0
     *
     * @param i Index of the octet, starting at 0 for the first byte
     * @return Value of the byte
     */
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
    
    /**
     * @return true if the UL is a class 14 UL
     */
    public boolean isClass14() {
        return getValueOctet(8) == 14;
    }

    /**
     * @return true if the UL is a class 15 UL
     */
    public boolean isClass15() {
        return getValueOctet(8) == 15;
    }

    /**
     * @return true if the UL is a class 13 UL
     */
    public boolean isClass13() {
        return getValueOctet(8) == 13;
    }

}
