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
import com.sandflow.util.EventHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Generates MetaDictionaries from SMPTE Metadata Registers.
 */
public class RegisterImporter {

    private final static Logger LOGGER = Logger.getLogger(RegisterImporter.class.getName());

    public static enum EventKind {

        /**
         * Element definition cannot be found
         *//**
         * Element definition cannot be found
         */
        UNKNOWN_ELEMENT(EventHandler.Severity.WARN),
        /**
         * Type definition cannot be found.
         */
        UNKNOWN_TYPE(EventHandler.Severity.WARN),
        /**
         * Weak Reference Target Set cannot be found.
         */
        UNKNOWN_TARGET_SET(EventHandler.Severity.WARN),
        /**
         * Weak Reference Target Set duplicated
         */
        DUP_TARGET_SET(EventHandler.Severity.WARN),
        /**
         * Unknown type kind encountered.
         */
        UNKNOWN_TYPE_KIND(EventHandler.Severity.ERROR),
        /**
         * Type Definition import failed
         */
        TYPE_IMPORT_FAILED(EventHandler.Severity.ERROR),
        /**
         * Duplicate symbol found
         */
        DUPLICATE_SYMBOL(EventHandler.Severity.ERROR);

        public final EventHandler.Severity severity;

        private EventKind(EventHandler.Severity severity) {
            this.severity = severity;
        }

    }

    public static class Event extends EventHandler.BasicEvent {

        private final EventKind kind;

        public Event(EventKind kind, String message) {
            super(kind.severity, Event.class.getCanonicalName(), message);

            this.kind = kind;
        }

        public EventKind getKind() {
            return kind;
        }

    }

    static void handleEvent(EventHandler handler, EventHandler.Event evt) throws Exception {

        if (handler != null) {

            if (!handler.handle(evt) || evt.getSeverity() == EventHandler.Severity.FATAL) {

                /* die on FATAL events or if requested by the handler */
                throw new Exception(evt.getMessage());

            }

        } else if (evt.getSeverity() == EventHandler.Severity.ERROR
            || evt.getSeverity() == EventHandler.Severity.FATAL) {

            /* if no event handler was provided, die on FATAL and ERROR events */
            throw new Exception(evt.getMessage());

        }
    }

    /**
     * @deprecated Replaced by {@link MetaDictionaryCollection fromRegister(TypesRegister,
     * GroupsRegister,ElementsRegister,EventHandler)}. This constructor does not allow the
     * caller to provide an event handler, and instead uses java.util.logging to
     * output events.
     *
     * @param tr Types Register
     * @param gr Groups Register
     * @param er Elements Register
     *
     * @return Collection of Metadictionaries
     *
     * @throws Exception
     */
    public static MetaDictionaryCollection fromRegister(TypesRegister tr,
        GroupsRegister gr,
        ElementsRegister er
    ) throws Exception {

        EventHandler handler = new EventHandler() {

            @Override
            public boolean handle(EventHandler.Event evt) {
                switch (evt.getSeverity()) {
                    case ERROR:
                    case FATAL:
                        LOGGER.severe(evt.getMessage());
                        break;
                    case INFO:
                        LOGGER.info(evt.getMessage());
                        break;
                    case WARN:
                        LOGGER.warning(evt.getMessage());
                }

                return true;
            }
        };

        return fromRegister(tr, gr, er, null);
    }

    /**
     * Generates MetaDictionaries from SMPTE Types, Groups and Elements
     * Registers.
     *
     * @param tr Types Register
     * @param gr Groups Register
     * @param er Elements Register
     * @param evthandler Event Handler. May be null.
     *
     * @return Collection of Metadictionaries
     *
     * @throws Exception
     */
    public static MetaDictionaryCollection fromRegister(TypesRegister tr,
        GroupsRegister gr,
        ElementsRegister er,
        EventHandler evthandler
    ) throws Exception {

        /* definition collection */
        LinkedHashMap<AUID, ArrayList<Definition>> defs = new LinkedHashMap<>();

        /* some types may refer to groups that have been excluded since they
         did not have a local set representation. This variable keep track of
         references in order to prune dangling references later */
        HashMap<AUID, HashSet<AUID>> isReferencedBy = new HashMap<>();

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

                if (defs.containsKey(id)) {

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

                    Event evt = new Event(
                        EventKind.UNKNOWN_ELEMENT,
                        String.format(
                            "Undefined Element %s for Group %s",
                            child.getItem(),
                            group.getUL()
                        )
                    );

                    handleEvent(evthandler, evt);

                    continue;
                }

                pdef.setDescription(element.getDefinition());

                pdef.setName(element.getName());

                pdef.setSymbol(element.getSymbol());

                if (element.getTypeUL() == null) {

                    Event evt = new Event(
                        EventKind.UNKNOWN_TYPE,
                        String.format(
                            "Missing Type UL at Element %s for Group %s",
                            child.getItem(),
                            group.getUL()
                        )
                    );

                    handleEvent(evthandler, evt);

                    continue;
                }

                pdef.setType(new AUID(element.getTypeUL()));

                pdef.setMemberOf(cdef.getIdentification());

                pdef.setNamespace(element.getNamespaceName());

                _add(defs, pdef);

            }

            _add(defs, cdef);

        }

        /* Handle Types Entries */
        for (com.sandflow.smpte.register.TypesRegister.Entry type : tr.getEntries()) {
            if (!type.getKind().equals(com.sandflow.smpte.register.TypesRegister.Entry.Kind.LEAF)) {
                continue;
            }

            Definition tdef = null;

            HashSet<AUID> references = new HashSet<>();

            if (com.sandflow.smpte.register.TypesRegister.Entry.RENAME_TYPEKIND.equals(type.getTypeKind())) {

                tdef = new RenameTypeDefinition();

                ((RenameTypeDefinition) tdef).setRenamedType(new AUID(type.getBaseType()));

                references.add(((RenameTypeDefinition) tdef).getRenamedType());

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

                    references.add(m.getType());
                }

            } else if (com.sandflow.smpte.register.TypesRegister.Entry.FIXEDARRAY_TYPEKIND.equals(type.getTypeKind())) {

                tdef = new FixedArrayTypeDefinition();

                ((FixedArrayTypeDefinition) tdef).setElementType(new AUID(type.getBaseType()));

                ((FixedArrayTypeDefinition) tdef).setElementCount(type.getTypeSize().intValue());

                references.add(((FixedArrayTypeDefinition) tdef).getElementType());

            } else if (com.sandflow.smpte.register.TypesRegister.Entry.ARRAY_TYPEKIND.equals(type.getTypeKind())) {

                tdef = new VariableArrayTypeDefinition();
                ((VariableArrayTypeDefinition) tdef).setElementType(new AUID(type.getBaseType()));

                references.add(((VariableArrayTypeDefinition) tdef).getElementType());

            } else if (com.sandflow.smpte.register.TypesRegister.Entry.SET_TYPEKIND.equals(type.getTypeKind())) {

                tdef = new SetTypeDefinition();
                ((SetTypeDefinition) tdef).setElementType(new AUID(type.getBaseType()));

                references.add(((SetTypeDefinition) tdef).getElementType());

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

                references.add(((WeakReferenceTypeDefinition) tdef).getReferencedType());

                for (TypesRegister.Entry.Facet f : type.getFacets()) {

                    UL ul = null;

                    if (f.getValue() != null) {
                        ul = UL.fromURN(f.getValue());
                    }

                    if (ul == null) {

                        Event evt = new Event(
                            EventKind.UNKNOWN_TARGET_SET,
                            String.format(
                                "Missing Target Set UL at Type %s",
                                type.getUL().toString()
                            )
                        );

                        handleEvent(evthandler, evt);

                        continue;
                    }

                    if (!((WeakReferenceTypeDefinition) tdef).getTargetSet().add(new AUID(ul))) {

                        Event evt = new Event(
                            EventKind.DUP_TARGET_SET,
                            String.format(
                                "Duplicate Target Set UL at Type %s",
                                type.getUL().toString()
                            )
                        );

                        handleEvent(evthandler, evt);

                    }

                    references.add(new AUID(ul));
                }

            } else if (com.sandflow.smpte.register.TypesRegister.Entry.STRONGREF_TYPEKIND.equals(type.getTypeKind())) {

                tdef = new StrongReferenceTypeDefinition();

                ((StrongReferenceTypeDefinition) tdef).setReferenceType(new AUID(type.getBaseType()));

                references.add(((StrongReferenceTypeDefinition) tdef).getReferenceType());

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

                    references.add(((EnumerationTypeDefinition) tdef).getElementType());
                }

            } else if (com.sandflow.smpte.register.TypesRegister.Entry.CHARACTER_TYPEKIND.equals(type.getTypeKind())) {

                tdef = new CharacterTypeDefinition();

            } else if (com.sandflow.smpte.register.TypesRegister.Entry.STRING_TYPEKIND.equals(type.getTypeKind())) {

                tdef = new StringTypeDefinition();

                ((StringTypeDefinition) tdef).setElementType(new AUID(type.getBaseType()));

                references.add(((StringTypeDefinition) tdef).getElementType());

            } else {

                Event evt = new Event(
                    EventKind.UNKNOWN_TYPE_KIND,
                    String.format(
                        "Unknown type kind of %s for Type UL %s.",
                        type.getTypeKind(),
                        type.getUL().toString()
                    )
                );

                handleEvent(evthandler, evt);

                continue;
            }

            if (tdef != null) {

                tdef.setIdentification(new AUID(type.getUL()));
                tdef.setSymbol(type.getSymbol());
                tdef.setName(type.getName());
                tdef.setDescription(type.getDefinition());
                tdef.setNamespace(type.getNamespaceName());

                _add(defs, tdef);

                /* adds any entry that the entry references */
                for (AUID aref : references) {

                    HashSet<AUID> hs = isReferencedBy.get(aref);

                    if (hs == null) {

                        hs = new HashSet<>();

                        isReferencedBy.put(aref, hs);

                    }

                    hs.add(tdef.getIdentification());
                }

            } else {

                Event evt = new Event(
                    EventKind.TYPE_IMPORT_FAILED,
                    String.format(
                        "Type UL %s import failed",
                        type.getUL().toString()
                    )
                );

                handleEvent(evthandler, evt);

            }
        }

        /* prune all dangling entries */
        for (AUID aref : isReferencedBy.keySet()) {

            if (!defs.containsKey(aref)) {

                /* if the referenced entry does not exist prune all entries that
                 reference it, directly or indirectly */
                _prune(defs, isReferencedBy, aref);

            }

        }

        /* create the metadictionaries */
        MetaDictionaryCollection mds = new MetaDictionaryCollection();

        long index = 0;

        for (ArrayList<Definition> defarray : defs.values()) {

            for (Definition def : defarray) {

                try {
                    mds.addDefinition(def);
                } catch (DuplicateSymbolException dse) {

                    /* attempt to generate an ad hoc symbol instead of dying */
                    String newsym = "dup" + def.getSymbol() + (index++);

                    Event evt = new Event(
                        EventKind.DUPLICATE_SYMBOL,
                        String.format(
                            "Duplicate symbol %s (%s) renamed %s",
                            def.getSymbol(),
                            def.getNamespace().toASCIIString(),
                            newsym
                        )
                    );

                    handleEvent(evthandler, evt);

                    def.setSymbol(newsym);

                    mds.addDefinition(def);
                }
            }
        }

        return mds;

    }

    private static void _add(Map<AUID, ArrayList<Definition>> defs, Definition def) {
        ArrayList<Definition> ad = defs.get(def.getIdentification());

        if (ad == null) {
            ad = new ArrayList<>();
            defs.put(def.getIdentification(), ad);
        }

        ad.add(def);

    }

    private static void _prune(Map<AUID, ArrayList<Definition>> defs,
        HashMap<AUID, HashSet<AUID>> isReferencedBy,
        AUID aref) {

        if (isReferencedBy.containsKey(aref)) {

            for (AUID entry : isReferencedBy.get(aref)) {

                _prune(defs, isReferencedBy, entry);

            }

        }

        defs.remove(aref);
    }

}
