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
 * AUID as specified in SMPTE ST 377-1
 */
public class AUID {
    
    /**
     * Creates a AUID from a UL or UUID URN. 
     * @param urn URN from which to create the AUID
     * @return AUID, or null if invalid URN
     */
    public static AUID fromURN(String urn) {
        
        if (urn == null) return null;
        
        if (urn.startsWith("urn:smpte:ul:")) {
            
            return new AUID(UL.fromURN(urn));
            
        } else if (urn.startsWith("urn:uuid:")) {
            
            return new AUID(UUID.fromURN(urn));
            
        }

        return null;
 
    }

    private byte[] value;
    
    private AUID() { }
    
    /**
     * Instantiates a AUID from a 16-byte buffer
     * @param auid 16-bytes
     */
    public AUID(byte[] auid) {
        this.value = java.util.Arrays.copyOf(auid, 16);
    }

    
    /**
     * Instantiates a AUID from a UL
     * @param ul UL from which to create the AUID
     */
    public AUID(UL ul) {
        this.value = ul.getValue();
    }

    /**
     * Instantiates a AUID from a UUID
     * @param uuid UUID from which to create the AUID
     */
    public AUID(UUID uuid) {
        
        value = new byte[16];
        
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
            return asUUID().toString();
        }
    }

    /**
     * Is the AUID a UL?
     * @return true if the AUID is a UL
     */
    public boolean isUL() {
        return (value[0] & 0x80) == 0;
    }
    
    /**
     * Is the AUID a UUID?
     * @return true if the AUID is a UUID
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
        
        byte[] uuid = new byte[16];
        
        System.arraycopy(this.value, 8, uuid, 0, 8);
        System.arraycopy(this.value, 0, uuid, 8, 8);
        
        return new UUID(uuid);
    }

    /**
     * Returns the underlying UL if available
     * @return Underlying UL, or null if not a UL
     */
    public UL asUL() {
        return isUL() ? new UL(value) : null;
    }

}
