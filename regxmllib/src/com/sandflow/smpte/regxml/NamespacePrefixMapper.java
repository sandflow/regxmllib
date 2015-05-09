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
package com.sandflow.smpte.regxml;

import java.net.URI;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
public class NamespacePrefixMapper {

    private final HashMap<URI, String> uris = new HashMap<>();
    private final HashMap<String, URI> prefixes = new HashMap<>();

    public String getPrefixOrCreate(URI ns) {
        String prefix = this.uris.get(ns);

        /* if prefix does not exist, create one */
        if (prefix == null) {
            prefix = "r" + this.uris.size();

            this.uris.put(ns, prefix);
            this.prefixes.put(prefix, ns);
        }

        return prefix;
    }

    public String putPrefix(URI ns, String suggested) {

        String np = this.uris.get(ns);
        URI uri = this.prefixes.get(suggested);

        /* if the prefix already exists, create a new one */
        if (uri != null) {
            np = "r" + this.uris.size();

        } else {
            np = suggested;
        }

        this.prefixes.put(np, ns);

        this.uris.put(ns, np);

        return np;

    }
    
    public Set<URI> getURIs() {
        return uris.keySet();
    }
    
    public void clear() {
        this.uris.clear();
        this.prefixes.clear();
    }
}
