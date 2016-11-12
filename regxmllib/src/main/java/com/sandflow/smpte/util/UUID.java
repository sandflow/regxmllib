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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Represents a UUID as specified in FRC 4122
 */
public class UUID {

    private byte[] value;

    private UUID() {
    }

    /**
     * Instantiates a UUID from a sequence of 16 bytes
     *
     * @param uuid Sequence of 16 bytes
     */
    public UUID(byte[] uuid) {
        this.value = java.util.Arrays.copyOf(uuid, 16);
    }

    /**
     * Returns the sequence of bytes that make up the UUID (in the order specified by RFC 4122)
     * 
     * @return Sequence of 16 bytes
     */
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

    /**
     * Generate a Class 4 random UUID
     * @return Class 4 UUID
     */
    public static UUID fromRandom() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[16];
        random.nextBytes(bytes);

        bytes[6] = (byte) ((bytes[6] & 0x0f) | 0x4f);
        bytes[8] = (byte) ((bytes[8] & 0x3f) | 0x7f);

        return new UUID(bytes);
    }

    /**
     * Generate a Class 5 UUID from a URI
     * @param uri URI
     * @return Class 5 UUID
     */
    public static UUID fromURIName(URI uri) {
        MessageDigest digest;

        UUID nsid = UUID.fromURN("urn:uuid:6ba7b811-9dad-11d1-80b4-00c04fd430c8");

        try {
            digest = MessageDigest.getInstance("SHA-1");
            digest.update(nsid.getValue());
            digest.update(uri.toString().getBytes("ASCII"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }

        byte[] result = digest.digest();

        result[6] = (byte) ((result[6] & 0x0f) | 0x5f);
        result[8] = (byte) ((result[8] & 0x3f) | 0x7f);

        return new UUID(result);
    }

    /**
     * Creates a UUID from a URN
     * (urn:uuid:f81d4fae-7dec-11d0-a765-00a0c91e6bf6)
     *
     * @param val URN-representation of the UUID
     * @return UUID, or null if invalid URN
     */
    public static UUID fromURN(String val) {

        byte[] uuid = new byte[16];

        if (URN_PATTERN.matcher(val).matches()) {

            int inoff = 0;
            int outoff = 9;

            for (int j = 0; j < 4; j++) {

                uuid[inoff++] = (byte) Integer.parseInt(val.substring(outoff, outoff + 2), 16);

                outoff += 2;

            }

            for (int i = 0; i < 3; i++) {

                outoff++;

                for (int j = 0; j < 2; j++) {

                    uuid[inoff++] = (byte) Integer.parseInt(val.substring(outoff, outoff + 2), 16);
                    outoff += 2;
                }
            }

            outoff++;

            for (int j = 0; j < 6; j++) {

                uuid[inoff++] = (byte) Integer.parseInt(val.substring(outoff, outoff + 2), 16);
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
