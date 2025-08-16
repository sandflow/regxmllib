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

package com.sandflow.smpte.regxml.dict.definitions;

import com.sandflow.smpte.util.AUID;
import com.sandflow.smpte.util.xml.AUIDAdapter;
import java.net.URI;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Base type for the MetaDictionary definitions. Each concrete subclass corresponds to 
 * a particular type of definition allowed in a MetaDictionary, as specified in
 * SMPTE ST 2001-1. XML Annotations are used to map the subclasses to an XML representation
 * consistent with that defined in SMPTE ST 2001-1.
 */
@XmlAccessorType(XmlAccessType.NONE)
abstract public class Definition {
    
    @XmlJavaTypeAdapter(value = AUIDAdapter.class)
    @XmlElement(name = "Identification")
    private AUID identification; 

    @XmlElement(name = "Symbol")
    private String symbol;

    @XmlElement(name = "Description")
    private String description;

    @XmlElement(name = "Name")
    private String name;
    
    @XmlTransient()
    private URI namespace;

    public URI getNamespace() {
        return namespace;
    }

    public void setNamespace(URI namespace) {
        this.namespace = namespace;
    }

    public AUID getIdentification() {
        return identification;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public void setIdentification(AUID identification) {
        this.identification = identification;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Supports the visitor design pattern.
     * @see DefinitionVisitor
     * @param visitor Visitor instance that will process the definition
     */
    abstract public void accept(DefinitionVisitor visitor) throws DefinitionVisitor.VisitorException;
    
}
