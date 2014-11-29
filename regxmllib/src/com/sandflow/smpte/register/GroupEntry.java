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
package com.sandflow.smpte.register;

import com.sandflow.smpte.util.UL;
import com.sandflow.smpte.util.xml.ULAdapter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
@XmlType(name = "GroupEntry", namespace = GroupsRegister.XML_NAMESPACE)
@XmlAccessorType(XmlAccessType.NONE)
public class GroupEntry {

    @XmlElement(name = "Register")
    static final String register = "Groups";

    @XmlElement(name = "NamespaceName", required = true)
    protected URI namespaceName;

    @XmlElement(name = "Symbol", required = true)
    protected String symbol;

    @XmlJavaTypeAdapter(value = ULAdapter.class)
    @XmlElement(name = "UL", required = true)
    protected UL ul;

    @XmlElement(name = "Kind")
    private Kind kind;

    @XmlElement(name = "Name")
    private String name;

    @XmlElement(name = "Definition")
    private String definition;
    
    @XmlElement(name = "Applications")
    private String applications;

    @XmlElement(name = "Notes")
    private String notes;
    
    @XmlElement(name = "DefiningDocument")
    private String definingDocument;

    @XmlElement(name = "isDeprecated")
    private boolean deprecated = false;

    @XmlJavaTypeAdapter(value = ULAdapter.class)
    @XmlElement(name = "Parent")
    private UL parent;

    @XmlElement(name = "isConcrete")
    private boolean concrete;

    @XmlElement(name = "KLVSyntax")
    @XmlList
    private HashSet<Byte> klvSyntax = new HashSet<>();

    @XmlElementWrapper(name = "Contents", namespace = GroupsRegister.XML_NAMESPACE)
    @XmlElement(name = "Record", namespace = GroupsRegister.XML_NAMESPACE)
    private List<Record> contents = new ArrayList<>();

    public URI getNamespaceName() {
        return namespaceName;
    }

    public String getSymbol() {
        return symbol;
    }

    public UL getUL() {
        return ul;
    }

    public void setNamespaceName(URI namespaceName) {
        this.namespaceName = namespaceName;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setUL(UL ul) {
        this.ul = ul;
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public UL getParent() {
        return parent;
    }

    public void setParent(UL parent) {
        this.parent = parent;
    }

    public boolean isConcrete() {
        return concrete;
    }

    public void setConcrete(boolean concrete) {
        this.concrete = concrete;
    }

    public Set<Byte> getKlvSyntax() {
        return klvSyntax;
    }

    public void setKlvSyntax(Set<Byte> klvSyntax) {
        this.klvSyntax = new HashSet<Byte>(klvSyntax);
    }

    public List<Record> getContents() {
        return contents;
    }

    public void setContents(List<Record> contents) {
        this.contents = contents;
    }

    public String getApplications() {
        return applications;
    }

    public void setApplications(String applications) {
        this.applications = applications;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public String getDefiningDocument() {
        return definingDocument;
    }

    public void setDefiningDocument(String definingDocument) {
        this.definingDocument = definingDocument;
    }
    
    @XmlType(name = "RecordType", namespace = GroupsRegister.XML_NAMESPACE)
    @XmlAccessorType(value = XmlAccessType.NONE)
    public static class Record {

        @XmlJavaTypeAdapter(value = ULAdapter.class)
        @XmlElement(name = "UL")
        private UL item;

        @XmlElement(name = "LocalTag")
        private Long localTag;

        @XmlElement(name = "LimitLength")
        private Long limitLength;

        @XmlElement(name = "isUniqueID")
        private boolean uniqueID = false;
        
        @XmlElement(name = "isOptional")
        private boolean optional = true;

        @XmlElement(name = "isIgnorable")
        private boolean ignorable = false;

        @XmlElement(name = "isDistinguished")
        private boolean distinguished = false;

        @XmlElement(name = "Value")
        private String value;

        public UL getItem() {
            return item;
        }

        public void setItem(UL item) {
            this.item = item;
        }

        public Long getLocalTag() {
            return localTag;
        }

        public void setLocalTag(Long localTag) {
            this.localTag = localTag;
        }

        public Long getLimitLength() {
            return limitLength;
        }

        public void setLimitLength(Long limitLength) {
            this.limitLength = limitLength;
        }

        public Boolean getUniqueID() {
            return uniqueID;
        }

        public void setUniqueID(boolean uniqueID) {
            this.uniqueID = uniqueID;
        }

        public boolean getOptional() {
            return optional;
        }

        public void setOptional(boolean optional) {
            this.optional = optional;
        }

        public boolean getIgnorable() {
            return ignorable;
        }

        public void setIgnorable(boolean ignorable) {
            this.ignorable = ignorable;
        }

        public boolean getDistinguished() {
            return distinguished;
        }

        public void setDistinguished(boolean distinguished) {
            this.distinguished = distinguished;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }

    @XmlType(name = "")
    public enum Kind {
        NODE, LEAF
    }

}
