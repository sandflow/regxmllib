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
package com.sandflow.smpte.klv;

import com.sandflow.smpte.klv.exceptions.KLVException;
import com.sandflow.smpte.klv.exceptions.TripletLengthException;
import com.sandflow.smpte.util.CountingInputStream;
import com.sandflow.smpte.util.UL;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * LocalSet implements a Local Set as specified in SMPTE ST 336. 
 */
public class LocalSet implements Group {

    /**
     * Creates a Group from a Local Set using a LocalTagRegister to map Local Tags to Keys 
     * @param localset Triplet containing a Group encoded as a Local Set
     * @param reg LocalTagRegister used to map Local Tags to Keys
     * @return Local Set, or null if the input Triplet is not a Local Set
     * @throws KLVException 
     */
    public static LocalSet fromTriplet(Triplet localset, LocalTagRegister reg) throws KLVException {
        try {

            if (!(localset.getKey().isUL() && localset.getKey().asUL().isLocalSet())) {
                return null;
            }
            
            UL lskey = localset.getKey().asUL();
            
            CountingInputStream cis = new CountingInputStream(localset.getValueAsStream());

            KLVInputStream kis = new KLVInputStream(cis);

            LocalSet set = new LocalSet(lskey);

            while(cis.getCount() < localset.getLength()) {

                long localtag = 0;

                /* read local tag */
                switch (lskey.getRegistryDesignator() >> 3 & 3) {

                    /* 1 byte length field */
                    case 0:
                        localtag = kis.readUnsignedByte();
                        break;

                    /* ASN.1 OID BER length field */
                    case 1:
                        localtag = kis.readBERLength();
                        break;

                    /* 2 byte length field */
                    case 2:
                        localtag = kis.readUnsignedShort();
                        break;

                    /* 4 byte length field */
                    case 3:
                        localtag = kis.readUnsignedInt();
                        break;
                }

                long locallen = 0;

                /* read local length */
                switch (lskey.getRegistryDesignator() >> 5 & 3) {

                    /* ASN.1 OID BER length field */
                    case 0:
                        locallen = kis.readBERLength();
                        break;

                    /* 1 byte length field */
                    case 1:
                        locallen = kis.readUnsignedByte();
                        break;

                    /* 2 byte length field */
                    case 2:
                        locallen = kis.readUnsignedShort();
                        break;

                    /* 4 byte length field */
                    case 3:
                        locallen = kis.readUnsignedInt();
                        break;
                }

                if (locallen > Integer.MAX_VALUE) {
                    throw new TripletLengthException();
                }

                byte[] localval = new byte[(int) locallen];

                kis.readFully(localval);
                
                if (reg.get(localtag) == null) {
                    throw new KLVException("Local tag not found: " + localtag + " in Local Set " + localset.getKey());
                }

                set.addItem(new MemoryTriplet(reg.get(localtag), localval));

            }

            return set;
                    
        } catch (IOException e) {
            throw new KLVException("Error parsing Local Set: " + localset.getKey(), e);
        }
        
    }

    private final ArrayList<Triplet> items = new ArrayList<>();

    private final UL key;

    private LocalSet(UL key) {
        this.key = key;
    }

    @Override
    public UL getKey() {
        return key;
    }

    @Override
    public Collection<Triplet> getItems() {
        return items;
    }

    private void addItem(Triplet triplet) {
        items.add(triplet);
    }

}
