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

import com.sandflow.smpte.util.AUID;
import com.sandflow.smpte.util.UL;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Unmarshals/marshals an xsd:string to/from a AIUD
 */
public class AUIDAdapter extends XmlAdapter<String, AUID> {

    @Override
    public AUID unmarshal(String val) throws Exception {

        AUID auid;

        if (val.charAt(15) == '.') {
            byte[] ul = new byte[16];

            for (int i = 0; i < 16; i++) {
                ul[i] = (byte) Integer.parseInt(val.substring(13 + i * 3, 13 + i * 3 + 2), 16);
            }

            auid = new AUID(new UL(ul));
        } else {

            auid = AUID.fromURN(val);
        }

        return auid;
    }

    @Override
    public String marshal(AUID val) throws Exception {
        return val.toString();
    }
}
