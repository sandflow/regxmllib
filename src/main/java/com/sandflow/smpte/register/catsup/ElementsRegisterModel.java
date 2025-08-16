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
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Elements Register as defined in SMPTE ST 335
 */
@XmlRootElement(name = "ElementsRegister", namespace = ElementsRegisterModel.XML_NAMESPACE)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "")
public class ElementsRegisterModel extends com.sandflow.smpte.register.ElementsRegister {
    
    public final static String XML_NAMESPACE = "http://www.smpte-ra.org/schemas/335/2012";

    @XmlElement(name = "Entry", namespace = XML_NAMESPACE)
    @XmlElementWrapper(name = "Entries", namespace = XML_NAMESPACE)
    private final ArrayList<Entry> entries = new ArrayList<>();

    public ElementsRegisterModel() {
    }

    @Override
    public Collection<? extends Entry> getEntries() {
        return entries;
    }

    /**
     * Single Entry in an Elements Register (SMPTE ST 335)
     */
    @XmlType(name = "ElementEntry", namespace = ElementsRegisterModel.XML_NAMESPACE)
    @XmlAccessorType(value = XmlAccessType.NONE)
    public static class Entry implements com.sandflow.smpte.register.ElementsRegister.Entry {

        @XmlElement(name = "Register")
        static final String register = "Elements";
        
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
        private String contextScope;
        
        @XmlJavaTypeAdapter(value = ULAdapter.class)
        @XmlElement(name = "Type")
        private UL typeUL;
        
        @XmlElement(name = "ValueLength")
        private String valueLength;
        
        @XmlElement(name = "ValueRange")
        private String valueRange;
        
        @XmlElement(name = "UnitOfMeasure")
        private String unitOfMeasure;

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
        public UL getTypeUL() {
            return typeUL;
        }

        public void setTypeUL(UL typeUL) {
            this.typeUL = typeUL;
        }

        @Override
        public String getValueLength() {
            return valueLength;
        }

        public void setValueLength(String valueLength) {
            this.valueLength = valueLength;
        }

        @Override
        public String getValueRange() {
            return valueRange;
        }

        public void setValueRange(String valueRange) {
            this.valueRange = valueRange;
        }

        @Override
        public String getUnitOfMeasure() {
            return unitOfMeasure;
        }

        public void setUnitOfMeasure(String unitOfMeasure) {
            this.unitOfMeasure = unitOfMeasure;
        }

        @Override
        public String getDefiningDocument() {
            return definingDocument;
        }

        public void setDefiningDocument(String definingDocument) {
            this.definingDocument = definingDocument;
        }

        @Override
        public String getContextScope() {
            return contextScope;
        }

        public void setContextScope(String contextScope) {
            this.contextScope = contextScope;
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
