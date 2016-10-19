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

import com.sandflow.smpte.klv.Group;
import com.sandflow.smpte.klv.KLVInputStream.ByteOrder;
import com.sandflow.smpte.klv.Triplet;
import com.sandflow.smpte.klv.exceptions.KLVException;
import com.sandflow.smpte.mxf.MXFInputStream;
import com.sandflow.smpte.mxf.Set;
import com.sandflow.smpte.regxml.dict.DefinitionResolver;
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
import com.sandflow.smpte.regxml.dict.definitions.RenameTypeDefinition;
import com.sandflow.smpte.regxml.dict.definitions.SetTypeDefinition;
import com.sandflow.smpte.regxml.dict.definitions.StreamTypeDefinition;
import com.sandflow.smpte.regxml.dict.definitions.StringTypeDefinition;
import com.sandflow.smpte.regxml.dict.definitions.StrongReferenceTypeDefinition;
import com.sandflow.smpte.regxml.dict.definitions.VariableArrayTypeDefinition;
import com.sandflow.smpte.regxml.dict.definitions.WeakReferenceTypeDefinition;
import com.sandflow.smpte.util.AUID;
import com.sandflow.util.EventHandler;
import com.sandflow.smpte.util.HalfFloat;
import com.sandflow.smpte.util.IDAU;
import com.sandflow.smpte.util.UL;
import com.sandflow.smpte.util.UMID;
import com.sandflow.smpte.util.UUID;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Builds a RegXML Fragment of a single KLV Group, typically a Header Metadata
 * MXF Set, using a collection of MetaDictionary definitions
 */
public class FragmentBuilder {

    public static enum EventKind {

        /**
         * Raised when a Group Key cannot be matched to a group definition
         */
        UNKNOWN_GROUP(EventHandler.Severity.INFO),
        /**
         * Raised when a Property Key cannot be matched to a property definition
         */
        UNKNOWN_PROPERTY(EventHandler.Severity.INFO),
        /**
         * Raised when the version byte of a UL does not match that listed in
         * the metadictionary
         */
        VERSION_BYTE_MISMATCH(EventHandler.Severity.WARN),
        /**
         * Raised when the definition associated with a UL does not match the
         * kind of definition that was expected
         */
        UNEXPECTED_DEFINITION(EventHandler.Severity.ERROR),
        /**
         * Raised when a circular reference is found between Sets
         */
        CIRCULAR_STRONG_REFERENCE(EventHandler.Severity.ERROR),
        /**
         * Raised when the byte order indicated in the file does not match the
         * specification.
         */
        UNEXPECTED_BYTE_ORDER(EventHandler.Severity.ERROR),
        /**
         * Raised when a type definition is not found.
         */
        UNKNOWN_TYPE(EventHandler.Severity.ERROR),
        /**
         * Raised when the target of a weak reference does not have a unique
         * property.
         */
        MISSING_UNIQUE(EventHandler.Severity.ERROR),
        /**
         * Raised when the referenced primary package is not found.
         */
        MISSING_PRIMARY_PACKAGE(EventHandler.Severity.ERROR),
        /**
         * Raised when the size of data read does not match expectations.
         */
        VALUE_LENGTH_MISMATCH(EventHandler.Severity.ERROR),
        /**
         * Raised when the type of a Character definition is not supported.
         */
        UNSUPPORTED_CHAR_TYPE(EventHandler.Severity.ERROR),
        /**
         * Raised when the type of an enum definition is not supported.
         */
        UNSUPPORTED_ENUM_TYPE(EventHandler.Severity.ERROR),
        /**
         * Raised when an enum value is not supported.
         */
        UNKNOWN_ENUM_VALUE(EventHandler.Severity.ERROR),
        /**
         * Raised when an IDAU is invalid.
         */
        INVALID_IDAU(EventHandler.Severity.ERROR),
        /**
         * Raised when an enum value is not supported.
         */
        INVALID_INTEGER_VALUE(EventHandler.Severity.ERROR),
        /**
         * Raised when the type of a String definition is not supported.
         */
        UNSUPPORTED_STRING_TYPE(EventHandler.Severity.ERROR),
        /**
         * Raised when the target of a Strong Reference Type is not a class.
         */
        INVALID_STRONG_REFERENCE_TYPE(EventHandler.Severity.ERROR),
        /**
         * Raised when the target of a Strong Reference is not found
         */
        STRONG_REFERENCE_NOT_FOUND(EventHandler.Severity.ERROR);

        public final EventHandler.Severity severity;

        private EventKind(EventHandler.Severity severity) {
            this.severity = severity;
        }

    }

    public static class Event implements EventHandler.Event {

        private final EventKind kind;
        private final String reason;
        private final String where;

        public Event(EventKind kind, String reason) {
            this(kind, reason, null);
        }

        public Event(EventKind kind, String reason, String where) {
            this.reason = reason;
            this.where = where;
            this.kind = kind;
        }

        public EventKind getKind() {
            return kind;
        }

        public String getReason() {
            return reason;
        }

        public String getWhere() {
            return where;
        }

        @Override
        public String getMessage() {
            return this.reason + (this.where != null ? " at " + this.where : "");
        }

        @Override
        public String getOrigin() {
            return FragmentBuilder.class.getSimpleName();
        }

        @Override
        public EventHandler.Severity getSeverity() {
            return kind.severity;
        }

    }

    private final static Logger LOG = Logger.getLogger(FragmentBuilder.class.getName());

    private static final UL INSTANCE_UID_ITEM_UL = UL.fromURN("urn:smpte:ul:060e2b34.01010101.01011502.00000000");
    private static final UL AUID_UL = UL.fromDotValue("06.0E.2B.34.01.04.01.01.01.03.01.00.00.00.00.00");
    private static final UL UUID_UL = UL.fromDotValue("06.0E.2B.34.01.04.01.01.01.03.03.00.00.00.00.00");
    private static final UL DateStruct_UL = UL.fromDotValue("06.0E.2B.34.01.04.01.01.03.01.05.00.00.00.00.00");
    private static final UL PackageID_UL = UL.fromDotValue("06.0E.2B.34.01.04.01.01.01.03.02.00.00.00.00.00");
    private static final UL Rational_UL = UL.fromDotValue("06.0E.2B.34.01.04.01.01.03.01.01.00.00.00.00.00");
    private static final UL TimeStruct_UL = UL.fromDotValue("06.0E.2B.34.01.04.01.01.03.01.06.00.00.00.00.00");
    private static final UL TimeStamp_UL = UL.fromDotValue("06.0E.2B.34.01.04.01.01.03.01.07.00.00.00.00.00");
    private static final UL VersionType_UL = UL.fromDotValue("06.0E.2B.34.01.04.01.01.03.01.03.00.00.00.00.00");
    private static final UL ByteOrder_UL = UL.fromDotValue("06.0E.2B.34.01.01.01.01.03.01.02.01.02.00.00.00");
    private static final UL Character_UL = UL.fromURN("urn:smpte:ul:060e2b34.01040101.01100100.00000000");
    private static final UL Char_UL = UL.fromURN("urn:smpte:ul:060e2b34.01040101.01100300.00000000");
    private static final UL UTF8Character_UL = UL.fromURN("urn:smpte:ul:060e2b34.01040101.01100500.00000000");
    private static final UL ProductReleaseType_UL = UL.fromURN("urn:smpte:ul:060e2b34.01040101.02010101.00000000");
    private static final UL Boolean_UL = UL.fromURN("urn:smpte:ul:060e2b34.01040101.01040100.00000000");
    private static final UL PrimaryPackage_UL = UL.fromURN("urn:smpte:ul:060e2b34.01010104.06010104.01080000");
    private static final UL LinkedGenerationID_UL = UL.fromURN("urn:smpte:ul:060e2b34.01010102.05200701.08000000");
    private static final UL GenerationID_UL = UL.fromURN("urn:smpte:ul:060e2b34.01010102.05200701.01000000");
    private static final UL ApplicationProductID_UL = UL.fromURN("urn:smpte:ul:060e2b34.01010102.05200701.07000000");

    private static final String REGXML_NS = "http://sandflow.com/ns/SMPTEST2001-1/baseline";
    private final static String XMLNS_NS = "http://www.w3.org/2000/xmlns/";

    private static final String BYTEORDER_BE = "BigEndian";
    private static final String BYTEORDER_LE = "LittleEndian";
    private static final String UID_ATTR = "uid";
    private static final String ACTUALTYPE_ATTR = "actualType";

    private final DefinitionResolver defresolver;
    private final Map<UUID, Set> setresolver;
    private final HashMap<URI, String> nsprefixes = new HashMap<>();
    private final AUIDNameResolver anameresolver;
    private final EventHandler evthandler;

    /**
     * Resolves a AUID into a local name
     */
    public static interface AUIDNameResolver {

        /**
         * Retrieves a local name for the provided AUID
         *
         * @param enumid AUID
         *
         * @return Local name of the AUID
         */
        String getLocalName(AUID enumid);
    }

    /**
     * Instantiates a FragmentBuilder. If the anamresolver argument is not null,
     * the FragmentBuilder will attempt to resolve the name of each AUID it
     * writes to the output and add it as an XML comment. If the evthandler
     * argument is not null, the Fragment builder will call back the caller with
     * events it encounters as it transforms a Triplet.
     *
     * @param defresolver Maps Group Keys to MetaDictionary definitions. Must
     * not be null;
     * @param setresolver Resolves Strong References to groups. Must not be
     * null.
     * @param anameresolver Resolves a AUID to a human-readable symbol. May be
     * null.
     * @param evthandler Calls back the caller when an event occurs. May be
     * null.
     */
    public FragmentBuilder(DefinitionResolver defresolver,
        Map<UUID, Set> setresolver,
        AUIDNameResolver anameresolver,
        EventHandler evthandler) {

        if (defresolver == null || setresolver == null) {
            throw new IllegalArgumentException();

        }

        this.defresolver = defresolver;
        this.setresolver = setresolver;
        this.anameresolver = anameresolver;
        this.evthandler = evthandler;
    }

    /**
     * Instantiates a FragmentBuilder
     *
     * @deprecated Replaced by
     * {@link FragmentBuilder(DefinitionResolver, Map<UUID, Set>,
     * AUIDNameResolver, EventHandler)}. This constructor does not allow the
     * caller to provide an event handler, and instead uses java.util.logging to
     * output events.
     *
     * @param defresolver Maps Group Keys to MetaDictionary definitions. Must
     * not be null;
     * @param setresolver Resolves Strong References to groups. Must not be
     * null.
     * @param anameresolver Resolves a AUID to a human-readable symbol. May be
     * null.
     */
    public FragmentBuilder(DefinitionResolver defresolver,
        Map<UUID, Set> setresolver,
        AUIDNameResolver anameresolver) {

        this(defresolver,
            setresolver,
            null,
            new EventHandler() {

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
            }
        );
    }

    /**
     * Instantiates a FragmentBuilder.
     *
     * @deprecated Replaced by
     * {@link FragmentBuilder(DefinitionResolver, Map<UUID, Set>,
     * AUIDNameResolver, EventHandler)}. This constructor does not allow the
     * caller to provide an event handler, and instead uses java.util.logging to
     * output events.
     *
     * @param defresolver Maps Group Keys to MetaDictionary definitions. Must
     * not be null;
     * @param setresolver Resolves Strong References to groups. Must not be
     * null.
     */
    public FragmentBuilder(DefinitionResolver defresolver, Map<UUID, Set> setresolver) {
        this(defresolver, setresolver, null);
    }

    /**
     * Creates a RegXML Fragment, represented an XML DOM Document Fragment
     *
     * @param group KLV Group for which the Fragment will be generated.
     * @param document Document from which the XML DOM Document Fragment will be
     * created.
     *
     * @return XML DOM Document Fragment containing a single RegXML Fragment
     *
     * @throws KLVException
     * @throws com.sandflow.smpte.regxml.FragmentBuilder.RuleException
     */
    public DocumentFragment fromTriplet(Group group, Document document) throws KLVException, RuleException {

        DocumentFragment df = document.createDocumentFragment();

        applyRule3(df, group);

        /* NOTE: Hack to clean-up namespace prefixes */
        for (Map.Entry<URI, String> entry : nsprefixes.entrySet()) {
            ((Element) df.getFirstChild()).setAttributeNS(XMLNS_NS, "xmlns:" + entry.getValue(), entry.getKey().toString());
        }

        return df;
    }

    private String getPrefix(URI ns) {
        String prefix = this.nsprefixes.get(ns);

        /* if prefix does not exist, create one */
        if (prefix == null) {
            prefix = "r" + this.nsprefixes.size();

            this.nsprefixes.put(ns, prefix);
        }

        return prefix;
    }

    String getPrefix(String ns) {

        try {
            return getPrefix(new URI(ns));
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    void addInformativeComment(Element element, String comment) {
        element.appendChild(element.getOwnerDocument().createComment(comment));
    }

    void handleEvent(EventHandler.Event evt) throws RuleException {

        if (evthandler != null) {

            if (! evthandler.handle(evt) || evt.getSeverity() == EventHandler.Severity.FATAL) {
                
                /* die on FATAL events or if requested by the handler */

                throw new RuleException(evt.getMessage());

            }
            
        } else if (evt.getSeverity() == EventHandler.Severity.ERROR ||
            evt.getSeverity() == EventHandler.Severity.FATAL) {
            
            /* if no event handler was provided, die on FATAL and ERROR events */
            
            throw new RuleException(evt.getMessage());
            
        }
        
    }

    void applyRule3(Node node, Group group) throws RuleException {

        Definition definition = defresolver.getDefinition(new AUID(group.getKey()));

        if (definition == null) {

            handleEvent(new Event(
                EventKind.UNKNOWN_GROUP,
                String.format(
                    "Unknown Group UL %s",
                    group.getKey().toString()
                )
            )
            );

            return;
        }

        if (definition.getIdentification().asUL().getVersion() != group.getKey().getVersion()) {

            handleEvent(new Event(
                EventKind.VERSION_BYTE_MISMATCH,
                String.format(
                    "Group UL %s in file does not have the same version as in the register (0x%02x)",
                    group.getKey(),
                    definition.getIdentification().asUL().getVersion()
                )
            )
            );

        }

        Element objelem = node.getOwnerDocument().createElementNS(definition.getNamespace().toString(), definition.getSymbol());

        node.appendChild(objelem);

        objelem.setPrefix(getPrefix(definition.getNamespace()));

        for (Triplet item : group.getItems()) {

            /* skip if the property is not defined in the registers */
            Definition itemdef = defresolver.getDefinition(new AUID(item.getKey()));

            if (itemdef == null) {

                handleEvent(
                    new Event(
                        EventKind.UNKNOWN_PROPERTY,
                        String.format(
                            "Unknown property %s",
                            item.getKey().toString()
                        ),
                        String.format(
                            "Group %s",
                            definition.getSymbol()
                        )
                    )
                );

                /* inserts the full value of the dark property as a comment */
                addInformativeComment(
                    objelem,
                    String.format(
                        "Unknown property\nKey: %s\nData: %s",
                        item.getKey().toString(),
                        bytesToString(item.getValue())
                    )
                );

                continue;

            }

            /* make sure this is a property definition */
            if (!(itemdef instanceof PropertyDefinition)) {

                Event evt = new Event(
                    EventKind.UNEXPECTED_DEFINITION,
                    String.format(
                        "Item %s is not a property",
                        item.getKey().toString()
                    ),
                    String.format(
                        "Group %s",
                        definition.getSymbol()
                    )
                );

                handleEvent(evt);

                addInformativeComment(objelem, evt.getReason());

                continue;
            }

            /* warn if version byte of the property does not match the register version byte  */
            if (itemdef.getIdentification().asUL().getVersion() != item.getKey().getVersion()) {

                handleEvent(new Event(
                    EventKind.VERSION_BYTE_MISMATCH,
                    String.format(
                        "Property UL %s in file does not have the same version as in the register (0x%02x)",
                        item.getKey().toString(),
                        itemdef.getIdentification().asUL().getVersion()
                    )
                )
                );

            }

            Element elem = node.getOwnerDocument().createElementNS(itemdef.getNamespace().toString(), itemdef.getSymbol());

            objelem.appendChild(elem);

            elem.setPrefix(getPrefix(itemdef.getNamespace()));

            /* write the property */
            applyRule4(elem, new MXFInputStream(item.getValueAsStream()), itemdef);

            /* detect cyclic references  */
            if (item.getKey().equals(INSTANCE_UID_ITEM_UL)) {

                String iidns = objelem.getLastChild().getNamespaceURI();
                String iidname = objelem.getLastChild().getLocalName();
                String iid = objelem.getLastChild().getTextContent();

                /* look for identical instanceID in parent elements */
                Node parent = node;

                while (parent.getNodeType() == Node.ELEMENT_NODE) {

                    for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {

                        if (n.getNodeType() == Node.ELEMENT_NODE
                            && iidname.equals(n.getLocalName())
                            && iidns.equals(n.getNamespaceURI())
                            && iid.equals(n.getTextContent())) {

                            Event evt = new Event(
                                EventKind.CIRCULAR_STRONG_REFERENCE,
                                String.format(
                                    "Circular Strong Reference to Set UID %s",
                                    iid
                                ),
                                String.format(
                                    "Group %s",
                                    definition.getSymbol()
                                )
                            );

                            handleEvent(evt);

                            addInformativeComment(
                                (Element) node,
                                evt.getReason()
                            );

                            return;
                        }
                    }

                    parent = parent.getParentNode();
                }

            }

            /* add reg:uid if property is a unique ID */
            if (((PropertyDefinition) itemdef).isUniqueIdentifier()) {

                Attr attr = node.getOwnerDocument().createAttributeNS(REGXML_NS, UID_ATTR);

                attr.setPrefix(getPrefix(REGXML_NS));
                attr.setTextContent(objelem.getLastChild().getTextContent());

                objelem.setAttributeNodeNS(attr);
            }

        }

    }

    void applyRule4(Element element, MXFInputStream value, Definition propdef) throws RuleException {

        try {

            if (propdef.getIdentification().equals(ByteOrder_UL)) {

                int byteorder;

                byteorder = value.readUnsignedShort();

                /* ISSUE: ST 2001-1 inverses these constants */
                if (byteorder == 0x4D4D) {

                    element.setTextContent(BYTEORDER_BE);

                } else if (byteorder == 0x4949) {

                    element.setTextContent(BYTEORDER_LE);

                    Event evt = new Event(
                        EventKind.UNEXPECTED_BYTE_ORDER,
                        "ByteOrder property set to little-endian: either the property is set"
                        + "incorrectly or the file does not conform to MXF. Processing will"
                        + "assume a big-endian byte order going forward."
                    );

                    handleEvent(evt);

                    addInformativeComment(element, evt.getReason());

                } else {
                    throw new RuleException("Unknown ByteOrder value.");
                }

            } else {

                if (propdef instanceof PropertyAliasDefinition) {
                    propdef = defresolver.getDefinition(((PropertyAliasDefinition) propdef).getOriginalProperty());
                }

                Definition typedef = findBaseDefinition(defresolver.getDefinition(((PropertyDefinition) propdef).getType()));

                /* return if no type definition is found */
                if (typedef == null) {

                    Event evt = new Event(
                        EventKind.UNKNOWN_TYPE,
                        String.format(
                            "Type %s not found",
                            ((PropertyDefinition) propdef).getType().toString()
                        ),
                        String.format(
                            "Property %s",
                            propdef.getSymbol()
                        )
                    );

                    handleEvent(evt);

                    addInformativeComment(element, evt.getReason());

                    return;

                }

                if (propdef.getIdentification().equals(PrimaryPackage_UL)) {

                    /* EXCEPTION: PrimaryPackage is encoded as the Instance UUID of the target set
                     but needs to be the UMID contained in the unique ID of the target set */
                    UUID uuid = value.readUUID();

                    /* is this a local reference through Instance ID? */
                    Group g = setresolver.get(uuid);

                    if (g != null) {

                        boolean foundUniqueID = false;

                        /* find the unique identifier in the group */
                        for (Triplet item : g.getItems()) {

                            Definition itemdef = defresolver.getDefinition(new AUID(item.getKey()));

                            if (itemdef != null
                                && itemdef instanceof PropertyDefinition
                                && ((PropertyDefinition) itemdef).isUniqueIdentifier()) {

                                applyRule4(element, new MXFInputStream(item.getValueAsStream()), itemdef);

                                foundUniqueID = true;

                                break;

                            }

                        }

                        if (foundUniqueID != true) {

                            Event evt = new Event(
                                EventKind.MISSING_UNIQUE,
                                String.format(
                                    "Target Primary Package with Instance UID %s has no IsUnique element.",
                                    uuid.toString()
                                ),
                                String.format(
                                    "Property %s",
                                    propdef.getSymbol()
                                )
                            );

                            handleEvent(evt);

                            addInformativeComment(element, evt.getReason());

                        }

                    } else {

                        Event evt = new Event(
                            EventKind.MISSING_PRIMARY_PACKAGE,
                            String.format(
                                "Target Primary Package with Instance UID %s not found",
                                uuid.toString()
                            ),
                            String.format(
                                "Property %s",
                                propdef.getSymbol()
                            )
                        );

                        handleEvent(evt);

                        addInformativeComment(element, evt.getReason());

                    }

                } else {

                    if (propdef.getIdentification().equals(LinkedGenerationID_UL)
                        || propdef.getIdentification().equals(GenerationID_UL)
                        || propdef.getIdentification().equals(ApplicationProductID_UL)) {

                        /* EXCEPTION: LinkedGenerationID, GenerationID and ApplicationProductID
                         are encoded using UUID */
                        typedef = defresolver.getDefinition(new AUID(UUID_UL));
                    }

                    applyRule5(element, value, typedef);
                }
            }

        } catch (EOFException eof) {

            Event evt = new Event(
                EventKind.VALUE_LENGTH_MISMATCH,
                "Value too short",
                String.format(
                    "Property %s",
                    propdef.getSymbol()
                )
            );

            handleEvent(evt);

            addInformativeComment(element, evt.getReason());

        } catch (IOException ioe) {

            throw new RuleException(ioe);

        }

    }

    void applyRule5(Element element, MXFInputStream value, Definition definition) throws RuleException, IOException {

        if (definition instanceof CharacterTypeDefinition) {
            applyRule5_1(element, value, (CharacterTypeDefinition) definition);
        } else if (definition instanceof EnumerationTypeDefinition) {
            applyRule5_2(element, value, (EnumerationTypeDefinition) definition);
        } else if (definition instanceof ExtendibleEnumerationTypeDefinition) {
            applyRule5_3(element, value, (ExtendibleEnumerationTypeDefinition) definition);
        } else if (definition instanceof FixedArrayTypeDefinition) {
            applyRule5_4(element, value, (FixedArrayTypeDefinition) definition);
        } else if (definition instanceof IndirectTypeDefinition) {
            applyRule5_5(element, value, (IndirectTypeDefinition) definition);
        } else if (definition instanceof IntegerTypeDefinition) {
            applyRule5_6(element, value, (IntegerTypeDefinition) definition);
        } else if (definition instanceof OpaqueTypeDefinition) {
            applyRule5_7(element, value, (OpaqueTypeDefinition) definition);
        } else if (definition instanceof RecordTypeDefinition) {
            applyRule5_8(element, value, (RecordTypeDefinition) definition);
        } else if (definition instanceof RenameTypeDefinition) {
            applyRule5_9(element, value, (RenameTypeDefinition) definition);
        } else if (definition instanceof SetTypeDefinition) {
            applyRule5_10(element, value, (SetTypeDefinition) definition);
        } else if (definition instanceof StreamTypeDefinition) {
            applyRule5_11(element, value, (StreamTypeDefinition) definition);
        } else if (definition instanceof StringTypeDefinition) {
            applyRule5_12(element, value, (StringTypeDefinition) definition);
        } else if (definition instanceof StrongReferenceTypeDefinition) {
            applyRule5_13(element, value, (StrongReferenceTypeDefinition) definition);
        } else if (definition instanceof VariableArrayTypeDefinition) {
            applyRule5_14(element, value, (VariableArrayTypeDefinition) definition);
        } else if (definition instanceof WeakReferenceTypeDefinition) {
            applyRule5_15(element, value, (WeakReferenceTypeDefinition) definition);
        } else if (definition instanceof FloatTypeDefinition) {
            applyRule5_alpha(element, value, (FloatTypeDefinition) definition);
        } else if (definition instanceof LensSerialFloatTypeDefinition) {
            applyRule5_beta(element, value, (LensSerialFloatTypeDefinition) definition);
        } else {

            throw new RuleException(
                String.format(
                    "Unknown Definition %s in Rule 5.",
                    definition.getClass().toString()
                )
            );

        }

    }

    private void readCharacters(Element element, MXFInputStream value, CharacterTypeDefinition definition, boolean removeTrailingZeroes) throws RuleException, IOException {

        StringBuilder sb = new StringBuilder();

        Reader in = null;

        if (definition.getIdentification().equals(Character_UL)) {

            if (value.getByteorder() == ByteOrder.BIG_ENDIAN) {

                in = new InputStreamReader(value, "UTF-16BE");

            } else {

                in = new InputStreamReader(value, "UTF-16LE");

            }

        } else if (definition.getIdentification().equals(Char_UL)) {

            in = new InputStreamReader(value, "US-ASCII");

        } else if (definition.getIdentification().equals(UTF8Character_UL)) {

            /* NOTE: Use of UTF-8 character encoding is specified in RP 2057 */
            in = new InputStreamReader(value, "UTF-8");

        } else {

            Event evt = new Event(
                EventKind.UNSUPPORTED_CHAR_TYPE,
                String.format(
                    "Character type %s is not supported",
                    definition.getSymbol()
                )
            );

            handleEvent(evt);

            addInformativeComment(element, evt.getReason());

            return;

        }

        char[] chars = new char[32];

        for (int c; (c = in.read(chars)) != -1;) {
            sb.append(chars, 0, c);
        }

        if (removeTrailingZeroes) {

            /* remove trailing zeroes if any */
            int nullpos = sb.indexOf("\0");

            if (nullpos > -1) {
                sb.setLength(nullpos);
            }
        }

        element.setTextContent(sb.toString());
    }

    void applyRule5_1(Element element, MXFInputStream value, CharacterTypeDefinition definition) throws RuleException, IOException {

        readCharacters(element, value, definition, false /* do not remove trailing zeroes for a single char */);

    }

    void applyRule5_2(Element element, MXFInputStream value, EnumerationTypeDefinition definition) throws RuleException, IOException {

        try {

            Definition bdef = findBaseDefinition(defresolver.getDefinition(definition.getElementType()));

            if (!(bdef instanceof IntegerTypeDefinition)) {

                Event evt = new Event(
                    EventKind.UNSUPPORTED_ENUM_TYPE,
                    "Enum does not have an Integer base type.",
                    String.format(
                        "Enum %s",
                        definition.getSymbol()
                    )
                );

                handleEvent(evt);

                addInformativeComment(element, evt.getReason());

                return;
            }

            IntegerTypeDefinition idef = (IntegerTypeDefinition) bdef;

            int len = 0;

            if (definition.getIdentification().equals(ProductReleaseType_UL)) {

                /* EXCEPTION: ProductReleaseType_UL is listed as
                 a UInt8 enum but encoded as a UInt16 */
                len = 2;

            } else {
                switch (idef.getSize()) {
                    case ONE:
                        len = 1;
                        break;
                    case TWO:
                        len = 2;
                        break;
                    case FOUR:
                        len = 4;
                        break;
                    case EIGHT:
                        len = 8;
                        break;
                }
            }

            byte[] val = new byte[len];

            int br = value.read(val);

            String str = null;

            if (br == 0) {

                str = "ERROR";

                Event evt = new Event(
                    EventKind.VALUE_LENGTH_MISMATCH,
                    "No data",
                    String.format(
                        "Enum %s",
                        definition.getSymbol()
                    )
                );

                handleEvent(evt);

                addInformativeComment(element, evt.getReason());

            } else {

                /* always try to read the value even if the length is not as expected */
                BigInteger bi = idef.isSigned() ? new BigInteger(val) : new BigInteger(1, val);

                if (definition.getElementType().equals(Boolean_UL)) {

                    /* find the "true" enum element */
                    /* MXF can encode "true" as any value other than 0 */
                    for (EnumerationTypeDefinition.Element e : definition.getElements()) {
                        if ((bi.intValue() == 0 && e.getValue() == 0) || (bi.intValue() != 0 && e.getValue() == 1)) {
                            str = e.getName();
                        }
                    }

                } else {

                    for (EnumerationTypeDefinition.Element e : definition.getElements()) {
                        if (e.getValue() == bi.intValue()) {
                            str = e.getName();
                        }
                    }

                }

                if (str == null) {

                    str = "UNDEFINED";

                    Event evt = new Event(
                        EventKind.UNKNOWN_ENUM_VALUE,
                        String.format(
                            "Undefined value %d",
                            bi.intValue()
                        ),
                        String.format(
                            "Enum %s",
                            definition.getSymbol()
                        )
                    );

                    handleEvent(evt);

                    addInformativeComment(element, evt.getReason());

                } else if (br != len) {

                    Event evt = new Event(
                        EventKind.VALUE_LENGTH_MISMATCH,
                        String.format(
                            "Incorrect length: expected %d and received %d",
                            len,
                            br
                        ),
                        String.format(
                            "Enumeration %s",
                            definition.getSymbol()
                        )
                    );

                    handleEvent(evt);

                    addInformativeComment(element, evt.getReason());

                }
            }

            element.setTextContent(str);

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    void appendCommentWithAUIDName(AUIDNameResolver anr, AUID auid, Element elem) {
        if (this.anameresolver != null) {

            String ename = this.anameresolver.getLocalName(auid);

            if (ename != null) {
                elem.appendChild(elem.getOwnerDocument().createComment(ename));
            }

        }
    }

    void applyRule5_3(Element element, MXFInputStream value, ExtendibleEnumerationTypeDefinition definition) throws RuleException, IOException {

        try {

            UL ul = value.readUL();

            /* NOTE: ST 2001-1 XML Schema does not allow ULs as values for Extendible Enumerations, which
             defeats the purpose of the type. This issue could be addressed at the next revision opportunity. */
            element.setTextContent(ul.toString());

            appendCommentWithAUIDName(anameresolver, new AUID(ul), element);

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    void applyRule5_4(Element element, MXFInputStream value, FixedArrayTypeDefinition definition) throws RuleException, IOException {

        if (definition.getIdentification().equals(UUID_UL)) {

            UUID uuid = value.readUUID();

            element.setTextContent(uuid.toString());

        } else {

            Definition typedef = findBaseDefinition(defresolver.getDefinition(definition.getElementType()));

            applyCoreRule5_4(element, value, typedef, definition.getElementCount());

        }
    }

    void applyCoreRule5_4(Element element, MXFInputStream value, Definition typedef, int elementcount) throws RuleException, IOException {

        for (int i = 0; i < elementcount; i++) {

            if (typedef instanceof StrongReferenceTypeDefinition) {

                /* Rule 5.4.1 */
                applyRule5_13(element, value, (StrongReferenceTypeDefinition) typedef);

            } else {

                /* Rule 5.4.2 */
                Element elem = element.getOwnerDocument().createElementNS(typedef.getNamespace().toString(), typedef.getSymbol());

                elem.setPrefix(getPrefix(typedef.getNamespace()));

                applyRule5(elem, value, typedef);

                element.appendChild(elem);

            }
        }
    }

    void applyRule5_5(Element element, MXFInputStream value, IndirectTypeDefinition definition) throws RuleException, IOException {

        /* see https://github.com/sandflow/regxmllib/issues/74 for a discussion on Indirect Type */
        ByteOrder bo;

        switch (value.readUnsignedByte()) {
            case 0x4c /* little endian */:
                bo = ByteOrder.LITTLE_ENDIAN;
                break;
            case 0x42 /* big endian */:
                bo = ByteOrder.BIG_ENDIAN;
                break;
            default:
                throw new RuleException("Unknown Indirect Byte Order value.");
        }

        MXFInputStream orderedval = new MXFInputStream(value, bo);

        IDAU idau = orderedval.readIDAU();

        if (idau == null) {

            Event evt = new Event(
                EventKind.INVALID_IDAU,
                "Invalid IDAU",
                String.format(
                    "Indirect Type %s",
                    definition.getSymbol()
                )
            );

            handleEvent(evt);

            addInformativeComment(element, evt.getReason());

            return;
        }

        AUID auid = idau.asAUID();

        Definition def = (Definition) defresolver.getDefinition(auid);

        if (def == null) {

            Event evt = new Event(
                EventKind.UNKNOWN_TYPE,
                String.format(
                    "No definition found for indirect type %s.",
                    auid.toString()
                ),
                String.format(
                    "Indirect Type %s",
                    definition.getSymbol()
                )
            );

            handleEvent(evt);

            addInformativeComment(element, evt.getReason());

            return;
        }

        // create reg:actualType attribute
        Attr attr = element.getOwnerDocument().createAttributeNS(REGXML_NS, ACTUALTYPE_ATTR);
        attr.setPrefix(getPrefix(REGXML_NS));
        attr.setTextContent(def.getSymbol());
        element.setAttributeNodeNS(attr);

        applyRule5(element, orderedval, def);

    }

    void applyRule5_6(Element element, MXFInputStream value, IntegerTypeDefinition definition) throws RuleException, IOException {

        try {

            int len = 0;

            switch (definition.getSize()) {
                case ONE:
                    len = 1;
                    break;
                case TWO:
                    len = 2;
                    break;
                case FOUR:
                    len = 4;
                    break;
                case EIGHT:
                    len = 8;
                    break;
            }

            byte[] val = new byte[len];

            int br = value.read(val);

            if (br == 0) {

                element.setTextContent("NaN");

                Event evt = new Event(
                    EventKind.VALUE_LENGTH_MISMATCH,
                    "No data",
                    String.format(
                        "Integer %s",
                        definition.getSymbol()
                    )
                );

                handleEvent(evt);

                addInformativeComment(element, evt.getReason());

            } else {

                try {

                    BigInteger bi = definition.isSigned() ? new BigInteger(val) : new BigInteger(1, val);

                    element.setTextContent(bi.toString());

                    if (br != len) {

                        Event evt = new Event(
                            EventKind.VALUE_LENGTH_MISMATCH,
                            String.format(
                                "Incorrect field length: expected %d and parsed %d.",
                                len,
                                br
                            ),
                            String.format(
                                "Integer %s",
                                definition.getSymbol()
                            )
                        );

                        handleEvent(evt);

                        addInformativeComment(element, evt.getReason());

                    }

                } catch (NumberFormatException e) {

                    Event evt = new Event(
                        EventKind.INVALID_INTEGER_VALUE,
                        "Invalid integer value",
                        String.format(
                            "Integer %s",
                            definition.getSymbol()
                        )
                    );

                    handleEvent(evt);

                    addInformativeComment(element, evt.getReason());
                }

            }

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }

    void applyRule5_7(Element element, MXFInputStream value, OpaqueTypeDefinition definition) throws RuleException {

        /* NOTE: Opaque Types are not used in MXF */
        throw new RuleException("Opaque types are not supported.");

    }

    String generateISO8601Time(int hour, int minutes, int seconds, int millis) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%02d:%02d:%02d", hour, minutes, seconds));

        if (millis != 0) {
            sb.append(String.format(".%03d", millis));
        }

        sb.append("Z");

        return sb.toString();
    }

    String generateISO8601Date(int year, int month, int day) {
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    void applyRule5_8(Element element, MXFInputStream value, RecordTypeDefinition definition) throws RuleException, IOException {

        if (definition.getIdentification().equals(AUID_UL)) {

            AUID auid = value.readAUID();

            element.setTextContent(auid.toString());

            appendCommentWithAUIDName(anameresolver, auid, element);

        } else if (definition.getIdentification().equals(DateStruct_UL)) {

            int year = value.readUnsignedShort();
            int month = value.readUnsignedByte();
            int day = value.readUnsignedByte();

            element.setTextContent(generateISO8601Date(year, month, day));

        } else if (definition.getIdentification().equals(PackageID_UL)) {

            UMID umid = value.readUMID();

            element.setTextContent(umid.toString());

        } else if (definition.getIdentification().equals(Rational_UL)) {

            int numerator = value.readInt();
            int denominator = value.readInt();

            element.setTextContent(String.format("%d/%d", numerator, denominator));

        } else if (definition.getIdentification().equals(TimeStruct_UL)) {

            /*INFO: ST 2001-1 and ST 377-1 diverge on the meaning of 'fraction'.
             fraction is msec/4 according to 377-1 */
            int hour = value.readUnsignedByte();
            int minute = value.readUnsignedByte();
            int second = value.readUnsignedByte();
            int fraction = value.readUnsignedByte();

            element.setTextContent(generateISO8601Time(hour, minute, second, 4 * fraction));

        } else if (definition.getIdentification().equals(TimeStamp_UL)) {

            int year = value.readUnsignedShort();
            int month = value.readUnsignedByte();
            int day = value.readUnsignedByte();
            int hour = value.readUnsignedByte();
            int minute = value.readUnsignedByte();
            int second = value.readUnsignedByte();
            int fraction = value.readUnsignedByte();

            element.setTextContent(generateISO8601Date(year, month, day) + "T" + generateISO8601Time(hour, minute, second, 4 * fraction));

        } else if (definition.getIdentification().equals(VersionType_UL)) {

            /* EXCEPTION: registers used Int8 but MXF specifies UInt8 */
            int major = value.readUnsignedByte();
            int minor = value.readUnsignedByte();

            element.setTextContent(String.format("%d.%d", major, minor));

        } else {

            for (RecordTypeDefinition.Member member : definition.getMembers()) {

                Definition itemdef = findBaseDefinition(defresolver.getDefinition(member.getType()));

                Element elem = element.getOwnerDocument().createElementNS(definition.getNamespace().toString(), member.getName());

                elem.setPrefix(getPrefix(definition.getNamespace()));

                applyRule5(elem, value, itemdef);

                element.appendChild(elem);
            }
        }

    }

    void applyRule5_9(Element element, MXFInputStream value, RenameTypeDefinition definition) throws RuleException, IOException {

        Definition rdef = defresolver.getDefinition(definition.getRenamedType());

        applyRule5(element, value, rdef);

    }

    void applyRule5_10(Element element, MXFInputStream value, SetTypeDefinition definition) throws RuleException, IOException {

        Definition typedef = findBaseDefinition(defresolver.getDefinition(definition.getElementType()));

        try {

            DataInputStream dis = new DataInputStream(value);

            long itemcount = dis.readInt() & 0xfffffffL;
            long itemlength = dis.readInt() & 0xfffffffL;

            applyCoreRule5_4(element, value, typedef, (int) itemcount);

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }

    void applyRule5_11(Element element, MXFInputStream value, StreamTypeDefinition definition) throws RuleException {

        throw new RuleException("Rule 5.11 is not supported yet.");

    }

    void applyRule5_12(Element element, MXFInputStream value, StringTypeDefinition definition) throws RuleException, IOException {

        /* Rule 5.12 */
        Definition chrdef = findBaseDefinition(defresolver.getDefinition(definition.getElementType()));

        /* NOTE: ST 2001-1 implies that integer-based strings are supported, but
         does not described semantics.
         */
        if (!(chrdef instanceof CharacterTypeDefinition)) {

            Event evt = new Event(
                EventKind.UNSUPPORTED_STRING_TYPE,
                String.format(
                    "Unsupported String with Element %s",
                    chrdef.getSymbol()
                ),
                String.format(
                    "String %s",
                    definition.getSymbol()
                )
            );

            handleEvent(evt);

            addInformativeComment(element, evt.getReason());

            return;
        }

        readCharacters(
            element,
            value,
            (CharacterTypeDefinition) chrdef,
            true /* remove trailing zeroes */
        );

    }

    void applyRule5_13(Element element, MXFInputStream value, StrongReferenceTypeDefinition definition) throws RuleException, IOException {

        Definition typedef = findBaseDefinition(defresolver.getDefinition(definition.getReferenceType()));

        if (!(typedef instanceof ClassDefinition)) {

            Event evt = new Event(
                EventKind.INVALID_STRONG_REFERENCE_TYPE,
                String.format(
                    "Target %s of Strong Reference Type is not a class",
                    typedef.getSymbol()
                ),
                String.format(
                    "Type %s",
                    definition.getSymbol()
                )
            );

            handleEvent(evt);

            addInformativeComment(element, evt.getReason());

            return;

        }

        UUID uuid = value.readUUID();

        Group g = setresolver.get(uuid);

        if (g != null) {

            applyRule3(element, g);

        } else {

            Event evt = new Event(
                EventKind.STRONG_REFERENCE_NOT_FOUND,
                String.format(
                    "Strong Reference target %s is not found",
                    uuid.toString()
                ),
                String.format(
                    "Type %s",
                    definition.getSymbol()
                )
            );

            handleEvent(evt);

            addInformativeComment(element, evt.getReason());

        }

    }

    void applyRule5_alpha(Element element, MXFInputStream value, FloatTypeDefinition definition) throws RuleException, IOException {

        try {

            DataInputStream dis = new DataInputStream(value);

            double val = 0;

            switch (definition.getSize()) {
                case HALF:

                    val = HalfFloat.toDouble(dis.readUnsignedShort());

                    break;
                case SINGLE:
                    val = dis.readFloat();
                    break;
                case DOUBLE:
                    val = dis.readDouble();
                    break;
            }

            element.setTextContent(Double.toString(val));

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }

    void applyRule5_beta(Element element, MXFInputStream value, LensSerialFloatTypeDefinition definition) throws RuleException {

        throw new RuleException("Lens serial floats not supported.");

    }

    Definition findBaseDefinition(Definition definition) {

        while (definition instanceof RenameTypeDefinition) {
            definition = defresolver.getDefinition(((RenameTypeDefinition) definition).getRenamedType());
        }

        return definition;
    }

    private Collection<PropertyDefinition> getAllMembersOf(ClassDefinition definition) {
        ClassDefinition cdef = definition;

        ArrayList<PropertyDefinition> props = new ArrayList<>();

        while (cdef != null) {

            for (AUID auid : defresolver.getMembersOf(cdef)) {
                props.add((PropertyDefinition) defresolver.getDefinition(auid));
            }

            if (cdef.getParentClass() != null) {
                cdef = (ClassDefinition) defresolver.getDefinition(cdef.getParentClass());
            } else {
                cdef = null;
            }

        }

        return props;
    }

    final static char[] HEXMAP = "0123456789abcdef".toCharArray();

    private String bytesToString(byte[] buffer) {

        char[] out = new char[2 * buffer.length];

        for (int j = 0; j < buffer.length; j++) {

            int v = buffer[j] & 0xFF;
            out[j * 2] = HEXMAP[v >>> 4];
            out[j * 2 + 1] = HEXMAP[v & 0x0F];
        }

        return new String(out);
    }

    void applyRule5_14(Element element, MXFInputStream value, VariableArrayTypeDefinition definition) throws RuleException, IOException {

        Definition typedef = findBaseDefinition(defresolver.getDefinition(definition.getElementType()));

        try {

            DataInputStream dis = new DataInputStream(value);

            if (definition.getSymbol().equals("DataValue")) {

                /* RULE 5.14.2 */
                /* DataValue is string of octets, without number of elements or size of elements */
                byte[] buffer = new byte[32];

                StringBuilder sb = new StringBuilder();

                for (int sz = 0; (sz = dis.read(buffer)) > -1;) {

                    for (int j = 0; j < sz; j++) {

                        int v = buffer[j] & 0xFF;
                        sb.append(HEXMAP[v >>> 4]);
                        sb.append(HEXMAP[v & 0x0F]);
                    }
                }

                element.setTextContent(sb.toString());

            } else {

                Definition base = findBaseDefinition(typedef);

                if (base instanceof CharacterTypeDefinition || base.getName().contains("StringArray")) {

                    /* RULE 5.14.1 */
                    /* INFO: StringArray is not used in MXF (ST 377-1) */
                    throw new RuleException("StringArray not supported.");

                } else {

                    long itemcount = dis.readInt() & 0xfffffffL;
                    long itemlength = dis.readInt() & 0xfffffffL;

                    applyCoreRule5_4(element, value, typedef, (int) itemcount);
                }

            }

        } catch (UnsupportedEncodingException e) {

            throw new RuntimeException(e);

        } catch (EOFException eof) {

            Event evt = new Event(
                EventKind.VALUE_LENGTH_MISMATCH,
                "Value too short",
                String.format(
                    "Array %s",
                    definition.getSymbol()
                )
            );

            handleEvent(evt);

            addInformativeComment(element, evt.getReason());

        }

    }

    void applyRule5_15(Element element, MXFInputStream value, WeakReferenceTypeDefinition typedefinition) throws RuleException {

        ClassDefinition classdef = (ClassDefinition) defresolver.getDefinition(typedefinition.getReferencedType());

        PropertyDefinition uniquepropdef = null;

        for (PropertyDefinition propdef : getAllMembersOf(classdef)) {

            if (propdef.isUniqueIdentifier()) {
                uniquepropdef = propdef;
                break;
            }
        }

        if (uniquepropdef == null) {

            Event evt = new Event(
                EventKind.MISSING_UNIQUE,
                String.format(
                    "Weak reference target %s has no IsUnique element.",
                    classdef.getSymbol()
                ),
                String.format(
                    "Type %s",
                    typedefinition.getSymbol()
                )
            );

            handleEvent(evt);

            addInformativeComment(element, evt.getReason());

            return;
        }

        applyRule4(element, value, uniquepropdef);

    }

    public static class RuleException extends Exception {

        public RuleException(Throwable t) {
            super(t);
        }

        public RuleException(String msg) {
            super(msg);
        }

        public RuleException(String msg, Throwable t) {
            super(msg, t);
        }

    }

}
