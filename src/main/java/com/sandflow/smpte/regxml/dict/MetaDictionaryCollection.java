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
package com.sandflow.smpte.regxml.dict;

import com.sandflow.smpte.regxml.dict.definitions.ClassDefinition;
import com.sandflow.smpte.regxml.dict.definitions.Definition;
import com.sandflow.smpte.regxml.dict.exceptions.IllegalDefinitionException;
import com.sandflow.smpte.regxml.dict.exceptions.IllegalDictionaryException;
import com.sandflow.smpte.util.AUID;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * A collection of multiple RegXML Metadictionary as specified in SMPTE ST 2001-1
 */
public class MetaDictionaryCollection implements DefinitionResolver {

    final private HashMap<URI, MetaDictionary> dicts = new HashMap<>();

    @Override
    public Definition getDefinition(AUID auid) {
        Definition def = null;

        for (MetaDictionary md : dicts.values()) {
            if ((def = md.getDefinition(auid)) != null) {
                break;
            }
        }

        return def;
    }
    
    /**
     * Retrieves a definition from the collection based on its symbol
     * @param namespace Namespace of the definition
     * @param symbol Symbol of the definition
     * @return Definition, or null if none found
     */
    public Definition getDefinition(URI namespace, String symbol) {
        Definition def = null;
        
        MetaDictionary md = dicts.get(namespace);
        
        if (md != null) {
            def = md.getDefinition(symbol);
        }

        return def;
    }

    /**
     * Adds a MetaDictionary to the collection. 
     * 
     * @param metadictionary MetaDictionary to be added
     * @throws IllegalDictionaryException If the MetaDictionary
     */
    public void addDictionary(MetaDictionary metadictionary) throws IllegalDictionaryException {
        MetaDictionary oldmd = dicts.get(metadictionary.getSchemeURI());

        if (oldmd == null) {
            dicts.put(metadictionary.getSchemeURI(), metadictionary);
        } else {
            throw new IllegalDictionaryException("Metadictionary already present in group.");
        }

    }

    /**
     * Adds a definition to the collection. Automatically creates a MetaDictionary
     * if none exists with the namespace of the definition.
     * 
     * @param def Definition to be added
     * @throws IllegalDefinitionException 
     */
    public void addDefinition(Definition def) throws IllegalDefinitionException {
        MetaDictionary md = dicts.get(def.getNamespace());

        if (md == null) {
            md = new MetaDictionary(def.getNamespace());

            dicts.put(md.getSchemeURI(), md);
        }

        md.add(def);
    }

    /**
     * Returns all the members of the collection
     * @return Collection of MetaDictionaries
     */
    public Collection<MetaDictionary> getDictionaries() {
        return dicts.values();
    }

    @Override
    public Collection<AUID> getSubclassesOf(ClassDefinition parent) {

        ArrayList<AUID> subclasses = new ArrayList<>();

        for (MetaDictionary md : dicts.values()) {
            
            Collection<AUID> defs = md.getSubclassesOf(parent);
            
            if (defs != null) subclasses.addAll(defs);

        }

        return subclasses;
    }

    @Override
    public Collection<AUID> getMembersOf(ClassDefinition parent) {
        ArrayList<AUID> members = new ArrayList<>();

        for (MetaDictionary md : dicts.values()) {
            
            Collection<AUID> defs = md.getMembersOf(parent);

            if (defs != null) members.addAll(defs);
        }

        return members;
    }

}
