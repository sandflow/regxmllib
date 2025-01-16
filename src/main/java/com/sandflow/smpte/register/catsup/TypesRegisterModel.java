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
package com.sandflow.smpte.register.catsup;

import com.sandflow.smpte.util.UL;
import com.sandflow.smpte.util.xml.ULAdapter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlList;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Types Register as defined in SMPTE ST 2003
 */
@XmlRootElement(name = "TypesRegister", namespace = TypesRegisterModel.XML_NAMESPACE)
@XmlType(name = "")
@XmlAccessorType(XmlAccessType.NONE)
public class TypesRegisterModel extends com.sandflow.smpte.register.TypesRegister {
    
    public final static String XML_NAMESPACE = "http://www.smpte-ra.org/schemas/2003/2012";

    @XmlElement(name="Entry", namespace = XML_NAMESPACE)
    @XmlElementWrapper(name = "Entries", namespace = XML_NAMESPACE)
    private final ArrayList<Entry> entries = new ArrayList<>();

    public TypesRegisterModel() {
    }
    
    @Override
    public Collection<Entry> getEntries() {
        return entries;
    }

    /**
     * Single Entry in a Types Register (SMPTE ST 2003)
     */
    @XmlType(name = "TypeEntry", namespace = TypesRegisterModel.XML_NAMESPACE)
    @XmlAccessorType(value = XmlAccessType.NONE)
    public static class Entry implements com.sandflow.smpte.register.TypesRegister.Entry {

        @XmlElement(name = "Register")
        static final String register = "Types";
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
        private boolean deprecated;
        @XmlElement(name = "ContextScope")
        private ContextScope contextScope = ContextScope.UnknownContext;
        @XmlElement(name = "TypeKind")
        private String typeKind;
        @XmlElement(name = "TypeSize")
        private Long typeSize;
        @XmlJavaTypeAdapter(value = ULAdapter.class)
        @XmlElement(name = "BaseType")
        private UL baseType;
        @XmlElement(name = "TypeQualifiers")
        @XmlList
        private EnumSet<TypeQualifiers> typeQualifiers = EnumSet.noneOf(TypeQualifiers.class);
        @XmlElementWrapper(name = "Facets")
        @XmlElement(name = "Facet")
        private List<Facet> facets = new ArrayList<>();


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
        public EnumSet<TypeQualifiers> getTypeQualifiers() {
            return typeQualifiers;
        }

        public void setTypeQualifiers(EnumSet<TypeQualifiers> typeQualifiers) {
            this.typeQualifiers = typeQualifiers;
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
        public String getTypeKind() {
            return typeKind;
        }

        public void setTypeKind(String typeKind) {
            this.typeKind = typeKind;
        }

        @Override
        public Long getTypeSize() {
            return typeSize;
        }

        public void setTypeSize(Long typeSize) {
            this.typeSize = typeSize;
        }

        @Override
        public UL getBaseType() {
            return baseType;
        }

        public void setBaseType(UL baseType) {
            this.baseType = baseType;
        }

        @Override
        public Collection<? extends com.sandflow.smpte.register.TypesRegister.Entry.Facet> getFacets() {
            return facets;
        }

        public void setFacets(List<Facet> facets) {
            this.facets = facets;
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

        @Override
        public ContextScope getContextScope() {
            return contextScope;
        }

        public void setContextScope(ContextScope contextScope) {
            this.contextScope = contextScope;
        }

        @XmlType(name = "FacetType", namespace = TypesRegisterModel.XML_NAMESPACE)
        @XmlAccessorType(value = XmlAccessType.NONE)
        public static class Facet implements com.sandflow.smpte.register.TypesRegister.Entry.Facet {

            @XmlElement(name = "Symbol")
            private String symbol;
            @XmlElement(name = "Name")
            private String name;
            @XmlJavaTypeAdapter(value = ULAdapter.class)
            @XmlElement(name = "Type")
            private UL type;
            @XmlElement(name = "Value")
            private String value;
            @XmlJavaTypeAdapter(value = ULAdapter.class)
            @XmlElement(name = "UL")
            private UL ul;
            @XmlElement(name = "Definition")
            private String definition;
            @XmlElement(name = "Applications")
            private String applications;
            @XmlElement(name = "Notes")
            private String notes;
            @XmlElement(name = "IsDeprecated")
            private boolean deprecated;

            @Override
            public String getSymbol() {
                return symbol;
            }

            public void setSymbol(String symbol) {
                this.symbol = symbol;
            }

            @Override
            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            @Override
            public UL getType() {
                return type;
            }

            public void setType(UL type) {
                this.type = type;
            }

            @Override
            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }

            @Override
            public UL getUL() {
                return ul;
            }

            public void setUL(UL ul) {
                this.ul = ul;
            }

            @Override
            public String getDefinition() {
                return definition;
            }

            public void setDefinition(String definition) {
                this.definition = definition;
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
        }

    }

}
