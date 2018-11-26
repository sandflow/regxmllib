/*
 * Copyright (c) 2018, pal
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

import junit.framework.TestCase;

public class UMIDTest extends TestCase {

    public UMIDTest(String testName) {
        super(testName);
    }

    public void testFromURN() {
        UMID umid1 = UMID.fromURN("urn:smpte:umid:060A2B34.01010105.01010D20.13000000.D2C9036C.8F195343.AB7014D2.D718BFDA");
        byte[] value = umid1.getValue();
        assertEquals(32, value.length);
        assertEquals((byte) 0x06, value[0]);
        assertEquals((byte) 0x0A, value[1]);
        assertEquals((byte) 0x2B, value[2]);
        assertEquals((byte) 0x34, value[3]);
        assertEquals((byte) 0x01, value[4]);
        assertEquals((byte) 0x01, value[5]);
        assertEquals((byte) 0x01, value[6]);
        assertEquals((byte) 0x05, value[7]);
        assertEquals((byte) 0x01, value[8]);
        assertEquals((byte) 0x01, value[9]);
        assertEquals((byte) 0x0D, value[10]);
        assertEquals((byte) 0x20, value[11]);
        assertEquals((byte) 0x13, value[12]);
        assertEquals((byte) 0x00, value[13]);
        assertEquals((byte) 0x00, value[14]);
        assertEquals((byte) 0x00, value[15]);
        assertEquals((byte) 0xD2, value[16]);
        assertEquals((byte) 0xC9, value[17]);
        assertEquals((byte) 0x03, value[18]);
        assertEquals((byte) 0x6C, value[19]);
        assertEquals((byte) 0x8F, value[20]);
        assertEquals((byte) 0x19, value[21]);
        assertEquals((byte) 0x53, value[22]);
        assertEquals((byte) 0x43, value[23]);
        assertEquals((byte) 0xAB, value[24]);
        assertEquals((byte) 0x70, value[25]);
        assertEquals((byte) 0x14, value[26]);
        assertEquals((byte) 0xD2, value[27]);
        assertEquals((byte) 0xD7, value[28]);
        assertEquals((byte) 0x18, value[29]);
        assertEquals((byte) 0xBF, value[30]);
        assertEquals((byte) 0xDA, value[31]);
    }

}
