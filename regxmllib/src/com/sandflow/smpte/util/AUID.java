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

/**
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
public class AUID {
    
    private AUID() { }

    public static AUID fromURN(String val) {
        if (val != null && val.startsWith("urn:smpte:ul:")) {
            return new AUID(UL.fromURN(val));
        }

        // TODO: handle UUID and errors
        return null;
    }

    private byte[] value;
    
    public AUID(byte[] auid) {
        this.value = java.util.Arrays.copyOf(auid, 16);
    }

    public AUID(UL ul) {
        this.value = ul.getValue();
    }

    public AUID(UUID uuid) {
        System.arraycopy(uuid.getValue(), 8, value, 0, 8);
        System.arraycopy(uuid.getValue(), 0, value, 8, 8);
    }

    @Override
    public boolean equals(Object auid) {
        if (!(auid instanceof AUID)) {
            return false;
        }
        return Arrays.equals(((AUID) auid).value, this.value);
    }

    public boolean equals(UL ul) {
        return Arrays.equals(ul.getValue(), this.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }

    
    @Override
    public String toString() {
        
        if (isUL()) {

            return asUL().toString();
        } else {
            UUID uuid = new UUID(value);
            
            return uuid.toString();
        }
    }

    public boolean isUL() {
        return (value[0] & 8) == 0;
    }

    public UL asUL() {
        return isUL() ? new UL(value) : null;
    }

}
