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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
@XmlType(name = "ElementEntry", namespace = ElementsRegister.REG_NAMESPACE)
@XmlAccessorType(XmlAccessType.NONE)
public class ElementEntry {

    @XmlElement(name = "Register")
    static final String register = "Elements";

    @XmlElement(name = "NamespaceName")
    protected URI namespaceName;

    @XmlElement(name = "Symbol")
    protected String symbol;

    @XmlJavaTypeAdapter(value = ULAdapter.class)
    @XmlElement(name = "UL")
    protected UL ul;

    @XmlElement(name = "Kind")
    private Kind kind;

    @XmlElement(name = "Name")
    private String name;

    @XmlElement(name = "Definition")
    private String definition;

    @XmlJavaTypeAdapter(value = ULAdapter.class)
    @XmlElement(name = "TypeUL")
    private UL typeUL;

    @XmlElement(name = "ValueLength")
    private String valueLength;

    @XmlElement(name = "ValueRange")
    private String valueRange;

    @XmlElement(name = "UnitOfMeasure")
    private String unitOfMeasure;

    @XmlElement(name = "DefiningDocument")
    private String definingDocument;

    @XmlElement(name = "ContextScope")
    private String contextScope;

    @XmlElement(name = "Applications")
    private String applications;

    @XmlElement(name = "Notes")
    private String notes;

    @XmlElement(name = "IsDeprecated")
    private boolean deprecated;

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

    public UL getTypeUL() {
        return typeUL;
    }

    public void setTypeUL(UL typeUL) {
        this.typeUL = typeUL;
    }

    public String getValueLength() {
        return valueLength;
    }

    public void setValueLength(String valueLength) {
        this.valueLength = valueLength;
    }

    public String getValueRange() {
        return valueRange;
    }

    public void setValueRange(String valueRange) {
        this.valueRange = valueRange;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public String getDefiningDocument() {
        return definingDocument;
    }

    public void setDefiningDocument(String definingDocument) {
        this.definingDocument = definingDocument;
    }

    public String getContextScope() {
        return contextScope;
    }

    public void setContextScope(String contextScope) {
        this.contextScope = contextScope;
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

    @XmlType(name = "")
    public enum Kind {

        NODE, LEAF
    }

}
