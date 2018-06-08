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

import com.sandflow.smpte.util.AUID;
import java.util.HashMap;
import java.util.Map;

/**
 * LocalTagRegister maps Local Tags found in a Local Set to AUID Keys
 */
public class LocalTagRegister {

    private final HashMap<Long, AUID> entries = new HashMap<>();
    
    /**
     * Instantiates an empty LocalTagRegister
     */
    public LocalTagRegister() { }

    /**
     * Instantiates a LocalTagRegister with an initial set of mappings from Local Tag values to AUID Keys
     * @param entries Initial set of mappings
     */
    public LocalTagRegister(Map<Long, AUID> entries) {
        this.entries.putAll(entries);
    }

    /**
     * Returns the Key corresponding to a Local Tag
     * @param localtag Local Tag
     * @return Key, or null if no Key exists for the Local Tag
     */
    public AUID get(long localtag) {
        return entries.get(localtag);
    }
    
    /**
     * Adds a Local Tag to the registry.
     * @param localtag Local Tag
     * @param key Key with which the Local Tag is associated
     * @return The Key is the Local Tag was already present in the registry, or null otherwise.
     */
    public AUID add(long localtag, AUID key) {
        return entries.put(localtag, key);
    }
    

}
