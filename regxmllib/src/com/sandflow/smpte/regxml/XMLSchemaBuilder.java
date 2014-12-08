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

import com.sandflow.smpte.klv.exception.KLVException;
import com.sandflow.smpte.regxml.definition.CharacterTypeDefinition;
import com.sandflow.smpte.regxml.definition.ClassDefinition;
import com.sandflow.smpte.regxml.definition.Definition;
import com.sandflow.smpte.regxml.definition.EnumerationTypeDefinition;
import com.sandflow.smpte.regxml.definition.ExtendibleEnumerationTypeDefinition;
import com.sandflow.smpte.regxml.definition.FixedArrayTypeDefinition;
import com.sandflow.smpte.regxml.definition.IndirectTypeDefinition;
import com.sandflow.smpte.regxml.definition.IntegerTypeDefinition;
import com.sandflow.smpte.regxml.definition.OpaqueTypeDefinition;
import com.sandflow.smpte.regxml.definition.PropertyDefinition;
import com.sandflow.smpte.regxml.definition.RecordTypeDefinition;
import com.sandflow.smpte.regxml.definition.RenameTypeDefinition;
import com.sandflow.smpte.regxml.definition.SetTypeDefinition;
import com.sandflow.smpte.regxml.definition.StreamTypeDefinition;
import com.sandflow.smpte.regxml.definition.StringTypeDefinition;
import com.sandflow.smpte.regxml.definition.StrongReferenceTypeDefinition;
import com.sandflow.smpte.regxml.definition.VariableArrayTypeDefinition;
import com.sandflow.smpte.regxml.definition.WeakReferenceTypeDefinition;
import com.sandflow.smpte.regxml.dict.DefinitionResolver;
import com.sandflow.smpte.regxml.dict.MetaDictionary;
import com.sandflow.smpte.util.AUID;
import com.sandflow.smpte.util.UL;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
public class XMLSchemaBuilder {

    private static final String REGXML_NS = "http://www.smpte-ra.org/schemas/2001-1b/2013/metadict";

    private static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";

    private static final AUID AUID_AUID = new AUID(UL.fromDotValue("06.0E.2B.34.01.04.01.01.01.03.01.00.00.00.00.00"));
    private static final AUID UUID_AUID = new AUID(UL.fromDotValue("06.0E.2B.34.01.04.01.01.01.03.03.00.00.00.00.00"));
    private static final AUID DateStruct_AUID = new AUID(UL.fromDotValue("06.0E.2B.34.01.04.01.01.03.01.05.00.00.00.00.00"));
    private static final AUID PackageID_AUID = new AUID(UL.fromDotValue("06.0E.2B.34.01.04.01.01.01.03.02.00.00.00.00.00"));
    private static final AUID Rational_AUID = new AUID(UL.fromDotValue("06.0E.2B.34.01.04.01.01.03.01.01.00.00.00.00.00"));
    private static final AUID TimeStruct_AUID = new AUID(UL.fromDotValue("06.0E.2B.34.01.04.01.01.03.01.06.00.00.00.00.00"));
    private static final AUID TimeStamp_AUID = new AUID(UL.fromDotValue("06.0E.2B.34.01.04.01.01.03.01.07.00.00.00.00.00"));
    private static final AUID VersionType_AUID = new AUID(UL.fromDotValue("06.0E.2B.34.01.04.01.01.03.01.03.00.00.00.00.00"));

    private static final AUID ObjectClass_AUID = new AUID(UL.fromDotValue("06.0E.2B.34.01.01.01.02.06.01.01.04.01.01.00.00"));

    private static final AUID ByteOrder_AUID = new AUID(UL.fromDotValue("06.0E.2B.34.01.01.01.01.03.01.02.01.02.00.00.00"));

    private DefinitionResolver resolver;
    private final NamespacePrefixMapper prefixes = new NamespacePrefixMapper();

    private static final String STD_DECL = "<?xml version='1.0' encoding='UTF-8'?>\n"
            + "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'\n"
            + "  elementFormDefault='qualified' attributeFormDefault='unqualified'>\n"
            + "<xs:import namespace='http://www.w3.org/1999/xlink' schemaLocation='http://www.w3.org/1999/xlink.xsd'/>\n"
            + "<xs:simpleType name='TargetType'>\n"
            + "    <xs:union memberTypes='token'>\n"
            + "        <xs:simpleType>\n"
            + "            <xs:restriction base='reg:AUID'/>\n"
            + "        </xs:simpleType>\n"
            + "        <xs:simpleType>\n"
            + "            <xs:restriction base='reg:PackageIDType'/>\n"
            + "        </xs:simpleType>\n"
            + "        <xs:simpleType>\n"
            + "            <xs:restriction base='string'>\n"
            + "                <xs:pattern value='([^\\s]+\\s)?[^\\s]+'/>\n"
            + "            </xs:restriction>\n"
            + "        </xs:simpleType>\n"
            + "    </xs:union>\n"
            + "</xs:simpleType>\n"
            + "<xs:simpleType name='ByteOrderType'>\n"
            + "     <xs:restriction base='string'>\n"
            + "        <xs:enumeration value='BigEndian'/>\n"
            + "        <xs:enumeration value='LittleEndian'/>\n"
            + "     </xs:restriction>\n"
            + "</xs:simpleType>\n"
            + "<xs:simpleType name='HexByteArrayType'>\n"
            + "    <xs:restriction base='string'>\n"
            + "        <xs:pattern value=' (\\s*[0-9a-fA-F][0-9a-fA-F])*\\s*'/>\n"
            + "    </xs:restriction>\n"
            + "</xs:simpleType>\n"
            + "<xs:attribute name='uid' type='reg:TargetType'/>\n"
            + "<xs:attribute name='byteOrder' type='reg:ByteOrderType'/>\n"
            + "<xs:attribute name='stream' type='ENTITY'/>\n"
            + "<xs:attribute name='actualType' type='reg:TargetType'/>\n"
            + "<xs:attribute name='escaped' type='xs:boolean'/>\n"
            + "<xs:attribute name='path' type='xs:string'/>\n"
            + "</xs:schema>\n";

    private final static String XMLNS_NS = "http://www.w3.org/2000/xmlns/";

    private String createQName(URI uri, String name) {
        return this.prefixes.getPrefixOrCreate(uri) + ":" + name;
    }

    public Document xmlSchemaFromDictionary(MetaDictionary dict) throws ParserConfigurationException, KLVException, RuleException, SAXException, IOException, URISyntaxException {

        /* reset namespace prefixes */
        this.prefixes.clear();

        /* create the DOM from the STD_DECL template */
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.newDocument();

        doc.setXmlStandalone(true);

        Element schema = doc.createElementNS(XSD_NS, "xs:schema");
        schema.setAttribute("targetNamespace", dict.getSchemeURI().toString());
        schema.setAttributeNS(XMLNS_NS, "xmlns:reg", REGXML_NS);
        schema.setAttributeNS(XMLNS_NS, "xmlns:xlink", "http://www.w3.org/1999/xlink");
        schema.setAttribute("elementFormDefault", "qualified");
        schema.setAttribute("attributeFormDefault", "unqualified");
        doc.appendChild(schema);

        Element importelem = doc.createElementNS(XSD_NS, "xs:import");
        importelem.setAttribute("namespace", "http://www.w3.org/1999/xlink");
        importelem.setAttribute("schemaLocation", "http://www.w3.org/1999/xlink.xsd");
        doc.getDocumentElement().appendChild(importelem);

        importelem = doc.createElementNS(XSD_NS, "xs:import");
        importelem.setAttribute("namespace", REGXML_NS);
        doc.getDocumentElement().appendChild(importelem);

        for (Definition definition : dict.getDefinitions()) {

            if (definition instanceof ClassDefinition) {

                applyRule4(doc.getDocumentElement(), (ClassDefinition) definition);

            } else if (definition instanceof PropertyDefinition) {

                applyRule5(doc.getDocumentElement(), (PropertyDefinition) definition);

            } else {
                applyRule6(doc.getDocumentElement(), definition);
            }
        }

        /* TODO: hack to clean-up namespace prefixes */
        for (URI uri : prefixes.getURIs()) {

            doc.getDocumentElement().setAttributeNS(
                    "http://www.w3.org/2000/xmlns/",
                    "xmlns:" + prefixes.getPrefixOrCreate(uri),
                    uri.toString()
            );

            if (!uri.equals(dict.getSchemeURI())) {
                importelem = doc.createElementNS(XSD_NS, "xs:import");
                importelem.setAttribute("namespace", uri.toString());
                doc.getDocumentElement().insertBefore(importelem, doc.getDocumentElement().getFirstChild());

            }
        }

        return doc;
    }

    public DefinitionResolver getDefinitionResolver() {
        return resolver;
    }

    public void setDefinitionResolver(DefinitionResolver resolver) {
        this.resolver = resolver;
    }

    void applyRule4(Element root, ClassDefinition definition) throws RuleException {

        /*
         <element name=”{name}” [abstract=”true”]?>
         <complexType>
         <complexContent>
         <all>
         Apply rule 4.1
         </all>
         [<attribute ref=”reg:uid” use=”required”/>]?
         <attribute ref="reg:path" use="optional"/>
         </complexContent>
         </complexType>
         </element>

         */
        Element element = root.getOwnerDocument().createElementNS(XSD_NS, "xs:element");
        element.setAttribute("name", definition.getSymbol());
        if (!definition.isConcrete()) {
            element.setAttribute("abstract", "true");
        }
        root.appendChild(element);

        Element complexType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:complexType");
        element.appendChild(complexType);

        Element all = root.getOwnerDocument().createElementNS(XSD_NS, "xs:all");
        complexType.appendChild(all);

        /* TODO: support reg:uid and reg:path */
        boolean hasUID = false;
        ClassDefinition cdef = definition;

        while (cdef != null) {

            for (AUID auid : resolver.getMembersOf(cdef)) {

                PropertyDefinition pdef
                        = (PropertyDefinition) resolver.getDefinition(auid);

                element = root.getOwnerDocument().createElementNS(XSD_NS, "xs:element");
                element.setAttribute("ref", createQName(pdef.getNamespace(), pdef.getSymbol()));

                if (pdef.isOptional() || pdef.getIdentification().equals(ObjectClass_AUID)) {
                    element.setAttribute("minOccurs", "0");
                }

                if (hasUID == false && pdef.isUniqueIdentifier()) {
                    hasUID = true;
                }

                all.appendChild(element);
            }

            if (cdef.getParentClass() != null) {
                cdef = (ClassDefinition) resolver.getDefinition(cdef.getParentClass());
            } else {
                cdef = null;
            }

        }

        Element attribute = root.getOwnerDocument().createElementNS(XSD_NS, "xs:attribute");
        attribute.setAttribute("ref", "reg:uid");
        attribute.setAttribute("use", "required");
        complexType.appendChild(attribute);

    }

    void applyRule5(Element root, PropertyDefinition definition) throws RuleException {

        Element elem = root.getOwnerDocument().createElementNS(XSD_NS, "xs:element");
        elem.setAttribute("name", definition.getSymbol());

        if (definition.getIdentification().equals(ByteOrder_AUID)) {

            /* rule 5.1 */
            elem.setAttribute("type", "reg:ByteOrderType");
        } else {
            Definition typedef = resolver.getDefinition(definition.getType());
            elem.setAttribute("type", createQName(typedef.getNamespace(), typedef.getSymbol()));
        }

        root.appendChild(elem);
    }

    void applyRule6(Element element, Definition definition) throws RuleException {

        if (definition instanceof CharacterTypeDefinition) {

            applyRule6_1(element, (CharacterTypeDefinition) definition);

            applyRule6Sub2(element, definition);

        } else if (definition instanceof EnumerationTypeDefinition) {

            applyRule6_2(element, (EnumerationTypeDefinition) definition);

            applyRule6Sub2(element, definition);

        } else if (definition instanceof ExtendibleEnumerationTypeDefinition) {

            applyRule6_3(element, (ExtendibleEnumerationTypeDefinition) definition);

            applyRule6Sub2(element, definition);

        } else if (definition instanceof FixedArrayTypeDefinition) {

            applyRule6_4(element, (FixedArrayTypeDefinition) definition);

            applyRule6Sub2(element, definition);

        } else if (definition instanceof IndirectTypeDefinition) {

            applyRule6_5(element, (IndirectTypeDefinition) definition);

        } else if (definition instanceof IntegerTypeDefinition) {

            applyRule6_6(element, (IntegerTypeDefinition) definition);

            applyRule6Sub2(element, definition);

        } else if (definition instanceof OpaqueTypeDefinition) {

            applyRule6_7(element, (OpaqueTypeDefinition) definition);

        } else if (definition instanceof RecordTypeDefinition) {

            applyRule6_8(element, (RecordTypeDefinition) definition);

            applyRule6Sub2(element, definition);

        } else if (definition instanceof RenameTypeDefinition) {

            applyRule6_9(element, (RenameTypeDefinition) definition);

            /* need to check if rename type works */
            applyRule6Sub2(element, definition);

        } else if (definition instanceof SetTypeDefinition) {

            applyRule6_10(element, (SetTypeDefinition) definition);

            applyRule6Sub2(element, definition);

        } else if (definition instanceof StreamTypeDefinition) {

            applyRule6_11(element, (StreamTypeDefinition) definition);

            applyRule6Sub2(element, definition);

        } else if (definition instanceof StringTypeDefinition) {

            applyRule6_12(element, (StringTypeDefinition) definition);

            applyRule6Sub2(element, definition);

        } else if (definition instanceof StrongReferenceTypeDefinition) {

            applyRule6_13(element, (StrongReferenceTypeDefinition) definition);

            applyRule6Sub2(element, definition);

        } else if (definition instanceof VariableArrayTypeDefinition) {

            applyRule6_14(element, (VariableArrayTypeDefinition) definition);

            applyRule6Sub2(element, definition);

        } else if (definition instanceof WeakReferenceTypeDefinition) {

            applyRule6_15(element, (WeakReferenceTypeDefinition) definition);

            applyRule6Sub2(element, definition);

        } else {

            throw new RuleException("Illegage Definition in Rule 5.");

        }

    }

    void applyRule6Sub2(Element root, Definition definition) throws RuleException {
        Element element = root.getOwnerDocument().createElementNS(XSD_NS, "xs:element");
        element.setAttribute("name", definition.getSymbol());
        element.setAttribute("type", createQName(definition.getNamespace(), definition.getSymbol()));
        root.appendChild(element);

    }

    void applyRule6_1(Element root, CharacterTypeDefinition definition) throws RuleException {

        /*
         <complexType name=”{name}”>
         <simpleContent>
         <extension base=”string”>
         <attribute ref=”reg:escaped” use=”optional”/>
         </extension>
         </simpleContent>
         </complexType>
         */
        Element complexType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:complexType");
        complexType.setAttribute("name", definition.getSymbol());
        root.appendChild(complexType);

        Element simpleContent = root.getOwnerDocument().createElementNS(XSD_NS, "xs:simpleContent");
        complexType.appendChild(simpleContent);

        Element extension = root.getOwnerDocument().createElementNS(XSD_NS, "xs:extension");
        extension.setAttribute("base", "xs:string");
        simpleContent.appendChild(extension);

        Element attribute = root.getOwnerDocument().createElementNS(XSD_NS, "xs:attribute");
        attribute.setAttribute("ref", "reg:escaped");
        attribute.setAttribute("use", "optional");
        extension.appendChild(attribute);

    }

    void applyRule6_2(Element root, EnumerationTypeDefinition definition) throws RuleException {

        /*
         <simpleType name=”{name}”>
         <restriction base=”token”>
         For each enumeration element
         <enumeration value=”{enum name}”/>
         </restriction>
         </simpleType>

         */
        Element simpleType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:simpleType");
        simpleType.setAttribute("name", definition.getSymbol());
        root.appendChild(simpleType);

        Element restriction = root.getOwnerDocument().createElementNS(XSD_NS, "xs:restriction");
        restriction.setAttribute("base", "xs:token");
        simpleType.appendChild(restriction);

        for (EnumerationTypeDefinition.Element e : definition.getElements()) {
            Element enumeration = root.getOwnerDocument().createElementNS(XSD_NS, "xs:enumeration");
            enumeration.setAttribute("value", e.getName());
            restriction.appendChild(enumeration);
        }

    }

    void applyRule6_3(Element root, ExtendibleEnumerationTypeDefinition definition) throws RuleException {

        /*
         BUG: ST 2001-1 does not allow arbitrary AUIDs
         */
        Element simpleType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:simpleType");
        simpleType.setAttribute("name", definition.getSymbol());

        Element restriction = root.getOwnerDocument().createElementNS(XSD_NS, "xs:restriction");
        restriction.setAttribute("base", "reg:AUID");

        root.appendChild(simpleType).appendChild(restriction);

    }

    void applyRule6_4(Element root, FixedArrayTypeDefinition definition) throws RuleException {

        /*
         Apply one of the following rules:
         1.	rule 6.4.1 if the elements of the fixed array have a Strong Object Reference base type category
         2.	otherwise rule 6.4.2
         */
        Definition elemdef = resolver.getDefinition(definition.getElementType());

        if (elemdef instanceof StrongReferenceTypeDefinition) {

            /* Rule 6.4.1 */

            /*
             <complexType name=”{name}”>
             <choice minOccurs=”{len}” maxOccurs=”{len}”>
             if the referenced class is concrete:
             <element ref=”{referenced class name}”/>
             for all concrete sub-classes:
             <element ref=”{referenced sub-class name}”/>
             </choice>
             </complexType>
             */
            Element complexType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:complexType");
            complexType.setAttribute("name", definition.getSymbol());
            root.appendChild(complexType);

            Element choice = root.getOwnerDocument().createElementNS(XSD_NS, "xs:choice");
            choice.setAttribute("minOccurs", Integer.toString(definition.getElementCount()));
            choice.setAttribute("maxOccurs", Integer.toString(definition.getElementCount()));
            complexType.appendChild(choice);

            ClassDefinition parent
                    = (ClassDefinition) resolver.getDefinition(
                            ((StrongReferenceTypeDefinition) elemdef).getReferenceType()
                    );

            applyRule6_4_1a(choice, parent);

        } else {

            /* Rule 6.4.2 */
            /*
             <complexType name=”{name}”>
             <sequence>
             <element ref=”{type element name}” minOccurs=”{len}” maxOccurs=”{len}”/>
             </sequence>
             </complexType>
             */
            Element complexType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:complexType");
            complexType.setAttribute("name", definition.getSymbol());
            root.appendChild(complexType);

            Element sequence = root.getOwnerDocument().createElementNS(XSD_NS, "xs:sequence");
            complexType.appendChild(sequence);

            Element element = root.getOwnerDocument().createElementNS(XSD_NS, "xs:element");
            element.setAttribute("minOccurs", Integer.toString(definition.getElementCount()));
            element.setAttribute("maxOccurs", Integer.toString(definition.getElementCount()));
            element.setAttribute("ref", createQName(elemdef.getNamespace(), elemdef.getSymbol()));
            sequence.appendChild(element);

        }

    }

    void applyRule6_4_1a(Element root, ClassDefinition cdef) throws RuleException {

        if (cdef != null) {

            if (cdef.isConcrete()) {
                Element element = root.getOwnerDocument().createElementNS(XSD_NS, "xs:element");
                element.setAttribute("ref", createQName(cdef.getNamespace(), cdef.getSymbol()));
                root.appendChild(element);
            }

            for (AUID auid : resolver.getSubclassesOf(cdef)) {
                ClassDefinition child
                        = (ClassDefinition) resolver.getDefinition(auid);

                applyRule6_4_1a(root, child);
            }

        }

    }

    void applyRule6_5(Element root, IndirectTypeDefinition definition) throws RuleException {

        /*
         <complexType name=”{name}”>
         <complexContent mixed=”true”>
         <restriction base=”anyType”>
         <sequence>
         <any minOccurs=”0” maxOccurs=”unbounded” processContents=”skip”/>
         <sequence>
         <attribute ref=”reg:actualType” use=”required”/>
         <attribute ref=”reg:escaped” use=”optional”/>
         </restriction>
         </complexContent>
         </complexType>

         */
        Element complexType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:complexType");
        complexType.setAttribute("name", definition.getSymbol());
        root.appendChild(complexType);

        Element complexContent = root.getOwnerDocument().createElementNS(XSD_NS, "xs:complexContent");
        complexType.appendChild(complexContent);

        Element restriction = root.getOwnerDocument().createElementNS(XSD_NS, "xs:restriction");
        restriction.setAttribute("base", "xs:anyType");
        complexContent.appendChild(restriction);

        Element sequence = root.getOwnerDocument().createElementNS(XSD_NS, "xs:sequence");
        restriction.appendChild(sequence);

        Element any = root.getOwnerDocument().createElementNS(XSD_NS, "xs:any");
        any.setAttribute("minOccurs", "0");
        any.setAttribute("maxOccurs", "unbounded");
        sequence.appendChild(any);

        Element attribute = root.getOwnerDocument().createElementNS(XSD_NS, "xs:attribute");
        attribute.setAttribute("ref", "reg:actualType");
        attribute.setAttribute("use", "required");
        restriction.appendChild(attribute);

        attribute = root.getOwnerDocument().createElementNS(XSD_NS, "xs:attribute");
        attribute.setAttribute("ref", "reg:escaped");
        attribute.setAttribute("use", "optional");
        restriction.appendChild(attribute);

    }

    void applyRule6_6(Element root, IntegerTypeDefinition definition) throws RuleException {

        /*
         <simpleType name=”{name}”>
         <union>
         <simpleType>
         <restriction base=”{XSDL integer type name}”/>
         </simpleType>
         <simpleType>
         <restriction base=”string”>
         <pattern value=”{pattern}”/>
         </restriction>
         </simpleType>
         </union>
         </simpleType>

         */
        String typename = "ERROR";
        String intpattern = "ERROR";

        switch (definition.getSize()) {
            case ONE:
                typename = definition.isSigned() ? "xs:byte" : "xs:unsignedByte";
                intpattern = "0x[0-9a-fA-F]{1,2}";
                break;
            case TWO:
                typename = definition.isSigned() ? "xs:short" : "xs:unsignedShort";
                intpattern = "0x[0-9a-fA-F]{1,4}";
                break;
            case FOUR:
                typename = definition.isSigned() ? "xs:int" : "xs:unsignedInt";
                intpattern = "0x[0-9a-fA-F]{1,8}";
                break;
            case EIGHT:
                typename = definition.isSigned() ? "xs:long" : "xs:unsignedLong";
                intpattern = "0x[0-9a-fA-F]{1,16}";
                break;
        }

        Element simpleType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:simpleType");
        simpleType.setAttribute("name", definition.getSymbol());
        root.appendChild(simpleType);

        Element union = root.getOwnerDocument().createElementNS(XSD_NS, "xs:union");
        simpleType.appendChild(union);

        Element simpleType2 = root.getOwnerDocument().createElementNS(XSD_NS, "xs:simpleType");
        union.appendChild(simpleType2);

        Element restriction = root.getOwnerDocument().createElementNS(XSD_NS, "xs:restriction");
        restriction.setAttribute("base", typename);
        simpleType2.appendChild(restriction);

        Element simpleType3 = root.getOwnerDocument().createElementNS(XSD_NS, "xs:simpleType");
        union.appendChild(simpleType3);

        Element restriction2 = root.getOwnerDocument().createElementNS(XSD_NS, "xs:restriction");
        restriction2.setAttribute("base", "xs:string");
        simpleType3.appendChild(restriction2);

        Element pattern = root.getOwnerDocument().createElementNS(XSD_NS, "xs:pattern");
        pattern.setAttribute("value", intpattern);
        restriction2.appendChild(pattern);

    }

    void applyRule6_7(Element root, OpaqueTypeDefinition definition) throws RuleException {
        Element complexType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:complexType");
        complexType.setAttribute("name", definition.getSymbol());
        root.appendChild(complexType);

        Element simpleContent = root.getOwnerDocument().createElementNS(XSD_NS, "xs:simpleContent");
        complexType.appendChild(simpleContent);

        Element extension = root.getOwnerDocument().createElementNS(XSD_NS, "xs:extension");
        extension.setAttribute("base", "reg:HexByteArrayType");
        simpleContent.appendChild(extension);

        Element attribute = root.getOwnerDocument().createElementNS(XSD_NS, "xs:attribute");
        attribute.setAttribute("ref", "reg:actualType");
        attribute.setAttribute("use", "required");
        extension.appendChild(attribute);

        attribute = root.getOwnerDocument().createElementNS(XSD_NS, "xs:attribute");
        attribute.setAttribute("ref", "reg:byteOrder");
        attribute.setAttribute("use", "required");
        extension.appendChild(attribute);
    }

    void applyRule6_8(Element root, RecordTypeDefinition definition) throws RuleException {

        /*
         Apply one of the following:
         1.	Apply rule 6.8.1 for types with well known string representations, namely AUID, DateStruct, PackageID, Rational, TimeStruct, TimeStamp and VersionType
         2.	Otherwise:
         <complexType name=”{name}”>
         <sequence>
         for each record member
         <element name=”{member name}” type=”{member type}”/>
         </sequence>
         </complexType>

         */
        if (definition.getIdentification().equals(AUID_AUID)) {
            /*
             <simpleType name="AUID">
             <restriction base="xs:anyURI">
             <pattern 
             value="urn:smpte:ul:([0-9a-fA-F]{8}\.){3}[0-9a-fA-F]{8}"/>
             <pattern 
             value="urn:uuid:[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}"/>
             </restriction>
             </simpleType>

             */

            Element simpleType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:simpleType");
            simpleType.setAttribute("name", "AUID");
            root.appendChild(simpleType);

            Element restriction = root.getOwnerDocument().createElementNS(XSD_NS, "xs:restriction");
            restriction.setAttribute("base", "xs:anyURI");
            simpleType.appendChild(restriction);

            Element pattern = root.getOwnerDocument().createElementNS(XSD_NS, "xs:pattern");
            pattern.setAttribute("value", "urn:smpte:ul:([0-9a-fA-F]{8}\\.){3}[0-9a-fA-F]{8}");
            restriction.appendChild(pattern);

            pattern = root.getOwnerDocument().createElementNS(XSD_NS, "xs:pattern");
            pattern.setAttribute("value", "urn:uuid:[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}");
            restriction.appendChild(pattern);

        } else if (definition.getIdentification().equals(UUID_AUID)) {
            /*
             <simpleType name="AUID">
             <restriction base="xs:anyURI">
             <pattern 
             value="urn:smpte:ul:([0-9a-fA-F]{8}\.){3}[0-9a-fA-F]{8}"/>
             <pattern 
             value="urn:uuid:[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}"/>
             </restriction>
             </simpleType>

             */

            Element simpleType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:simpleType");
            simpleType.setAttribute("name", "UUID");
            root.appendChild(simpleType);

            Element restriction = root.getOwnerDocument().createElementNS(XSD_NS, "xs:restriction");
            restriction.setAttribute("base", "xs:anyURI");
            simpleType.appendChild(restriction);

            Element pattern = root.getOwnerDocument().createElementNS(XSD_NS, "xs:pattern");
            pattern.setAttribute("value", "urn:uuid:[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}");
            restriction.appendChild(pattern);

        } else if (definition.getIdentification().equals(DateStruct_AUID)) {

            /*
             <simpleType name="DateStruct">
             <union>
             <simpleType>
             <restriction base="date">
             <pattern value=".+(((\+|\-)\d\d:\d\d)|Z)"/>
             </restriction>
             </simpleType>
             <simpleType>
             <restriction base="xs:string">
             <enumeration value="0000-00-00Z"/>
             </restriction>
             </simpleType>
             </union>
             </simpleType>

            
             */
            Element simpleType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:simpleType");
            simpleType.setAttribute("name", "DateStruct");
            root.appendChild(simpleType);

            Element union = root.getOwnerDocument().createElementNS(XSD_NS, "xs:union");
            simpleType.appendChild(union);

            simpleType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:simpleType");
            union.appendChild(simpleType);

            Element restriction = root.getOwnerDocument().createElementNS(XSD_NS, "xs:restriction");
            restriction.setAttribute("base", "xs:date");
            simpleType.appendChild(restriction);

            Element pattern = root.getOwnerDocument().createElementNS(XSD_NS, "xs:pattern");
            pattern.setAttribute("value", ".+(((\\+|\\-)\\d\\d:\\d\\d)|Z)");
            restriction.appendChild(pattern);

            simpleType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:simpleType");
            union.appendChild(simpleType);

            restriction = root.getOwnerDocument().createElementNS(XSD_NS, "xs:restriction");
            restriction.setAttribute("base", "xs:string");
            simpleType.appendChild(restriction);

            Element enumeration = root.getOwnerDocument().createElementNS(XSD_NS, "xs:enumeration");
            enumeration.setAttribute("value", "0000-00-00Z");
            restriction.appendChild(enumeration);

        } else if (definition.getIdentification().equals(PackageID_AUID)) {
            /*
             <simpleType name="PackageIDType">
             <restriction base="string">
             <pattern value=" urn:smpte:umid:([0-9a-fA-F]{8}\.){7}[0-9a-fA-F]{8}"/>
             </restriction>
             </simpleType>

             */

            Element simpleType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:simpleType");
            simpleType.setAttribute("name", "PackageIDType");
            root.appendChild(simpleType);

            Element restriction = root.getOwnerDocument().createElementNS(XSD_NS, "xs:restriction");
            restriction.setAttribute("base", "xs:string");
            simpleType.appendChild(restriction);

            Element pattern = root.getOwnerDocument().createElementNS(XSD_NS, "xs:pattern");
            pattern.setAttribute("value", "urn:smpte:umid:([0-9a-fA-F]{8}\\.){7}[0-9a-fA-F]{8}");
            restriction.appendChild(pattern);

        } else if (definition.getIdentification().equals(Rational_AUID)) {

            Element simpleType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:simpleType");
            simpleType.setAttribute("name", "Rational");
            root.appendChild(simpleType);

            Element restriction = root.getOwnerDocument().createElementNS(XSD_NS, "xs:restriction");
            restriction.setAttribute("base", "xs:string");
            simpleType.appendChild(restriction);

            Element pattern = root.getOwnerDocument().createElementNS(XSD_NS, "xs:pattern");
            pattern.setAttribute("value", "\\-?\\d{1,10}(/\\-?\\d{1,10})?");
            restriction.appendChild(pattern);

        } else if (definition.getIdentification().equals(TimeStruct_AUID)) {

            Element simpleType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:simpleType");
            simpleType.setAttribute("name", "TimeStruct");
            root.appendChild(simpleType);

            Element union = root.getOwnerDocument().createElementNS(XSD_NS, "xs:union");
            simpleType.appendChild(union);

            simpleType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:simpleType");
            union.appendChild(simpleType);

            Element restriction = root.getOwnerDocument().createElementNS(XSD_NS, "xs:restriction");
            restriction.setAttribute("base", "xs:time");
            simpleType.appendChild(restriction);

            Element pattern = root.getOwnerDocument().createElementNS(XSD_NS, "xs:pattern");
            pattern.setAttribute("value", ".+(((\\+|\\-)\\d\\d:\\d\\d)|Z)");
            restriction.appendChild(pattern);

            simpleType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:simpleType");
            union.appendChild(simpleType);

            restriction = root.getOwnerDocument().createElementNS(XSD_NS, "xs:restriction");
            restriction.setAttribute("base", "xs:string");
            simpleType.appendChild(restriction);

            Element enumeration = root.getOwnerDocument().createElementNS(XSD_NS, "xs:enumeration");
            enumeration.setAttribute("value", "00:00:00Z");
            restriction.appendChild(enumeration);

        } else if (definition.getIdentification().equals(TimeStamp_AUID)) {

            Element simpleType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:simpleType");
            simpleType.setAttribute("name", "TimeStamp");
            root.appendChild(simpleType);

            Element union = root.getOwnerDocument().createElementNS(XSD_NS, "xs:union");
            simpleType.appendChild(union);

            simpleType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:simpleType");
            union.appendChild(simpleType);

            Element restriction = root.getOwnerDocument().createElementNS(XSD_NS, "xs:restriction");
            restriction.setAttribute("base", "xs:dateTime");
            simpleType.appendChild(restriction);

            Element pattern = root.getOwnerDocument().createElementNS(XSD_NS, "xs:pattern");
            pattern.setAttribute("value", ".+(((\\+|\\-)\\d\\d:\\d\\d)|Z)");
            restriction.appendChild(pattern);

            simpleType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:simpleType");
            union.appendChild(simpleType);

            restriction = root.getOwnerDocument().createElementNS(XSD_NS, "xs:restriction");
            restriction.setAttribute("base", "xs:string");
            simpleType.appendChild(restriction);

            Element enumeration = root.getOwnerDocument().createElementNS(XSD_NS, "xs:enumeration");
            enumeration.setAttribute("value", "0000-00-00T00:00:00Z");
            restriction.appendChild(enumeration);

        } else if (definition.getIdentification().equals(VersionType_AUID)) {

            Element simpleType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:simpleType");
            simpleType.setAttribute("name", "VersionType");
            root.appendChild(simpleType);

            Element restriction = root.getOwnerDocument().createElementNS(XSD_NS, "xs:restriction");
            restriction.setAttribute("base", "xs:string");
            simpleType.appendChild(restriction);

            Element pattern = root.getOwnerDocument().createElementNS(XSD_NS, "xs:pattern");
            pattern.setAttribute("value", "\\-?\\d{1,3}\\.\\-?\\d{1,3}");
            restriction.appendChild(pattern);

        } else {

            /*
             <complexType name=”{name}”>
             <sequence>
             for each record member
             <element name=”{member name}” type=”{member type}”/>
             </sequence>
             </complexType>

             */
            Element complexType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:complexType");
            complexType.setAttribute("name", definition.getSymbol());
            root.appendChild(complexType);

            Element sequence = root.getOwnerDocument().createElementNS(XSD_NS, "xs:sequence");
            complexType.appendChild(sequence);

            for (RecordTypeDefinition.Member member : definition.getMembers()) {

                Definition typedef = resolver.getDefinition(member.getType());

                Element element = root.getOwnerDocument().createElementNS(XSD_NS, "xs:element");
                element.setAttribute("name", member.getName());
                element.setAttribute("type", createQName(typedef.getNamespace(), typedef.getSymbol()));

                sequence.appendChild(element);

            }

        }

    }

    void applyRule6_9(Element root, RenameTypeDefinition definition) throws RuleException {

        Definition origtype = resolver.getDefinition(definition.getRenamedType());

        Element dummy = root.getOwnerDocument().createElement("dummy");

        applyRule6(dummy, origtype);

        if ("simpleType".equals(dummy.getFirstChild().getLocalName())) {

            Element simpleType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:simpleType");
            simpleType.setAttribute("name", definition.getSymbol());
            root.appendChild(simpleType);

            Element restriction = root.getOwnerDocument().createElementNS(XSD_NS, "xs:restriction");
            restriction.setAttribute("base", createQName(origtype.getNamespace(), origtype.getSymbol()));
            simpleType.appendChild(restriction);

        } else {
            Element complexType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:complexType");
            complexType.setAttribute("name", definition.getSymbol());
            root.appendChild(complexType);

            Element complexContent = root.getOwnerDocument().createElementNS(XSD_NS, "xs:complexContent");
            complexType.appendChild(complexContent);

            Element extension = root.getOwnerDocument().createElementNS(XSD_NS, "xs:extension");
            extension.setAttribute("base", createQName(origtype.getNamespace(), origtype.getSymbol()));
            complexContent.appendChild(extension);
        }
    }

    void applyRule6_10(Element root, SetTypeDefinition definition) throws RuleException {
        Definition elemdef = resolver.getDefinition(definition.getElementType());

        if (elemdef instanceof StrongReferenceTypeDefinition) {

            Element complexType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:complexType");
            complexType.setAttribute("name", definition.getSymbol());
            root.appendChild(complexType);

            Element choice = root.getOwnerDocument().createElementNS(XSD_NS, "xs:choice");
            choice.setAttribute("minOccurs", "0");
            choice.setAttribute("maxOccurs", "unbounded");
            complexType.appendChild(choice);

            ClassDefinition parent
                    = (ClassDefinition) resolver.getDefinition(
                            ((StrongReferenceTypeDefinition) elemdef).getReferenceType()
                    );

            applyRule6_4_1a(choice, parent);

        } else {

            Element complexType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:complexType");
            complexType.setAttribute("name", definition.getSymbol());
            root.appendChild(complexType);

            Element sequence = root.getOwnerDocument().createElementNS(XSD_NS, "xs:sequence");
            complexType.appendChild(sequence);

            Element element = root.getOwnerDocument().createElementNS(XSD_NS, "xs:element");
            element.setAttribute("minOccurs", "0");
            element.setAttribute("maxOccurs", "unbounded");
            element.setAttribute("ref", createQName(elemdef.getNamespace(), elemdef.getSymbol()));
            sequence.appendChild(element);

        }
    }

    void applyRule6_11(Element root, StreamTypeDefinition definition) throws RuleException {

        Element complexType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:complexType");
        complexType.setAttribute("name", definition.getSymbol());
        root.appendChild(complexType);

        Element attribute = root.getOwnerDocument().createElementNS(XSD_NS, "xs:attribute");
        attribute.setAttribute("ref", "reg:stream");
        attribute.setAttribute("use", "optional");
        complexType.appendChild(attribute);

        attribute = root.getOwnerDocument().createElementNS(XSD_NS, "xs:attribute");
        attribute.setAttribute("ref", "xlink:href");
        attribute.setAttribute("use", "optional");
        complexType.appendChild(attribute);

        attribute = root.getOwnerDocument().createElementNS(XSD_NS, "xs:attribute");
        attribute.setAttribute("ref", "reg:byteOrder");
        attribute.setAttribute("use", "optional");
        complexType.appendChild(attribute);
    }

    void applyRule6_12(Element root, StringTypeDefinition definition) throws RuleException {

        Element complexType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:complexType");
        complexType.setAttribute("name", definition.getSymbol());
        root.appendChild(complexType);

        Element simpleContent = root.getOwnerDocument().createElementNS(XSD_NS, "xs:simpleContent");
        complexType.appendChild(simpleContent);

        Element extension = root.getOwnerDocument().createElementNS(XSD_NS, "xs:extension");
        extension.setAttribute("base", "xs:string");
        simpleContent.appendChild(extension);

        Element attribute = root.getOwnerDocument().createElementNS(XSD_NS, "xs:attribute");
        attribute.setAttribute("ref", "reg:escaped");
        attribute.setAttribute("use", "optional");
        extension.appendChild(attribute);

    }

    void applyRule6_13(Element root, StrongReferenceTypeDefinition definition) throws RuleException {
        Element complexType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:complexType");
        complexType.setAttribute("name", definition.getSymbol());
        root.appendChild(complexType);

        Element choice = root.getOwnerDocument().createElementNS(XSD_NS, "xs:choice");
        complexType.appendChild(choice);

        ClassDefinition parent
                = (ClassDefinition) resolver.getDefinition(
                        definition.getReferenceType()
                );

        /* BUG: some SMPTE types reference class 14 groups, e.g. GeographicPolygon */
        if (parent == null) {
            return;
        }

        applyRule6_4_1a(choice, parent);

    }

    void applyRule6_14(Element root, VariableArrayTypeDefinition definition) throws RuleException {

        /*
         Apply one of the following rules:
         1.	rule 6.14.1 if the elements of the array have a Strong Object Reference base type category
         2.	rule 6.14.2 if the element type is Character or the type name contains StringArray
         3.	rule 6.14.3 if the type name is DataValue
         4.	otherwise rule 6.14.4

         */
        Definition elemdef = resolver.getDefinition(definition.getElementType());

        if (elemdef instanceof StrongReferenceTypeDefinition) {

            /* Rule 6.14.1 */

            /*
             <complexType name=”{name}”>
             <choice minOccurs=”0” maxOccurs=”unbounded”>
             if the referenced class is concrete:
             <element ref=”{referenced class name}”/>
             for all concrete sub-classes:
             <element ref=”{referenced sub-class name}”/>
             </choice>
             </complexType>

             */
            Element complexType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:complexType");
            complexType.setAttribute("name", definition.getSymbol());
            root.appendChild(complexType);

            Element choice = root.getOwnerDocument().createElementNS(XSD_NS, "xs:choice");
            choice.setAttribute("minOccurs", "0");
            choice.setAttribute("maxOccurs", "unbounded");
            complexType.appendChild(choice);

            ClassDefinition parent
                    = (ClassDefinition) resolver.getDefinition(
                            ((StrongReferenceTypeDefinition) elemdef).getReferenceType()
                    );

            applyRule6_4_1a(choice, parent);

        } else if (elemdef instanceof CharacterTypeDefinition || elemdef.getSymbol().contains("StringArray")) {

            /* Rule 6.14.2 
             <complexType name="{name}">
             <sequence>
             <element ref=”{referenced string type}” minOccurs="0" maxOccurs="unbounded"/>
             </sequence>
             </complexType>
             */
            Element complexType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:complexType");
            complexType.setAttribute("name", definition.getSymbol());
            root.appendChild(complexType);

            Element sequence = root.getOwnerDocument().createElementNS(XSD_NS, "xs:sequence");
            complexType.appendChild(sequence);

            Element element = root.getOwnerDocument().createElementNS(XSD_NS, "xs:element");
            element.setAttribute("minOccurs", "0");
            element.setAttribute("maxOccurs", "unbounded");
            element.setAttribute("ref", createQName(elemdef.getNamespace(), elemdef.getSymbol()));
            sequence.appendChild(element);

        } else if (elemdef.getSymbol().equals("DataValue")) {

            /* Rule 6.14.3
             <simpleType name="DataValue">
             <restriction base="reg:HexByteArrayType"/>
             </simpleType>

            
             */
            Element simpleType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:simpleType");
            simpleType.setAttribute("name", "DataValue");
            root.appendChild(simpleType);

            Element restriction = root.getOwnerDocument().createElementNS(XSD_NS, "xs:restriction");
            restriction.setAttribute("base", "reg:HexByteArrayType");
            simpleType.appendChild(restriction);

        } else {

            /* Rule 6.14.4 */
            /*
             <complexType name=”{name}”>
             <sequence>
             <element ref=”{type element name}” minOccurs=”0” maxOccurs=”unbounded”/>
             </sequence>
             </complexType>

             */
            Element complexType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:complexType");
            complexType.setAttribute("name", definition.getSymbol());
            root.appendChild(complexType);

            Element sequence = root.getOwnerDocument().createElementNS(XSD_NS, "xs:sequence");
            complexType.appendChild(sequence);

            Element element = root.getOwnerDocument().createElementNS(XSD_NS, "xs:element");
            element.setAttribute("minOccurs", "0");
            element.setAttribute("maxOccurs", "unbounded");
            element.setAttribute("ref", createQName(elemdef.getNamespace(), elemdef.getSymbol()));
            sequence.appendChild(element);

        }

    }

    void applyRule6_15(Element root, WeakReferenceTypeDefinition definition) throws RuleException {
        Element simpleType = root.getOwnerDocument().createElementNS(XSD_NS, "xs:simpleType");
        simpleType.setAttribute("name", definition.getSymbol());
        root.appendChild(simpleType);

        Element restriction = root.getOwnerDocument().createElementNS(XSD_NS, "xs:restriction");
        restriction.setAttribute("base", "reg:TargetType");
        simpleType.appendChild(restriction);
    }

    public static class RuleException extends Exception {

        public RuleException(Throwable t) {
            super(t);
        }

        public RuleException(String msg) {
            super(msg);
        }

    }

}
