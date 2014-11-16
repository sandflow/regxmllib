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

import com.sandflow.smpte.register.ElementEntry;
import com.sandflow.smpte.register.ElementsRegister;
import com.sandflow.smpte.register.importer.ExcelElementsRegister;
import com.sandflow.smpte.register.importer.ExcelGroupsRegister;
import com.sandflow.smpte.register.importer.ExcelTypesRegister;
import com.sandflow.smpte.register.GroupEntry;
import com.sandflow.smpte.register.GroupsRegister;
import com.sandflow.smpte.register.TypeEntry;
import com.sandflow.smpte.register.TypeEntry.Facet;
import com.sandflow.smpte.register.TypesRegister;
import com.sandflow.smpte.util.AUID;
import com.sandflow.smpte.regxml.definition.CharacterTypeDefinition;
import com.sandflow.smpte.regxml.definition.ClassDefinition;
import com.sandflow.smpte.regxml.definition.Definition;
import com.sandflow.smpte.regxml.definition.EnumerationTypeDefinition;
import com.sandflow.smpte.regxml.definition.ExtendibleEnumerationTypeDefinition;
import com.sandflow.smpte.regxml.definition.FixedArrayTypeDefinition;
import com.sandflow.smpte.regxml.definition.IndirectTypeDefinition;
import com.sandflow.smpte.regxml.definition.IntegerTypeDefinition;
import com.sandflow.smpte.regxml.dict.MetaDictionary;
import com.sandflow.smpte.regxml.definition.PropertyDefinition;
import com.sandflow.smpte.regxml.definition.OpaqueTypeDefinition;
import com.sandflow.smpte.regxml.definition.RecordTypeDefinition;
import com.sandflow.smpte.regxml.definition.RecordTypeDefinition.Member;
import com.sandflow.smpte.regxml.definition.RenameTypeDefinition;
import com.sandflow.smpte.regxml.definition.SetTypeDefinition;
import com.sandflow.smpte.regxml.definition.StreamTypeDefinition;
import com.sandflow.smpte.regxml.definition.StringTypeDefinition;
import com.sandflow.smpte.regxml.definition.StrongReferenceTypeDefinition;
import com.sandflow.smpte.regxml.definition.VariableArrayTypeDefinition;
import com.sandflow.smpte.regxml.definition.WeakReferenceTypeDefinition;
import com.sandflow.smpte.util.UL;
import com.sandflow.smpte.util.ExcelCSVParser;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

/**
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
public class XMLRegistryImporter {
    
    private final static Logger LOGGER = Logger.getLogger(ExcelElementsRegister.class.getName());


    public static MetaDictionary fromRegister(TypesRegister tr, GroupsRegister gr, ElementsRegister er) throws Exception {

        /* create definition collection */
        HashMap<AUID, Definition> defs = new HashMap<>();


        /* handle groups */
        for (GroupEntry group : gr.getEntries()) {

            if (group.getKind().equals(GroupEntry.Kind.NODE)) {
                continue;
            }

            /* BUG: some groups are broken! Skip class 14 and 15 */
            if (group.getUL().isClass14() || group.getUL().isClass15()) {
                continue;
            }

            /* BUG: skip non locat set groups */
            if (!group.getKlvSyntax().contains((byte) 0x53)) {
                continue;
            }

            ClassDefinition cdef = new ClassDefinition();

            cdef.setConcrete(group.isConcrete());

            cdef.setDescription(group.getDefinition());

            cdef.setName(group.getName());

            cdef.setSymbol(group.getSymbol());

            if (group.getParent() != null) {
                cdef.setParentClass(new AUID(group.getParent()));
            }

            cdef.setIdentification(new AUID(group.getUL()));

            for (GroupEntry.Record child : group.getContents()) {
                PropertyDefinition pdef = new PropertyDefinition();

                pdef.setOptional(child.getOptional());
                pdef.setIdentification(new AUID(child.getItem()));
                pdef.setUniqueIdentifier(child.getUniqueID());
                pdef.setLocalIdentification((int) (child.getLocalTag() == null ? 0 : child.getLocalTag()));

                /* retrieve the element */
                ElementEntry element = er.getEntryByUL(child.getItem());

                if (element == null) {
                    LOGGER.warning(String.format(
                                    "Undefined Element %s for Group %s",
                                    child.getItem(),
                                    group.getUL()
                            )
                    );
                    
                    continue;
                }

                pdef.setDescription(element.getDefinition());

                pdef.setName(element.getName());

                pdef.setSymbol(element.getSymbol());

                pdef.setType(new AUID(element.getTypeUL()));

                pdef.setMemberOf(cdef.getIdentification());

                /* add property definition */
                defs.put(pdef.getIdentification(), pdef);
            }

            defs.put(cdef.getIdentification(), cdef);
        }

        /* handle types */
        /* TODO: missing alias */
        for (TypeEntry type : tr.getEntries()) {
            if (!type.getKind().equals(TypeEntry.Kind.LEAF)) {
                continue;
            }

            Definition tdef = null;

            /* BUG: skip bad UUID type */
            if (type.getUL().equals(UL.fromURN("urn:smpte:ul:060E2B34.01040101.04011100.00000000"))) {
                continue;
            }

            switch (type.getTypeKind()) {
                case Rename:

                    tdef = new RenameTypeDefinition();

                    ((RenameTypeDefinition) tdef).setRenamedType(new AUID(type.getBaseType()));

                    break;

                case Integer:

                    tdef = new IntegerTypeDefinition();

                    ((IntegerTypeDefinition) tdef).setSigned(type.getTypeQualifiers().contains(TypeEntry.TypeQualifiers.isSigned));

                    switch (type.getTypeSize().intValue()) {
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
                        default:
                            throw new Exception("Illegal Type Size.");

                    }

                    break;

                case Record:

                    tdef = new RecordTypeDefinition();

                    for (TypeEntry.Facet tchild : type.getFacets()) {
                        Member m = new Member();

                        m.setName(tchild.getName());
                        m.setType(new AUID(tchild.getType()));

                        ((RecordTypeDefinition) tdef).addMember(m);
                    }

                    break;
                case Multiple:
                    if (type.getTypeSize().intValue() != 0) {
                        tdef = new FixedArrayTypeDefinition();

                        ((FixedArrayTypeDefinition) tdef).setElementType(new AUID(type.getBaseType()));

                        ((FixedArrayTypeDefinition) tdef).setElementCount(type.getTypeSize().intValue());

                    } else {

                        if (type.getTypeQualifiers().contains(TypeEntry.TypeQualifiers.isIdentified)) {
                            tdef = new SetTypeDefinition();
                            ((SetTypeDefinition) tdef).setElementType(new AUID(type.getBaseType()));
                        } else {
                            tdef = new VariableArrayTypeDefinition();
                            ((VariableArrayTypeDefinition) tdef).setElementType(new AUID(type.getBaseType()));
                        }
                    }

                    break;

                case Indirect:
                    tdef = new IndirectTypeDefinition();
                    break;
                case Opaque:
                    tdef = new OpaqueTypeDefinition();
                    break;
                case Stream:
                    tdef = new StreamTypeDefinition();
                    break;
                case WeakReference:

                    tdef = new WeakReferenceTypeDefinition();

                    if (type.getBaseType() == null) {
                        throw new Exception(
                                String.format(
                                        "Missing base type for Type %s",
                                        type.getUL()
                                )
                        );
                    }

                    ((WeakReferenceTypeDefinition) tdef).setReferencedType(new AUID(type.getBaseType()));

                    for (Facet f : type.getFacets()) {
                        ((WeakReferenceTypeDefinition) tdef).getTargetSet().add(new AUID(f.getUL()));
                    }

                    break;
                case StrongReference:
                    tdef = new StrongReferenceTypeDefinition();

                    ((StrongReferenceTypeDefinition) tdef).setReferenceType(new AUID(type.getBaseType()));

                    /* BUG: class_of_objects_referenced has wrong UL format */
                    break;
                case Enumerated:

                    if (type.getBaseType().equals(UL.fromURN("urn:smpte:ul:060E2B34.01040101.01030100.00000000"))) {
                        ArrayList<ExtendibleEnumerationTypeDefinition.Element> ecelems = new ArrayList<>();

                        for (Facet f : type.getFacets()) {
                            ExtendibleEnumerationTypeDefinition.Element m = new ExtendibleEnumerationTypeDefinition.Element();

                            m.setValue(new AUID(f.getUL()));

                            /* TODO: lookup labels */
                            ecelems.add(m);
                        }

                        tdef = new ExtendibleEnumerationTypeDefinition(ecelems);

                    } else {

                        ArrayList<EnumerationTypeDefinition.Element> celems = new ArrayList<>();

                        for (Facet f : type.getFacets()) {
                            EnumerationTypeDefinition.Element m = new EnumerationTypeDefinition.Element();

                            m.setName(f.getSymbol());
                            m.setValue(Integer.decode(f.getValue()));

                            // BUG: some enumerations use hex notation
                            celems.add(m);
                        }

                        tdef = new EnumerationTypeDefinition(celems);

                        ((EnumerationTypeDefinition) tdef).setElementType(new AUID(type.getBaseType()));
                    }
                    break;
                case Character:
                    tdef = new CharacterTypeDefinition();
                    break;
                case String:
                    tdef = new StringTypeDefinition();

                    ((StringTypeDefinition) tdef).setElementType(new AUID(type.getBaseType()));
                    break;

                default:
                /* todo: error handling */
            }

            if (tdef != null) {
                tdef.setIdentification(new AUID(type.getUL()));
                tdef.setSymbol(type.getSymbol());
                tdef.setName(type.getName());
                tdef.setDescription(type.getDefinition());

                defs.put(tdef.getIdentification(), tdef);
            } else {
                /* todo: error handling */
            }
        }

        /* BUG: check for duplicate symbols */
        HashSet<String> syms = new HashSet<>();
        long index = 0;

        for (Definition def : defs.values()) {
            if (syms.contains(def.getSymbol())) {
                def.setSymbol("dup" + def.getSymbol() + (index++));
            }

            syms.add(def.getSymbol());
        }

        /* create dictionary */
        MetaDictionary md = new MetaDictionary(new URI("http://www.smpte-ra.org/reg"), defs.values());

        return md;

    }

    public static void main(String args[]) throws FileNotFoundException, SAXException, IOException, ParserConfigurationException, XPathExpressionException, MissingElementDefinitionException, JAXBException, InvalidIdentificationException, ExcelCSVParser.SyntaxException, Exception {

        FileInputStream fe = new FileInputStream("\\\\SERVER\\Business\\sandflow-consulting\\projects\\imf\\regxml\\register-format\\input\\elements-smpte-ra-frozen-20140304.2118.csv");
        FileInputStream fg = new FileInputStream("\\\\SERVER\\Business\\sandflow-consulting\\projects\\imf\\regxml\\register-format\\input\\groups-smpte-ra-frozen-20140304.2118.csv");
        FileInputStream ft = new FileInputStream("\\\\SERVER\\Business\\sandflow-consulting\\projects\\imf\\regxml\\register-format\\input\\types-smpte-ra-frozen-20140304.2118.csv");

        ElementsRegister ereg = ExcelElementsRegister.fromXLS(fe);
        GroupsRegister greg = ExcelGroupsRegister.fromXLS(fg);
        TypesRegister treg = ExcelTypesRegister.fromXLS(ft);

        MetaDictionary md = fromRegister(treg, greg, ereg);

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
