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

/**
 * Utility functions for converting IEEE half-floats
 */
public class HalfFloat {

    /**
     * Converts a half-float stored in an int into a double
     * 
     * @param hf half-float stored in an int
     * @return Half-float value as a double
     */
    public static double toDouble(int hf) {
        double val;

        double sign = (hf & 0x8000) >> 15 == 0 ? 1 : -1;
        int exponent = (hf & 0x7c00) >> 10;
        int mantissa = hf & 0x03ff;

        if (exponent == 31) {
            if (mantissa == 0) {
                val = sign > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
            } else {
                val = Double.NaN;
            }
        } else if (exponent == 0) {
            val = sign * Math.pow(2, -24) * mantissa;
        } else {
            val = sign * Math.pow(2, exponent - 15) * (1024 + mantissa) * Math.pow(2, -10);
        }
        
        return val;
    }
}
