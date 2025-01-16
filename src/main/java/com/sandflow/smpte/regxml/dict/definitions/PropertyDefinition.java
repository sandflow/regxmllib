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
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Property Definition as defined in ST 2001-1
 */
@XmlAccessorType(XmlAccessType.NONE)
public class PropertyDefinition extends Definition {

    public PropertyDefinition() {
    }
    
    

    @XmlJavaTypeAdapter(value = AUIDAdapter.class)
    @XmlElement(name = "Type")
    private AUID type;

    @XmlElement(name = "IsOptional")
    private boolean optional;

    @XmlElement(name = "IsUniqueIdentifier")
    private boolean uniqueIdentifier;

    @XmlElement(name = "LocalIdentification")
    private int localIdentification;

    @XmlJavaTypeAdapter(value = AUIDAdapter.class)
    @XmlElement(name = "MemberOf")
    private AUID memberOf;

    @Override
    public void accept(DefinitionVisitor visitor) throws DefinitionVisitor.VisitorException {
        visitor.visit(this);
    }

    public AUID getType() {
        return type;
    }

    public void setType(AUID type) {
        this.type = type;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public boolean isUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public void setUniqueIdentifier(boolean uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public int getLocalIdentification() {
        return localIdentification;
    }

    public void setLocalIdentification(int localIdentification) {
        this.localIdentification = localIdentification;
    }

    public AUID getMemberOf() {
        return memberOf;
    }

    public void setMemberOf(AUID memberOf) {
        this.memberOf = memberOf;
    }

}
