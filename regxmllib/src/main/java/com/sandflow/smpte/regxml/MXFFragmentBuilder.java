/*
 * Copyright (c) 2015, Pierre-Anthony Lemieux (pal@sandflow.com)
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

import com.sandflow.smpte.klv.Group;
import com.sandflow.smpte.klv.KLVInputStream;
import com.sandflow.smpte.klv.LocalSet;
import com.sandflow.smpte.klv.LocalTagRegister;
import com.sandflow.smpte.klv.Triplet;
import com.sandflow.smpte.klv.exceptions.KLVException;
import com.sandflow.smpte.mxf.FillItem;
import com.sandflow.smpte.mxf.PartitionPack;
import com.sandflow.smpte.mxf.PrimerPack;
import com.sandflow.smpte.mxf.Set;
import com.sandflow.smpte.regxml.dict.DefinitionResolver;
import com.sandflow.smpte.regxml.dict.definitions.ClassDefinition;
import com.sandflow.smpte.regxml.dict.definitions.Definition;
import com.sandflow.smpte.util.AUID;
import com.sandflow.smpte.util.CountingInputStream;
import com.sandflow.smpte.util.UL;
import com.sandflow.smpte.util.UUID;
import com.sandflow.util.EventHandler;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

/**
 * Builds a RegXML Fragment (SMPTE ST 2001-1) from an MXF file (SMPTE ST 377-1).
 */
public class MXFFragmentBuilder {

    private final static Logger LOG = Logger.getLogger(MXFFragmentBuilder.class.getName());

    private static final UL INDEX_TABLE_SEGMENT_UL
        = UL.fromURN("urn:smpte:ul:060e2b34.02530101.0d010201.01100100");

    private static final UL PREFACE_KEY
        = UL.fromURN("urn:smpte:ul:060e2b34.027f0101.0d010101.01012f00");

    public static enum EventKind {

        /**
         * No root object found
         */
        MISSING_ROOT_OBJECT(EventHandler.Severity.FATAL),
        /**
         * No partition pack found in the MXF file
         */
        MISSING_PARTITION_PACK(EventHandler.Severity.FATAL),
        /**
         * No primer pack found in the MXF file
         */
        MISSING_PRIMER_PACK(EventHandler.Severity.FATAL),
        /**
         * Unexpected group sequence encountered
         */
        UNEXPECTED_STRUCTURE(EventHandler.Severity.ERROR),
        /**
         * Failed to read Group
         */
        GROUP_READ_FAILED(EventHandler.Severity.ERROR);

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

    static void handleEvent(EventHandler handler, EventHandler.Event evt) throws MXFException {

        if (handler != null) {

            if (! handler.handle(evt) || evt.getSeverity() == EventHandler.Severity.FATAL) {
                
                /* die on FATAL events or if requested by the handler */

                throw new MXFException(evt.getMessage());

            }
            
        } else if (evt.getSeverity() == EventHandler.Severity.ERROR ||
            evt.getSeverity() == EventHandler.Severity.FATAL) {
            
            /* if no event handler was provided, die on FATAL and ERROR events */
            
            throw new MXFException(evt.getMessage());
            
        }
    }

    /**
     * @deprecated Replaced by {@link fromInputStreamfromInputStream(InputStream,DefinitionResolver,
     * FragmentBuilder.AUIDNameResolver,UL,Document)}. This constructor does not allow the
     * caller to provide an event handler, and instead uses java.util.logging to
     * output events.
     * 
     * @param mxfpartition MXF partition, including the Partition Pack. Must not be null.
     * @param defresolver MetaDictionary definitions. Must not be null.
     * @param rootclasskey Root class of Fragment. The Preface class is used if null.
     * @param document DOM for which the Document Fragment is created. Must not be null.
     *
     * @return Document Fragment containing a single RegXML Fragment
     *
     * @throws IOException
     * @throws KLVException
     * @throws com.sandflow.smpte.regxml.MXFFragmentBuilder.MXFException
     * @throws com.sandflow.smpte.regxml.FragmentBuilder.RuleException
     */
    public static DocumentFragment fromInputStream(InputStream mxfpartition,
        DefinitionResolver defresolver,
        UL rootclasskey,
        Document document) throws IOException, KLVException, MXFException, FragmentBuilder.RuleException {

        return fromInputStream(mxfpartition,
            defresolver,
            null,
            rootclasskey,
            document);
    }
    
    /**
     * @deprecated Replaced by {@link fromInputStreamfromInputStream(InputStream,DefinitionResolver,
     * FragmentBuilder.AUIDNameResolver,UL,Document)}. This constructor does not allow the
     * caller to provide an event handler, and instead uses java.util.logging to
     * output events.
     * 
     * @param mxfpartition MXF partition, including the Partition Pack. Must not be null.
     * @param defresolver MetaDictionary definitions. Must not be null.
     * @param enumnameresolver Allows the local name of extendible enumeration
     * values to be inserted as comments. May be null.
     * @param rootclasskey Root class of Fragment. The Preface class is used if null.
     * @param document DOM for which the Document Fragment is created. Must not be null.
     *
     * @return Document Fragment containing a single RegXML Fragment
     *
     * @throws IOException
     * @throws KLVException
     * @throws com.sandflow.smpte.regxml.MXFFragmentBuilder.MXFException
     * @throws com.sandflow.smpte.regxml.FragmentBuilder.RuleException
     */
    public static DocumentFragment fromInputStream(InputStream mxfpartition,
        DefinitionResolver defresolver,
        FragmentBuilder.AUIDNameResolver enumnameresolver,
        UL rootclasskey,
        Document document) throws IOException, KLVException, MXFException, FragmentBuilder.RuleException {
        
        EventHandler handler = new EventHandler() {

                @Override
                public boolean handle(EventHandler.Event evt) {
                    switch (evt.getSeverity()) {
                        case ERROR:
                        case FATAL:
                            LOG.severe(evt.getMessage());
                            break;
                        case INFO:
                            LOG.info(evt.getMessage());
                            break;
                        case WARN:
                            LOG.warning(evt.getMessage());
                    }

                    return true;
                }
            };

        return fromInputStream(mxfpartition,
            defresolver,
            enumnameresolver,
            handler,
            rootclasskey,
            document);
    }

    /**
     * Returns a DOM Document Fragment containing a RegXML Fragment rooted at
     * the first Header Metadata object with a class that descends from the
     * specified class.
     *
     * @param mxfpartition MXF partition, including the Partition Pack. Must not be null.
     * @param defresolver MetaDictionary definitions. Must not be null.
     * @param enumnameresolver Allows the local name of extendible enumeration
     * values to be inserted as comments. May be null.
     * @param evthandler Calls back the caller when an event occurs. Must not be null.
     * @param rootclasskey Root class of Fragment. The Preface class is used if null.
     * @param document DOM for which the Document Fragment is created. Must not be null.
     *
     * @return Document Fragment containing a single RegXML Fragment
     *
     * @throws IOException
     * @throws KLVException
     * @throws com.sandflow.smpte.regxml.MXFFragmentBuilder.MXFException
     * @throws com.sandflow.smpte.regxml.FragmentBuilder.RuleException
     */
    public static DocumentFragment fromInputStream(
        InputStream mxfpartition,
        DefinitionResolver defresolver,
        FragmentBuilder.AUIDNameResolver enumnameresolver,
        EventHandler evthandler,
        UL rootclasskey,
        Document document
    ) throws IOException, KLVException, MXFException, FragmentBuilder.RuleException {

        CountingInputStream cis = new CountingInputStream(mxfpartition);

        /* look for the partition pack */
        KLVInputStream kis = new KLVInputStream(cis);

        PartitionPack pp = null;

        for (Triplet t; (t = kis.readTriplet()) != null;) {

            if ((pp = PartitionPack.fromTriplet(t)) != null) {
                break;
            }
        }

        if (pp == null) {

            Event evt = new Event(
                EventKind.MISSING_PARTITION_PACK,
                "No Partition Pack found"
            );

            handleEvent(evthandler, evt);

        }

        /* start counting header metadata bytes */
        cis.resetCount();

        /* look for the primer pack */
        LocalTagRegister localreg = null;

        for (Triplet t; (t = kis.readTriplet()) != null; cis.resetCount()) {

            /* skip fill items, if any */
            if (!t.getKey().equalsIgnoreVersion(FillItem.getKey())) {
                localreg = PrimerPack.createLocalTagRegister(t);
                break;
            }

        }

        if (localreg == null) {

            Event evt = new Event(
                EventKind.MISSING_PRIMER_PACK,
                "No Primer Pack found"
            );

            handleEvent(evthandler, evt);
        }

        /* capture all local sets within the header metadata */
        ArrayList<Group> gs = new ArrayList<>();
        HashMap<UUID, Set> setresolver = new HashMap<>();

        for (Triplet t;
            cis.getCount() < pp.getHeaderByteCount()
            && (t = kis.readTriplet()) != null;) {

            if (t.getKey().equalsIgnoreVersion(INDEX_TABLE_SEGMENT_UL)) {

                /* stop if Index Table reached */
                Event evt = new Event(
                    EventKind.UNEXPECTED_STRUCTURE,
                    "Index Table Segment encountered before Header Byte Count bytes read"
                );

                handleEvent(evthandler, evt);

                break;

            } else if (t.getKey().equalsIgnoreVersion(FillItem.getKey())) {

                /* skip fill items */
                continue;
            }

            try {
                Group g = LocalSet.fromTriplet(t, localreg);

                if (g != null) {

                    gs.add(g);

                    Set set = Set.fromGroup(g);

                    if (set != null) {
                        setresolver.put(set.getInstanceID(), set);
                    }

                } else {

                    Event evt = new Event(
                        EventKind.GROUP_READ_FAILED,
                        String.format(
                            "Failed to read Group: {0}",
                            t.getKey().toString()
                        )
                    );

                    handleEvent(evthandler, evt);

                }
            } catch (KLVException ke) {

                Event evt = new Event(
                    EventKind.GROUP_READ_FAILED,
                    String.format(
                        "Failed to read Group %s with error %s",
                        t.getKey().toString(),
                        ke.getMessage()
                    )
                );

                handleEvent(evthandler, evt);

            }
        }

        for (Group agroup : gs) {

            /* in MXF, the first header metadata set should be the 
             Preface set according to ST 377-1 Section 9.5.1, preceded
             by Class 14 groups
             */
            if (agroup.getKey().equalsWithMask(PREFACE_KEY, 0b1111101011111111 /* ignore version and Group coding */)) {

                break;

            } else if (!agroup.getKey().isClass14()) {

                Event evt = new Event(
                    EventKind.UNEXPECTED_STRUCTURE,
                    String.format(
                        "At least one non-class 14 Set %s was found between"
                        + " the Primer Pack and the Preface Set.",
                        agroup.getKey()
                    )
                );

                handleEvent(evthandler, evt);

                break;

            }

        }

        /* create the fragment */
        FragmentBuilder fb = new FragmentBuilder(defresolver, setresolver, enumnameresolver, evthandler);

        Group rootgroup = null;

        if (rootclasskey != null) {

            Iterator<Group> iter = gs.iterator();

            /* find first essence descriptor */
            while (rootgroup == null && iter.hasNext()) {

                Group g = iter.next();

                AUID gid = new AUID(g.getKey());

                /* go up the class hierarchy */
                while (rootgroup == null && gid != null) {

                    Definition def = defresolver.getDefinition(gid);

                    /* skip if not a class instance */
                    if (!(def instanceof ClassDefinition)) {
                        break;
                    }

                    /* is it an instance of the requested root object */
                    UL gul = def.getIdentification().asUL();

                    if (gul.equalsWithMask(rootclasskey, 0b1111101011111111 /* ignore version and Group coding */)) {
                        rootgroup = g;

                    } else {
                        /* get parent class */
                        gid = ((ClassDefinition) def).getParentClass();
                    }
                }

            }

        } else {

            rootgroup = gs.get(0);

        }

        if (rootgroup == null) {

            Event evt = new Event(
                EventKind.MISSING_ROOT_OBJECT,
                "No Root Object found"
            );

            handleEvent(evthandler, evt);

        }

        return fb.fromTriplet(rootgroup, document);

    }

    public static class MXFException extends Exception {

        public MXFException(String msg) {
            super(msg);
        }
    }
}
