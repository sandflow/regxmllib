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
package com.sandflow.smpte.regxml.dict.importers;

import com.sandflow.smpte.register.ElementsRegister;
import com.sandflow.smpte.register.GroupsRegister;
import com.sandflow.smpte.register.TypesRegister;
import com.sandflow.smpte.regxml.dict.MetaDictionaryCollection;
import com.sandflow.smpte.regxml.dict.definitions.CharacterTypeDefinition;
import com.sandflow.smpte.regxml.dict.definitions.ClassDefinition;
import com.sandflow.smpte.regxml.dict.definitions.Definition;
import com.sandflow.smpte.regxml.dict.definitions.EnumerationTypeDefinition;
import com.sandflow.smpte.regxml.dict.definitions.ExtendibleEnumerationTypeDefinition;
import com.sandflow.smpte.regxml.dict.definitions.FixedArrayTypeDefinition;
import com.sandflow.smpte.regxml.dict.definitions.FloatTypeDefinition;
import com.sandflow.smpte.regxml.dict.definitions.IndirectTypeDefinition;
import com.sandflow.smpte.regxml.dict.definitions.IntegerTypeDefinition;
import com.sandflow.smpte.regxml.dict.definitions.LensSerialFloatTypeDefinition;
import com.sandflow.smpte.regxml.dict.definitions.OpaqueTypeDefinition;
import com.sandflow.smpte.regxml.dict.definitions.PropertyAliasDefinition;
import com.sandflow.smpte.regxml.dict.definitions.PropertyDefinition;
import com.sandflow.smpte.regxml.dict.definitions.RecordTypeDefinition;
import com.sandflow.smpte.regxml.dict.definitions.RecordTypeDefinition.Member;
import com.sandflow.smpte.regxml.dict.definitions.RenameTypeDefinition;
import com.sandflow.smpte.regxml.dict.definitions.SetTypeDefinition;
import com.sandflow.smpte.regxml.dict.definitions.StreamTypeDefinition;
import com.sandflow.smpte.regxml.dict.definitions.StringTypeDefinition;
import com.sandflow.smpte.regxml.dict.definitions.StrongReferenceTypeDefinition;
import com.sandflow.smpte.regxml.dict.definitions.VariableArrayTypeDefinition;
import com.sandflow.smpte.regxml.dict.definitions.WeakReferenceTypeDefinition;
import com.sandflow.smpte.regxml.dict.exceptions.DuplicateSymbolException;
import com.sandflow.smpte.util.AUID;
import com.sandflow.smpte.util.UL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * Generates MetaDictionaries from SMPTE Metadata Registers.
 */
public class RegisterImporter {

    private final static Logger LOGGER = Logger.getLogger(RegisterImporter.class.getName());

    /**
     * Generates MetaDictionaries from SMPTE Types, Groups and Elements Registers.
     * 
     * @param tr Types Register
     * @param gr Groups Register
     * @param er Elements Register
     * @return Collection of Metadictionaries 
     * @throws Exception 
     */
    public static MetaDictionaryCollection fromRegister(TypesRegister tr, GroupsRegister gr, ElementsRegister er) throws Exception {

        /* create definition collection */
        ArrayList<Definition> defs = new ArrayList<>();

        /* keep track of definitions to prevent duplicates */
        HashSet<AUID> defIDs = new HashSet<>();
        
        /* Handles Group Entries */
        for (GroupsRegister.Entry group : gr.getEntries()) {

            if (group.getKind().equals(GroupsRegister.Entry.Kind.NODE)) {
                continue;
            }

            /* Skip class 14 and 15 */
            if (group.getUL().isClass14() || group.getUL().isClass15()) {
                continue;
            }

            /* Skip groups that do not have a local set representation locat set groups */
            if (!group.getKlvSyntax().contains((byte) 0x53)) {
                continue;
            }

            ClassDefinition cdef = new ClassDefinition();

            if (group.isConcrete() != null) {
                cdef.setConcrete(group.isConcrete());
            } else {
                cdef.setConcrete(true);
            }

            cdef.setDescription(group.getDefinition());

            cdef.setName(group.getName());

            cdef.setSymbol(group.getSymbol());

            cdef.setNamespace(group.getNamespaceName());

            if (group.getParent() != null) {
                cdef.setParentClass(new AUID(group.getParent()));
            }

            cdef.setIdentification(new AUID(group.getUL()));

            for (GroupsRegister.Entry.Record child : group.getContents()) {

                AUID id = new AUID(child.getItem());

                PropertyDefinition pdef = null;

                if (defIDs.contains(id)) {

                    /* if the property has already been added, e.g. BodySID, create an alias */
                    PropertyAliasDefinition padef = new PropertyAliasDefinition();

                    padef.setOriginalProperty(id);

                    pdef = padef;

                } else {

                    pdef = new PropertyDefinition();

                }

                pdef.setIdentification(id);
                pdef.setOptional(child.getOptional());

                if (child.getUniqueID() != null) {
                    pdef.setUniqueIdentifier(child.getUniqueID());
                }
                pdef.setLocalIdentification((int) (child.getLocalTag() == null ? 0 : child.getLocalTag()));

                /* retrieve the element */
                ElementsRegister.Entry element = er.getEntryByUL(child.getItem());

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

                if (element.getTypeUL() == null) {
                    LOGGER.warning(String.format(
                            "Missing Type UL at Element %s for Group %s",
                            child.getItem(),
                            group.getUL()
                    )
                    );

                    continue;
                }

                pdef.setType(new AUID(element.getTypeUL()));

                pdef.setMemberOf(cdef.getIdentification());

                pdef.setNamespace(element.getNamespaceName());

                defs.add(pdef);
                defIDs.add(pdef.getIdentification());
            }

            defs.add(cdef);
            defIDs.add(cdef.getIdentification());
        }

        /* Handle Types Entries */
        
        for (com.sandflow.smpte.register.TypesRegister.Entry type : tr.getEntries()) {
            if (!type.getKind().equals(com.sandflow.smpte.register.TypesRegister.Entry.Kind.LEAF)) {
                continue;
            }

            Definition tdef = null;

            if (com.sandflow.smpte.register.TypesRegister.Entry.RENAME_TYPEKIND.equals(type.getTypeKind())) {

                tdef = new RenameTypeDefinition();

                ((RenameTypeDefinition) tdef).setRenamedType(new AUID(type.getBaseType()));

            } else if (com.sandflow.smpte.register.TypesRegister.Entry.INTEGER_TYPEKIND.equals(type.getTypeKind())) {

                tdef = new IntegerTypeDefinition();

                ((IntegerTypeDefinition) tdef).setSigned(type.getTypeQualifiers().contains(com.sandflow.smpte.register.TypesRegister.Entry.TypeQualifiers.isSigned));

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

            } else if (com.sandflow.smpte.register.TypesRegister.Entry.FLOAT_TYPEKIND.equals(type.getTypeKind())) {

                tdef = new FloatTypeDefinition();

                switch (type.getTypeSize().intValue()) {
                    case 2:
                        ((FloatTypeDefinition) tdef).setSize(FloatTypeDefinition.Size.HALF);
                        break;
                    case 4:
                        ((FloatTypeDefinition) tdef).setSize(FloatTypeDefinition.Size.SINGLE);
                        break;
                    case 8:
                        ((FloatTypeDefinition) tdef).setSize(FloatTypeDefinition.Size.DOUBLE);
                        break;
                    default:
                        throw new Exception("Illegal Type Size.");

                }

            } else if (com.sandflow.smpte.register.TypesRegister.Entry.LENSSERIALFLOAT_TYPEKIND.equals(type.getTypeKind())) {

                tdef = new LensSerialFloatTypeDefinition();

            } else if (com.sandflow.smpte.register.TypesRegister.Entry.RECORD_TYPEKIND.equals(type.getTypeKind())) {

                tdef = new RecordTypeDefinition();

                for (com.sandflow.smpte.register.TypesRegister.Entry.Facet tchild : type.getFacets()) {
                    Member m = new Member();

                    m.setName(tchild.getSymbol());
                    m.setType(new AUID(tchild.getType()));

                    ((RecordTypeDefinition) tdef).addMember(m);
                }

            } else if (com.sandflow.smpte.register.TypesRegister.Entry.FIXEDARRAY_TYPEKIND.equals(type.getTypeKind())) {

                tdef = new FixedArrayTypeDefinition();

                ((FixedArrayTypeDefinition) tdef).setElementType(new AUID(type.getBaseType()));

                ((FixedArrayTypeDefinition) tdef).setElementCount(type.getTypeSize().intValue());

            } else if (com.sandflow.smpte.register.TypesRegister.Entry.ARRAY_TYPEKIND.equals(type.getTypeKind())) {

                tdef = new VariableArrayTypeDefinition();
                ((VariableArrayTypeDefinition) tdef).setElementType(new AUID(type.getBaseType()));

            } else if (com.sandflow.smpte.register.TypesRegister.Entry.SET_TYPEKIND.equals(type.getTypeKind())) {

                tdef = new SetTypeDefinition();
                ((SetTypeDefinition) tdef).setElementType(new AUID(type.getBaseType()));

            } else if (com.sandflow.smpte.register.TypesRegister.Entry.INDIRECT_TYPEKIND.equals(type.getTypeKind())) {

                tdef = new IndirectTypeDefinition();

            } else if (com.sandflow.smpte.register.TypesRegister.Entry.OPAQUE_TYPEKIND.equals(type.getTypeKind())) {

                tdef = new OpaqueTypeDefinition();

            } else if (com.sandflow.smpte.register.TypesRegister.Entry.STREAM_TYPEKIND.equals(type.getTypeKind())) {

                tdef = new StreamTypeDefinition();

            } else if (com.sandflow.smpte.register.TypesRegister.Entry.WEAKREF_TYPEKIND.equals(type.getTypeKind())) {

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

                /* INFO: skip weak Reference target sets until registers are accurate.
                *        They are not necessary for encoding.
                */
                
                for (TypesRegister.Entry.Facet f : type.getFacets()) {

                    UL ul = null;

                    if (f.getValue() != null) {
                        ul = UL.fromURN(f.getValue());
                    }

                    if (ul == null) {
                        LOGGER.warning(
                                String.format(
                                        "Missing Target Set UL at Type %s",
                                        type.getUL().toString()
                                )
                        );

                        continue;
                    }

                    if (!((WeakReferenceTypeDefinition) tdef).getTargetSet().add(new AUID(ul))) {

                        LOGGER.warning(
                                String.format(
                                        "Duplicate Target Set UL at Type %s",
                                        type.getUL().toString()
                                )
                        );
                    }
                }

            } else if (com.sandflow.smpte.register.TypesRegister.Entry.STRONGREF_TYPEKIND.equals(type.getTypeKind())) {

                tdef = new StrongReferenceTypeDefinition();

                ((StrongReferenceTypeDefinition) tdef).setReferenceType(new AUID(type.getBaseType()));

            } else if (com.sandflow.smpte.register.TypesRegister.Entry.ENUMERATION_TYPEKIND.equals(type.getTypeKind())) {

                if (type.getBaseType().equals(UL.fromURN("urn:smpte:ul:060E2B34.01040101.01030100.00000000"))) {
                    
                    ArrayList<ExtendibleEnumerationTypeDefinition.Element> ecelems = new ArrayList<>();

                    /* NOTE: Facets of Extendible Enumeration Definitions are not imported since
                     *       they are, by definition, extendible. In other words, Extendible
                     *       Enumeration instance are expected to handle UL values that are not
                     *       listed in the register. 
                     */
                    
                    /*
                     for (Facet f : type.getFacets()) {
                     ExtendibleEnumerationTypeDefinition.Element m = new ExtendibleEnumerationTypeDefinition.Element();

                     m.setValue(new AUID(f.getUL()));

                     ecelems.add(m);
                     }*/
                    
                    tdef = new ExtendibleEnumerationTypeDefinition(ecelems);

                } else {

                    ArrayList<EnumerationTypeDefinition.Element> celems = new ArrayList<>();

                    for (TypesRegister.Entry.Facet f : type.getFacets()) {
                        EnumerationTypeDefinition.Element m = new EnumerationTypeDefinition.Element();

                        m.setName(f.getSymbol());
                        m.setValue(Integer.decode(f.getValue()));

                        celems.add(m);
                    }

                    tdef = new EnumerationTypeDefinition(celems);

                    ((EnumerationTypeDefinition) tdef).setElementType(new AUID(type.getBaseType()));
                }

            } else if (com.sandflow.smpte.register.TypesRegister.Entry.CHARACTER_TYPEKIND.equals(type.getTypeKind())) {

                tdef = new CharacterTypeDefinition();

            } else if (com.sandflow.smpte.register.TypesRegister.Entry.STRING_TYPEKIND.equals(type.getTypeKind())) {

                tdef = new StringTypeDefinition();

                ((StringTypeDefinition) tdef).setElementType(new AUID(type.getBaseType()));

            } else {
                LOGGER.warning(
                        String.format(
                                "Unknown type kind of %s for Type UL %s.",
                                type.getTypeKind(),
                                type.getUL().toString()
                        )
                );

                continue;
            }

            if (tdef != null) {
                tdef.setIdentification(new AUID(type.getUL()));
                tdef.setSymbol(type.getSymbol());
                tdef.setName(type.getName());
                tdef.setDescription(type.getDefinition());
                tdef.setNamespace(type.getNamespaceName());

                defs.add(tdef);
                defIDs.add(tdef.getIdentification());
            } else {
                LOGGER.warning(
                        String.format(
                                "Byte Type UL %s.",
                                type.getUL().toString()
                        )
                );
            }
        }

        MetaDictionaryCollection mds = new MetaDictionaryCollection();

        long index = 0;

        for (Definition def : defs) {

            try {
                mds.addDefinition(def);
            } catch (DuplicateSymbolException dse) {

                /* attempt to generate an ad hoc symbol instead of dying */
                String newsym = "dup" + def.getSymbol() + (index++);

                LOGGER.warning(
                        String.format(
                                "Duplicate symbol %s (%s) renamed %s",
                                def.getSymbol(),
                                def.getNamespace().toASCIIString(),
                                newsym
                        )
                );

                def.setSymbol(newsym);

                mds.addDefinition(def);
            }

        }

        return mds;

    }

}
