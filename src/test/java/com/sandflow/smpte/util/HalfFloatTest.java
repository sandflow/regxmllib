/*
 * Copyright (c) 2015, Pierre-Anthony Lemieux (pal@sandflow.com)
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

/**
 * Half-float unit tests.
 */
public class HalfFloatTest extends TestCase {
    
    public HalfFloatTest(String testName) {
        super(testName);
    }
    
    /**
     * Test of toDouble method, of class HalfFloat.
     */
    public void testToDouble() {
        assertEquals(1.0d, HalfFloat.toDouble(0b0011110000000000));
        assertEquals(1.0009765625d, HalfFloat.toDouble(0b0011110000000001));
        assertEquals(-2d, HalfFloat.toDouble(0b1100000000000000));
        assertEquals(65504d, HalfFloat.toDouble(0b0111101111111111));
        assertEquals(Math.pow(2, -14), HalfFloat.toDouble(0b0000010000000000));
        assertEquals(Math.pow(2, -14) - Math.pow(2, -24), HalfFloat.toDouble(0b0000001111111111));
        assertEquals(Math.pow(2, -24), HalfFloat.toDouble(0b0000000000000001));
        assertEquals(0.0d, HalfFloat.toDouble(0b0000000000000000));
        assertEquals(-0.0d, HalfFloat.toDouble(0b1000000000000000));
        assertEquals(Double.POSITIVE_INFINITY, HalfFloat.toDouble(0b0111110000000000));
        assertEquals(Double.NEGATIVE_INFINITY, HalfFloat.toDouble(0b1111110000000000));
        assertEquals(0.333251953125d, HalfFloat.toDouble(0b0011010101010101)); 
    }
    
}
