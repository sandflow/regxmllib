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
 * IDAU as specified in SMPTE ST 377-1
 */
public class IDAU {
    
    /**
     * Creates a AUID from a UL or UUID URN. 
     * @param urn URN from which to create the AUID
     * @return AUID, or null if invalid URN
     */
    public static IDAU fromURN(String urn) {
        
        if (urn == null) return null;
        
        if (urn.startsWith("urn:smpte:ul:")) {
            
            return new IDAU(UL.fromURN(urn));
            
        } else if (urn.startsWith("urn:uuid:")) {
            
            return new IDAU(UUID.fromURN(urn));
            
        }

        return null;
 
    }

    private byte[] value;
    
    private IDAU() { }
    
    /**
     * Instantiates a IDAU from a 16-byte buffer
     * @param idau 16-bytes
     */
    public IDAU(byte[] idau) {
        this.value = java.util.Arrays.copyOf(idau, 16);
    }

    
    /**
     * Instantiates a IDAU from a UL
     * @param ul UL from which to create the IDAU
     */
    public IDAU(UL ul) {
        this.value = new byte[16];
        
        System.arraycopy(ul.getValue(), 8, this.value, 0, 8);
        System.arraycopy(ul.getValue(), 0, this.value, 8, 8);
    }

    /**
     * Instantiates a IDAU from a UUID
     * @param uuid UUID from which to create the IDAU
     */
    public IDAU(UUID uuid) {
        
        this.value = java.util.Arrays.copyOf(uuid.getValue(), 16);
        
    }

    @Override
    public boolean equals(Object idau) {
        if (!(idau instanceof IDAU)) {
            return false;
        }
        return Arrays.equals(((IDAU) idau).value, this.value);
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
            return asUUID().toString();
        }
    }

    /**
     * Is the IDAU a UL?
     * @return true if the IDAU is a UL
     */
    public boolean isUL() {
        return (value[9] & 0x80) == 0;
    }
    
    /**
     * Is the IDAU a UUID?
     * @return true if the IDAU is a UUID
     */
    public boolean isUUID() {
        return ! isUL();
    }
    
    /**
     * Returns the underlying UUID if available
     * @return Underlying UUID, or null if not a UUID
     */
    public UUID asUUID() {
        
        if (isUL()) return null;
        
        return new UUID(this.value);
    }
    
    /**
     * Returns as AUID
     * @return A newly-created AUID
     */
    public AUID asAUID() {
        
        byte[] auid = new byte[16];
        
        System.arraycopy(this.value, 8, auid, 0, 8);
        System.arraycopy(this.value, 0, auid, 8, 8);
        
        return new AUID(auid);
    }
    

    /**
     * Returns the underlying UL if available
     * @return Underlying UL, or null if not a UL
     */
    public UL asUL() {
        return isUL() ? asAUID().asUL() : null;
    }

}
