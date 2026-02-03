/*
 * Copyright (c) 2016, Pierre-Anthony Lemieux (pal@sandflow.com)
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
package com.sandflow.smpte.register.brown_sauce;

import com.sandflow.smpte.util.UL;
import com.sandflow.smpte.util.xml.HexBinaryByteAdapter;
import com.sandflow.smpte.util.xml.HexBinaryLongAdapter;
import com.sandflow.smpte.util.xml.ULAdapter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlList;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Groups Register as defined in SMPTE ST 395
 */
@XmlRootElement(name = "GroupsRegister", namespace = GroupsRegisterModel.XML_NAMESPACE)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "")
public class GroupsRegisterModel extends com.sandflow.smpte.register.GroupsRegister {

    public final static String XML_NAMESPACE = "http://www.smpte-ra.org/ns/395/2016";

    @XmlElement(name = "Entry", namespace = XML_NAMESPACE)
    @XmlElementWrapper(name = "Entries", namespace = XML_NAMESPACE)
    private final ArrayList<Entry> entries = new ArrayList<>();

    public GroupsRegisterModel() {
    }

    @Override
    public Collection<Entry> getEntries() {
        return entries;
    }

    /**
     * Single Entry in a Groups Register (SMPTE ST 395)
     */
    @XmlType(name = "GroupEntry", namespace = GroupsRegisterModel.XML_NAMESPACE)
    @XmlAccessorType(value = XmlAccessType.NONE)
    public static class Entry implements com.sandflow.smpte.register.GroupsRegister.Entry {

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
        
        @XmlElement(name = "IsDeprecated")
        private boolean deprecated = false;
        
        @XmlJavaTypeAdapter(value = ULAdapter.class)
        @XmlElement(name = "Parent")
        private UL parent;
        
        @XmlElement(name = "IsConcrete")
        private Boolean concrete;
        
        @XmlElement(name = "KLVSyntax")
        @XmlList
        @XmlJavaTypeAdapter(value = HexBinaryByteAdapter.class)
        @XmlSchemaType(name="hexBinary")
        private HashSet<Byte> klvSyntax = new HashSet<>();
        
        @XmlElementWrapper(name = "Contents", namespace = GroupsRegisterModel.XML_NAMESPACE)
        @XmlElement(name = "Record", namespace = GroupsRegisterModel.XML_NAMESPACE)
        private List<Record> contents = new ArrayList<>();

        @Override
        public URI getNamespaceName() {
            return namespaceName;
        }

        @Override
        public String getSymbol() {
            return symbol;
        }

        @Override
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

        @Override
        public Kind getKind() {
            return kind;
        }

        public void setKind(Kind kind) {
            this.kind = kind;
        }

        @Override
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String getDefinition() {
            return definition;
        }

        public void setDefinition(String definition) {
            this.definition = definition;
        }

        @Override
        public UL getParent() {
            return parent;
        }

        public void setParent(UL parent) {
            this.parent = parent;
        }

        @Override
        public Boolean isConcrete() {
            return concrete;
        }

        public void setConcrete(Boolean concrete) {
            this.concrete = concrete;
        }

        @Override
        public Set<Byte> getKlvSyntax() {
            return klvSyntax;
        }

        public void setKlvSyntax(Set<Byte> klvSyntax) {
            this.klvSyntax = new HashSet<>(klvSyntax);
        }

        @Override
        public Collection<? extends com.sandflow.smpte.register.GroupsRegister.Entry.Record> getContents() {
            return contents;
        }

        public void setContents(List<Record> contents) {
            this.contents = contents;
        }

        @Override
        public String getApplications() {
            return applications;
        }

        public void setApplications(String applications) {
            this.applications = applications;
        }

        @Override
        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        @Override
        public boolean isDeprecated() {
            return deprecated;
        }

        public void setDeprecated(boolean deprecated) {
            this.deprecated = deprecated;
        }

        @Override
        public String getDefiningDocument() {
            return definingDocument;
        }

        public void setDefiningDocument(String definingDocument) {
            this.definingDocument = definingDocument;
        }

        @XmlType(name = "RecordType", namespace = GroupsRegisterModel.XML_NAMESPACE)
        @XmlAccessorType(value = XmlAccessType.NONE)
        public static class Record implements com.sandflow.smpte.register.GroupsRegister.Entry.Record {

            @XmlJavaTypeAdapter(value = ULAdapter.class)
            @XmlElement(name = "UL", required = true)
            private UL item;
            
            @XmlElement(name = "LocalTag")
            @XmlJavaTypeAdapter(value = HexBinaryLongAdapter.class)
            @XmlSchemaType(name="hexBinary")
            private Long localTag;
            
            @XmlElement(name = "LimitLength")
            private Long limitLength;
            
            @XmlElement(name = "IsUniqueID")
            private Boolean uniqueID;
            
            @XmlElement(name = "IsOptional")
            private Boolean optional;
            
            @XmlElement(name = "IsIgnorable")
            private Boolean ignorable;
            
            @XmlElement(name = "IsDistinguished")
            private Boolean distinguished;
            
            @XmlElement(name = "Value")
            private String value;

            @Override
            public UL getItem() {
                return item;
            }

            public void setItem(UL item) {
                this.item = item;
            }

            @Override
            public Long getLocalTag() {
                return localTag;
            }

            public void setLocalTag(Long localTag) {
                this.localTag = localTag;
            }

            @Override
            public Long getLimitLength() {
                return limitLength;
            }

            public void setLimitLength(Long limitLength) {
                this.limitLength = limitLength;
            }

            @Override
            public Boolean getUniqueID() {
                return uniqueID;
            }

            public void setUniqueID(Boolean uniqueID) {
                this.uniqueID = uniqueID;
            }

            @Override
            public Boolean getOptional() {
                return optional;
            }

            public void setOptional(Boolean optional) {
                this.optional = optional;
            }

            @Override
            public Boolean getIgnorable() {
                return ignorable;
            }

            public void setIgnorable(Boolean ignorable) {
                this.ignorable = ignorable;
            }

            @Override
            public Boolean getDistinguished() {
                return distinguished;
            }

            public void setDistinguished(Boolean distinguished) {
                this.distinguished = distinguished;
            }

            @Override
            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }
    }

}
