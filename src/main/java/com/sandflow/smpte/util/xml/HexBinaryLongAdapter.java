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
package com.sandflow.smpte.util.xml;

import java.util.Arrays;
import jakarta.xml.bind.DatatypeConverter;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Unmarshals/marshals a hexadecimal integer from/to an xsd:string value
 */
public class HexBinaryLongAdapter extends XmlAdapter<String, Long> {

    @Override
    public Long unmarshal(String s) throws Exception {
        
        byte[] val = DatatypeConverter.parseHexBinary(s);
        
        if (val.length > 4) throw new Exception();
        
        long out = 0;
        
        for(int i = 0; i < val.length; i++) {
            out = out << 8 | (val[i] & 0xFF);
        } 
        
        return out;
    }

    @Override
    public String marshal(Long val) throws Exception {
        byte[] out = new byte[8];
        
        long l = val;

        for(int i = out.length - 1; i >= 0; i--) {
            out[i] = (byte) (l & 0xFF);
            l >>= 8;
        }
        
        /* look for the first non-zero value */
        
        int first = 0;
        
        for(; first < out.length - 1; first++) {
            if (out[first] != 0) break; 
        }
        
        return DatatypeConverter.printHexBinary(
                Arrays.copyOfRange(out, first, out.length)
        );
    }
}
