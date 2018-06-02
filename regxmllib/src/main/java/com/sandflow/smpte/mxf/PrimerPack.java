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
package com.sandflow.smpte.mxf;

import com.sandflow.smpte.klv.LocalTagRegister;
import com.sandflow.smpte.klv.Triplet;
import com.sandflow.smpte.klv.exceptions.KLVException;
import com.sandflow.smpte.util.AUID;
import com.sandflow.smpte.util.UL;
import java.io.IOException;
import java.util.HashMap;

/**
 * Represents a MXF Primer Pack (see SMPTE ST 377-1)
 */
public class PrimerPack {

    private static final UL KEY = new UL(new byte[]{0x06, 0x0e, 0x2b, 0x34, 0x02, 0x05, 0x01, 0x01, 0x0d, 0x01, 0x02, 0x01, 0x01, 0x05, 0x01, 0x00});

    /**
     * Creates a LocalTagRegister from a PrimerPack
     *
     * @param triplet Triplet representation of the Primer Pack
     * @return LocalTagRegister or null if the Triplet is not a Primer Pack
     * @throws KLVException
     */
    public static LocalTagRegister createLocalTagRegister(Triplet triplet) throws KLVException {

        if (!PrimerPack.KEY.equalsIgnoreVersion(triplet.getKey())) {
            return null;
        }

        HashMap<Long, AUID> reg = new HashMap<>();

        MXFInputStream kis = new MXFInputStream(triplet.getValueAsStream());

        try {

            long itemcount = kis.readUnsignedInt();

            long itemlength = kis.readUnsignedInt();

            for (int i = 0; i < itemcount; i++) {

                reg.put((long) kis.readUnsignedShort(), kis.readAUID());
            }

        } catch (IOException e) {
            throw new KLVException(e);
        }

        return new LocalTagRegister(reg);
    }

    /**
     * Returns the Primer Pack Key
     *
     * @return Key
     */
    public static UL getKey() {
        return KEY;
    }
}
