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
import java.util.EnumSet;
import java.util.List;
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
@XmlType(name = "TypeEntry", namespace = TypesRegister.XML_NAMESPACE)
@XmlAccessorType(XmlAccessType.NONE)
public class TypeEntry {

    public static final String INTEGER_TYPEKIND = "Integer";
    public static final String RENAME_TYPEKIND = "Rename";
    public static final String RECORD_TYPEKIND = "Record";
    public static final String ARRAY_TYPEKIND = "VariableArray";
    public static final String FIXEDARRAY_TYPEKIND = "FixedArray";
    public static final String CHARACTER_TYPEKIND = "Character";
    public static final String STRING_TYPEKIND = "String";
    public static final String ENUMERATION_TYPEKIND = "Enumeration";
    public static final String SET_TYPEKIND = "Set";
    public static final String STREAM_TYPEKIND = "Stream";
    public static final String INDIRECT_TYPEKIND = "Indirect";
    public static final String OPAQUE_TYPEKIND = "Opaque";
    public static final String STRONGREF_TYPEKIND = "StrongReference";
    public static final String WEAKREF_TYPEKIND = "WeakReference";
    public static final String FLOAT_TYPEKIND = "Float";
    
    /**
     * @deprecated
     */
    public static final String LENSSERIALFLOAT_TYPEKIND = "LensSerialFloat";

    @XmlElement(name = "Register")
    final static String register = "Types";

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


    public TypeEntry() {
    }

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

    public EnumSet<TypeQualifiers> getTypeQualifiers() {
        return typeQualifiers;
    }

    public void setTypeQualifiers(EnumSet<TypeQualifiers> typeQualifiers) {
        this.typeQualifiers = typeQualifiers;
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

    public String getTypeKind() {
        return typeKind;
    }

    public void setTypeKind(String typeKind) {
        this.typeKind = typeKind;
    }

    public Long getTypeSize() {
        return typeSize;
    }

    public void setTypeSize(Long typeSize) {
        this.typeSize = typeSize;
    }

    public UL getBaseType() {
        return baseType;
    }

    public void setBaseType(UL baseType) {
        this.baseType = baseType;
    }

    public List<Facet> getFacets() {
        return facets;
    }

    public void setFacets(List<Facet> facets) {
        this.facets = facets;
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

    public ContextScope getContextScope() {
        return contextScope;
    }

    public void setContextScope(ContextScope contextScope) {
        this.contextScope = contextScope;
    }

    @XmlType(name = "FacetType", namespace = TypesRegister.XML_NAMESPACE)
    @XmlAccessorType(value = XmlAccessType.NONE)
    public static class Facet {

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

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public UL getType() {
            return type;
        }

        public void setType(UL type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public UL getUL() {
            return ul;
        }

        public void setUL(UL ul) {
            this.ul = ul;
        }

        public String getDefinition() {
            return definition;
        }

        public void setDefinition(String definition) {
            this.definition = definition;
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
    }

    @XmlType(name = "")
    public enum TypeQualifiers {

        isNumeric, isSigned, isIdentified, isOrdered, isCountImplicit, isSizeImplicit
    }

    @XmlType(name = "")
    public enum Kind {

        NODE,
        LEAF
    }

    @XmlType(name = "")
    public enum ContextScope {

        DefinedContext, AbstractContext, UnknownContext
    }

}
