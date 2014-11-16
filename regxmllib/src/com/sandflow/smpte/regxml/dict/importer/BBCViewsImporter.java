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
package com.sandflow.smpte.regxml.dict.importer;

import com.sandflow.smpte.regxml.definition.CharacterTypeDefinition;
import com.sandflow.smpte.regxml.definition.ClassDefinition;
import com.sandflow.smpte.regxml.definition.Definition;
import com.sandflow.smpte.regxml.dict.DuplicateDefinitionException;
import com.sandflow.smpte.regxml.definition.EnumerationTypeDefinition;
import com.sandflow.smpte.regxml.definition.ExtendibleEnumerationTypeDefinition;
import com.sandflow.smpte.regxml.definition.FixedArrayTypeDefinition;
import com.sandflow.smpte.regxml.definition.IndirectTypeDefinition;
import com.sandflow.smpte.regxml.definition.IntegerTypeDefinition;
import com.sandflow.smpte.regxml.dict.MetaDictionary;
import com.sandflow.smpte.regxml.definition.OpaqueTypeDefinition;
import com.sandflow.smpte.regxml.definition.PropertyDefinition;
import com.sandflow.smpte.regxml.definition.RecordTypeDefinition;
import com.sandflow.smpte.regxml.definition.RecordTypeDefinition.Member;
import com.sandflow.smpte.regxml.definition.RenameTypeDefinition;
import com.sandflow.smpte.regxml.definition.SetTypeDefinition;
import com.sandflow.smpte.regxml.definition.StreamTypeDefinition;
import com.sandflow.smpte.regxml.definition.StringTypeDefinition;
import com.sandflow.smpte.regxml.definition.StrongReferenceTypeDefinition;
import com.sandflow.smpte.regxml.definition.VariableArrayTypeDefinition;
import com.sandflow.smpte.regxml.definition.WeakReferenceTypeDefinition;
import com.sandflow.smpte.util.AUID;
import com.sandflow.smpte.util.xml.AUIDAdapter;
import com.sandflow.smpte.util.UL;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
public class BBCViewsImporter {

    public static class HexIntegerAdapter extends XmlAdapter<String, Integer> {

        public Integer unmarshal(String val) throws Exception {
            return Integer.parseUnsignedInt(val, 16);
        }

        public String marshal(Integer val) throws Exception {

            /* TODO: implement */
            return null;
        }
    }

    @XmlRootElement(name = "result", namespace = "http://htsql.org/2010/xml")
    private static class HTSQLElements {

        @XmlElement(name = "elements_core")
        List<HTSQLElement> elements;
    }

    @XmlRootElement(name = "result", namespace = "http://htsql.org/2010/xml")
    private static class HTSQLGroups {

        @XmlElement(name = "groups_core")
        List<HTSQLGroup> groups;
    }

    @XmlRootElement(name = "result", namespace = "http://htsql.org/2010/xml")
    private static class HTSQLTypes {

        @XmlElement(name = "types_core")
        List<HTSQLType> types;
    }

    private static class HTSQLElement {

        @XmlJavaTypeAdapter(value = AUIDAdapter.class)
        @XmlElement(name = "ul_urn")
        AUID identification;

        @XmlElement(name = "symbol")
        String symbol;

        @XmlElement(name = "definition")
        String description;

        @XmlElement(name = "name")
        String name;

        @XmlElement(name = "type_ul")
        String type;

        @XmlElement(name = "node_or_leaf")
        String node_or_leaf;

    }

    private static class HTSQLGroupChild {

        @XmlJavaTypeAdapter(value = AUIDAdapter.class)
        @XmlElement(name = "contents_item")
        AUID identification;

        @XmlJavaTypeAdapter(value = HexIntegerAdapter.class)
        @XmlElement(name = "contents_local_tag")
        Integer localIdentification = 0;

        @XmlElement(name = "contents_unique_identifier")
        boolean unique;

        @XmlElement(name = "contents_optional")
        boolean optional;

    }

    private static class HTSQLTypesChild {

        @XmlJavaTypeAdapter(value = AUIDAdapter.class)
        @XmlElement(name = "facet_type")
        AUID typeIdentification;

        @XmlElement(name = "facet_symbol")
        String symbol;

        @XmlElement(name = "facet_value")
        String value;

    }

    private static class HTSQLGroup {

        @XmlJavaTypeAdapter(value = AUIDAdapter.class)
        @XmlElement(name = "ul_urn")
        AUID identification;

        @XmlElement(name = "symbol")
        String symbol;

        @XmlElement(name = "definition")
        String description;

        @XmlElement(name = "name")
        String name;

        @XmlElement(name = "concrete")
        boolean concrete;

        @XmlElement(name = "parent_group")
        String parentClass;

        @XmlElement(name = "node_or_leaf")
        String node_or_leaf;
        
        @XmlElement(name = "allowed_klv_syntax")
        String allowed_klv_syntax;

        @XmlElement(name = "groups_core_children")
        List<HTSQLGroupChild> children;

    }

    private static class HTSQLType {

        @XmlJavaTypeAdapter(value = AUIDAdapter.class)
        @XmlElement(name = "ul_urn")
        AUID identification;

        @XmlElement(name = "symbol")
        String symbol;

        @XmlElement(name = "definition")
        String description;

        @XmlElement(name = "name")
        String name;

        @XmlJavaTypeAdapter(value = AUIDAdapter.class)
        @XmlElement(name = "base_type_ul")
        AUID baseTypeIdentification;

        @XmlElement(name = "type_qualifiers")
        String qualifiers;

        @XmlElement(name = "node_or_leaf")
        String node_or_leaf;

        @XmlJavaTypeAdapter(value = AUIDAdapter.class)
        @XmlElement(name = "class_of_objects_referenced")
        AUID reference;

        @XmlEnum(String.class)
        public enum Kind {

            @XmlEnumValue("rename")
            RENAME,
            @XmlEnumValue("integer")
            INTEGER,
            @XmlEnumValue("record")
            RECORD,
            @XmlEnumValue("array")
            ARRAY,
            @XmlEnumValue("indirect")
            INDIRECT,
            @XmlEnumValue("opaque")
            OPAQUE,
            @XmlEnumValue("set")
            SET,
            @XmlEnumValue("stream")
            STREAM,
            @XmlEnumValue("reference")
            REFERENCE,
            @XmlEnumValue("enumeration")
            ENUMERATION,
            @XmlEnumValue("character")
            CHARACTER,
            @XmlEnumValue("string")
            STRING,
            @XmlEnumValue("extendible")
            EXTENDIBLE
        }

        @XmlElement(name = "type_kind")
        Kind kind;

        @XmlElement(name = "types_core_children_via_parent")
        List<HTSQLTypesChild> children;

    }

    public static MetaDictionary fromBBCView(InputStream eis, InputStream gis, InputStream tis) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException, MissingElementDefinitionException, DuplicateDefinitionException, JAXBException, InvalidIdentificationException {

        /* create definition collection */
        HashMap<AUID, Definition> defs = new HashMap<>();

        /* parse the xml documents */
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        dbf.setNamespaceAware(true);

        DocumentBuilder db = dbf.newDocumentBuilder();

        /* retrieve and index all elements */
        HashMap<AUID, HTSQLElement> elements = new HashMap<>();

        Element eroot = db.parse(eis).getDocumentElement();

        HTSQLElements tmp = (HTSQLElements) JAXBContext.newInstance(HTSQLElements.class).createUnmarshaller().unmarshal(eroot);

        for (HTSQLElement element : tmp.elements) {

            /* BUG: some elements have illegal symbols! */
            /*if (element.symbol.equals("Not Specified")
                    || element.symbol.equals("JobFunctionCode")
                    || element.symbol.equals("UTCInstantDateTime")) {
                continue;
            }*/

            elements.put(element.identification, element);

        }

        /* handle groups */
        Element groot = db.parse(gis).getDocumentElement();

        HTSQLGroups groups = (HTSQLGroups) JAXBContext.newInstance(HTSQLGroups.class).createUnmarshaller().unmarshal(groot);

        for (HTSQLGroup group : groups.groups) {

            if ("node".equalsIgnoreCase(group.node_or_leaf)) {
                continue;
            }

            ClassDefinition cdef = new ClassDefinition();

            cdef.setConcrete(group.concrete);

            cdef.setDescription(group.description);

            cdef.setDescription(group.name);

            cdef.setSymbol(group.symbol);
            cdef.setParentClass(AUID.fromURN(group.parentClass));
            cdef.setIdentification(group.identification);

            if (cdef.getIdentification() == null) {
                throw new InvalidIdentificationException();
            }
            
            
            /* BUG: some groups are broken! Skip class 14 and 15 */
            
            if (cdef.getIdentification().asUL().getValueOctet(8) == 14 ||
                    cdef.getIdentification().asUL().getValueOctet(8) == 15) {
                continue;
            }
            
            /* BUG: skip packs */
            
            if (group.allowed_klv_syntax != null && group.allowed_klv_syntax.contains("05"))
                continue;

            if (group.children != null) {

                for (HTSQLGroupChild child : group.children) {
                    PropertyDefinition pdef = new PropertyDefinition();

                    pdef.setOptional(child.optional);
                    pdef.setIdentification(child.identification);
                    pdef.setUniqueIdentifier(child.unique);
                    pdef.setLocalIdentification(child.localIdentification);

                    /* retrieve the element */
                    HTSQLElement element = elements.get(child.identification);

                    if (element == null) {
                        throw new MissingElementDefinitionException("Group ID: "
                                + group.identification
                                + " -> Element ID: "
                                + child.identification);
                    }

                    pdef.setDescription(element.description);

                    pdef.setName(element.name);

                    pdef.setSymbol(element.symbol);

                    pdef.setType(AUID.fromURN(element.type));

                    pdef.setMemberOf(cdef.getIdentification());

                    /* add property definition */
                    defs.put(pdef.getIdentification(), pdef);
                }

            }

            defs.put(cdef.getIdentification(), cdef);
        }

        /* handle types */
        Element troot = db.parse(tis).getDocumentElement();

        HTSQLTypes types = (HTSQLTypes) JAXBContext.newInstance(HTSQLTypes.class).createUnmarshaller().unmarshal(troot);

        /* TODO: missing alias */
        for (HTSQLType type : types.types) {
            if (!"leaf".equalsIgnoreCase(type.node_or_leaf)) {
                continue;
            }

            if (type.kind == null) {
                continue;
            }

            Definition tdef = null;
            
            /* BUG: skip bad UUID type */
            
            if (type.identification.equals(UL.fromURN("urn:smpte:ul:060E2B34.01040101.04011100.00000000"))) continue;
            
            /* end BUG */

            switch (type.kind) {
                case RENAME:

                    tdef = new RenameTypeDefinition();

                    ((RenameTypeDefinition) tdef).setRenamedType(type.baseTypeIdentification);

                    break;

                case INTEGER:

                    tdef = new IntegerTypeDefinition();

                    /* BUG: signed missing! */
                    ((IntegerTypeDefinition) tdef).setSigned(true);

                    int sz = Integer.parseInt(type.qualifiers);

                    switch (sz) {
                        case 1:
                            ((IntegerTypeDefinition) tdef).setSize(IntegerTypeDefinition.Size.ONE);
                            break;
                        case 2:
                            ((IntegerTypeDefinition) tdef).setSize(IntegerTypeDefinition.Size.TWO);
                            break;
                        case 4:
                            ((IntegerTypeDefinition) tdef).setSize(IntegerTypeDefinition.Size.FOUR);
                            break;
                        case 8:
                            ((IntegerTypeDefinition) tdef).setSize(IntegerTypeDefinition.Size.EIGHT);
                            break;
                    }

                    break;

                case RECORD:

                    tdef = new RecordTypeDefinition();

                    for (HTSQLTypesChild tchild : type.children) {
                        Member m = new Member();

                        m.setName(tchild.symbol);
                        m.setType(tchild.typeIdentification);

                        ((RecordTypeDefinition) tdef).addMember(m);
                    }

                    break;
                case ARRAY:

                    if ("fixed".equalsIgnoreCase(type.qualifiers)) {
                        tdef = new FixedArrayTypeDefinition();

                        ((FixedArrayTypeDefinition) tdef).setElementType(type.baseTypeIdentification);

                        // BUG: missing element count
                    } else {
                        tdef = new VariableArrayTypeDefinition();

                        ((VariableArrayTypeDefinition) tdef).setElementType(type.baseTypeIdentification);

                    }

                    break;

                case INDIRECT:
                    tdef = new IndirectTypeDefinition();
                    break;
                case OPAQUE:
                    tdef = new OpaqueTypeDefinition();
                    break;
                case SET:
                    tdef = new SetTypeDefinition();

                    ((SetTypeDefinition) tdef).setElementType(type.baseTypeIdentification);
                    break;
                case STREAM:
                    tdef = new StreamTypeDefinition();
                    break;
                case REFERENCE:
                    if ("weak".equalsIgnoreCase(type.qualifiers)) {
                        tdef = new WeakReferenceTypeDefinition();

                        ((WeakReferenceTypeDefinition) tdef).setReferencedType(type.reference);

                        // BUG: RegXML defines "Target Set"
                    } else {
                        tdef = new StrongReferenceTypeDefinition();

                        ((StrongReferenceTypeDefinition) tdef).setReferenceType(type.reference);

                        /* BUG: class_of_objects_referenced has wrong UL format */
                    }
                    break;
                case ENUMERATION:

                    ArrayList<EnumerationTypeDefinition.Element> celems = new ArrayList<>();

                    if (type.children != null) {

                        for (HTSQLTypesChild tchild : type.children) {
                            EnumerationTypeDefinition.Element m = new EnumerationTypeDefinition.Element();

                            m.setName(tchild.symbol);
                            m.setValue(Integer.decode(tchild.value));

                            // BUG: some enumerations use hex notation
                            celems.add(m);
                        }
                    } else {
                        /* TODO: error handling */
                    }
                    tdef = new EnumerationTypeDefinition(celems);

                    ((EnumerationTypeDefinition) tdef).setElementType(type.baseTypeIdentification);

                    break;
                case CHARACTER:
                    tdef = new CharacterTypeDefinition();
                    break;
                case STRING:
                    tdef = new StringTypeDefinition();

                    ((StringTypeDefinition) tdef).setElementType(type.baseTypeIdentification);
                    break;
                case EXTENDIBLE:

                    ArrayList<ExtendibleEnumerationTypeDefinition.Element> ecelems = new ArrayList<>();

                    for (HTSQLTypesChild tchild : type.children) {
                        ExtendibleEnumerationTypeDefinition.Element m = new ExtendibleEnumerationTypeDefinition.Element();

                        m.setValue(AUID.fromURN(tchild.value));

                        /* TODO: lookup labels */
                        ecelems.add(m);
                    }

                    tdef = new ExtendibleEnumerationTypeDefinition(ecelems);

                    break;
                default:
                /* todo: error handling */
            }

            if (tdef != null) {
                tdef.setIdentification(type.identification);
                tdef.setSymbol(type.symbol);
                tdef.setName(type.name);
                tdef.setDescription(type.description);

                defs.put(tdef.getIdentification(), tdef);
            } else {
                /* todo: error handling */
            }
        }
        
        /* BUG: check for duplicate symbols */
        
        HashSet<String> syms = new HashSet<>();
        long index = 0;
        
        for(Definition def : defs.values()) {
            if (syms.contains(def.getSymbol())) {
                def.setSymbol("dup" + def.getSymbol() + (index++));
            }
            
            syms.add(def.getSymbol());
        }

        /* create dictionary */
        MetaDictionary md;
        try {
            md = new MetaDictionary(new URI("http://www.smpte-ra.org/reg"), defs.values());
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }

        return md;

    }

    public static void main(String args[]) throws FileNotFoundException, SAXException, IOException, ParserConfigurationException, XPathExpressionException, MissingElementDefinitionException, DuplicateDefinitionException, JAXBException, InvalidIdentificationException {

        FileInputStream fe = new FileInputStream("\\\\SERVER\\Business\\sandflow-consulting\\projects\\netflix\\Elements View.xml");
        FileInputStream fg = new FileInputStream("\\\\SERVER\\Business\\sandflow-consulting\\projects\\netflix\\Groups View.xml");
        FileInputStream ft = new FileInputStream("\\\\SERVER\\Business\\sandflow-consulting\\projects\\netflix\\Types View.xml");

        MetaDictionary md = fromBBCView(fe, fg, ft);

        System.out.println(md.getDefinitions().size());

    }

    public static class MissingElementDefinitionException extends Exception {

        public MissingElementDefinitionException(String string) {
            super(string);
        }
    }

    private static class InvalidIdentificationException extends Exception {

        public InvalidIdentificationException() {
        }
    }

}
