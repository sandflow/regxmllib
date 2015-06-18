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
import com.sandflow.smpte.util.HalfFloat;
import com.sandflow.smpte.util.UL;
import com.sandflow.smpte.util.UMID;
import com.sandflow.smpte.util.UUID;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Builds a RegXML Fragment of a single KLV Group, typically a Header Metadata
 * MXF Set, using a collection of MetaDictionary definitions
 */
public class FragmentBuilder {

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
    private static final UL ProductReleaseType_UL = UL.fromURN("urn:smpte:ul:060e2b34.01040101.02010101.00000000");
    private static final UL Boolean_UL = UL.fromURN("urn:smpte:ul:060e2b34.01040101.01040100.00000000");

    private static final String REGXML_NS = "http://sandflow.com/ns/SMPTEST2001-1/baseline";
    private final static String XMLNS_NS = "http://www.w3.org/2000/xmlns/";

    private static final String ACTUALTYPE_ATTR = "actualType";
    private static final String BYTEORDER_ATTR = "byteOrder";
    private static final String BYTEORDER_BE = "BigEndian";
    private static final String UID_ATTR = "uid";

    private final DefinitionResolver defresolver;
    private final Map<UUID, Set> setresolver;
    private final HashMap<URI, String> nsprefixes = new HashMap<>();

    /**
     * Instantiates a FragmentBuilder
     *
     * @param defresolver Map between Group Keys and MetaDictionary definitions
     * @param setresolver Allows Strong References to be resolved
     */
    public FragmentBuilder(DefinitionResolver defresolver, Map<UUID, Set> setresolver) {
        this.defresolver = defresolver;
        this.setresolver = setresolver;
    }

    /**
     * Creates a RegXML Fragment, represented an XML DOM Document Fragment
     *
     * @param group KLV Group for which the Fragment will be generated.
     * @param document Document from which the XML DOM Document Fragment will be
     * created.
     * @return XML DOM Document Fragment containing a single RegXML Fragment
     * @throws ParserConfigurationException
     * @throws KLVException
     * @throws com.sandflow.smpte.regxml.FragmentBuilder.RuleException
     */
    public DocumentFragment fromTriplet(Group group, Document document) throws ParserConfigurationException, KLVException, RuleException {

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

    void applyRule3(Node node, Group group) throws RuleException {

        Definition definition = defresolver.getDefinition(new AUID(group.getKey()));

        if (definition == null) {
            LOG.warning(
                    String.format(
                            "Unknown Group UL = %s",
                            group.getKey().toString()
                    )
            );

            return;
        }

        if (definition.getIdentification().asUL().getVersion() != group.getKey().getVersion()) {
            LOG.warning(
                    String.format(
                            "Group UL %s in file does not have the same version as in the register (0x%02x)",
                            group.getKey(),
                            definition.getIdentification().asUL().getVersion()
                    )
            );
        }

        Element elem = node.getOwnerDocument().createElementNS(definition.getNamespace().toString(), definition.getSymbol());

        node.appendChild(elem);

        elem.setPrefix(getPrefix(definition.getNamespace()));

        for (Triplet item : group.getItems()) {

            if (item.getKey().equals(INSTANCE_UID_ITEM_UL)) {

                MXFInputStream mis = new MXFInputStream(item.getValueAsStream());

                try {
                    UUID uuid = mis.readUUID();

                    String uuidstr = uuid.toString();

                    /* prevent self-references */
                    Node parent = node;

                    do {

                        NamedNodeMap attrs = parent.getAttributes();

                        if (attrs == null) {
                            continue;
                        }

                        Node attr = attrs.getNamedItemNS(REGXML_NS, UID_ATTR);

                        if (attr == null) {
                            continue;
                        }

                        if (uuidstr.equals(attr.getTextContent())) {

                            LOG.warning(
                                    String.format(
                                            "Self-referencing Strong Reference at Group %s with UID %s",
                                            definition.getSymbol(),
                                            uuidstr
                                    )
                            );

                            Comment comment = node.getOwnerDocument().createComment(
                                    String.format(
                                            "Strong Reference %s not found",
                                            uuid.toString()
                                    )
                            );

                            node.appendChild(comment);

                            return;
                        }
                    } while ((parent = parent.getParentNode()) != null);

                    elem.setAttributeNS(
                            REGXML_NS,
                            UID_ATTR,
                            uuidstr
                    );

                } catch (IOException ex) {
                    throw new RuleException(ex);
                }

            } else {

                Definition itemdef = defresolver.getDefinition(new AUID(item.getKey()));

                if (itemdef == null) {
                    LOG.warning(
                            String.format(
                                    "Unknown property UL = %s at group %s",
                                    item.getKey().toString(),
                                    definition.getSymbol()
                            )
                    );

                    elem.appendChild(
                            elem.getOwnerDocument().createComment(
                                    String.format(
                                            "Unknow Item\nKey: %s\nData: %s",
                                            item.getKey().toString(),
                                            bytesToString(item.getValue())
                                    )
                            )
                    );
                } else {

                    if (itemdef.getIdentification().asUL().getVersion() != item.getKey().getVersion()) {
                        LOG.warning(
                                String.format(
                                        "Property UL %s in file does not have the same version as in the register (0x%02x)",
                                        item.getKey().toString(),
                                        itemdef.getIdentification().asUL().getVersion()
                                )
                        );
                    }

                    applyRule4(elem, item.getValueAsStream(), itemdef);
                }

            }

        }

    }

    void applyRule4(Element element, InputStream value, Definition definition) throws RuleException {

        Element elem = element.getOwnerDocument().createElementNS(definition.getNamespace().toString(), definition.getSymbol());

        element.appendChild(elem);

        elem.setPrefix(getPrefix(definition.getNamespace()));

        if (definition.getIdentification().equals(ByteOrder_UL)) {
            MXFInputStream kis = new MXFInputStream(value);

            int byteorder;

            try {
                byteorder = kis.readInt();
            } catch (IOException ex) {
                throw new RuleException(ex);
            }

            if (byteorder == 0x4949) {
                elem.setTextContent("BigEndian");
            } else if (byteorder == 0x4D4D) {
                elem.setTextContent("LittleEndian");
            } else {
                throw new RuleException("Unknown ByteOrder value.");
            }

        } else {

            if (definition instanceof PropertyAliasDefinition) {
                definition = defresolver.getDefinition(((PropertyAliasDefinition) definition).getOriginalProperty());
            }

            Definition typedef = findBaseDefinition(defresolver.getDefinition(((PropertyDefinition) definition).getType()));

            if (typedef == null) {
                throw new RuleException(
                        String.format(
                                "Type %s not found at %s.",
                                ((PropertyDefinition) definition).getType().toString(),
                                definition.getSymbol()
                        )
                );
            }

            applyRule5(elem, value, typedef);
        }

    }

    void applyRule5(Element element, InputStream value, Definition definition) throws RuleException {

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
                            "Illegal Definition %s in Rule 5.",
                            definition.getClass().toString()
                    )
            );

        }

    }

    private void readCharacters(InputStream value, CharacterTypeDefinition definition, StringBuilder sb) throws RuleException {

        try {

            Reader in = null;

            if (definition.getIdentification().equals(Character_UL)) {
                in = new InputStreamReader(value, "UTF-16BE");
            } else if (definition.getIdentification().equals(Char_UL)) {
                in = new InputStreamReader(value, "US-ASCII");
            } else {
                throw new RuleException(
                        String.format("Character  type %s not supported",
                                definition.getIdentification().toString()
                        )
                );
            }

            char[] chars = new char[32];

            for (int c; (c = in.read(chars)) != -1;) {
                sb.append(chars, 0, c);
            }

        } catch (IOException ioe) {
            throw new RuleException(ioe);
        }
    }

    void applyRule5_1(Element element, InputStream value, CharacterTypeDefinition definition) throws RuleException {

        StringBuilder sb = new StringBuilder();

        readCharacters(value, definition, sb);

        element.setTextContent(sb.toString());

    }

    void applyRule5_2(Element element, InputStream value, EnumerationTypeDefinition definition) throws RuleException {

        try {

            Definition bdef = findBaseDefinition(defresolver.getDefinition(definition.getElementType()));

            if (!(bdef instanceof IntegerTypeDefinition)) {
                throw new RuleException(
                        String.format("Enum %s does not have an Integer base type.",
                                definition.getIdentification().toString()
                        ));
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

            value.read(val);

            BigInteger bi = idef.isSigned() ? new BigInteger(val) : new BigInteger(1, val);

            String str = null;

            for (EnumerationTypeDefinition.Element e : definition.getElements()) {
                if (e.getValue() == bi.intValue()) {
                    str = e.getName();
                }
            }

            if (str == null) {

                if (definition.getElementType().equals(Boolean_UL)) {

                    /* find the "true" enum element */
                    /* MXF can encode "true" as any value other than 0 */
                    for (EnumerationTypeDefinition.Element e : definition.getElements()) {
                        if (e.getValue() == 1) {
                            str = e.getName();
                        }
                    }

                } else {

                    str = "ERROR";

                    LOG.warning(
                            String.format(
                                    "Undefined value %d for Enumeration %s.",
                                    bi.intValue(),
                                    definition.getIdentification()
                            )
                    );
                }
            }

            element.setTextContent(str);

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException ioe) {
            throw new RuleException(ioe);
        }
    }

    void applyRule5_3(Element element, InputStream value, ExtendibleEnumerationTypeDefinition definition) throws RuleException {

        try {

            MXFInputStream ki = new MXFInputStream(value);

            UL ul = ki.readUL();

            /* NOTE: ST 2001-1 XML Schema does not allow ULs as values for Extendible Enumerations, which
            defeats the purpose of the type. This issue could be addressed at the next revision opportunity. */
            
            element.setTextContent(ul.toString());

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException ioe) {
            throw new RuleException(ioe);
        }
    }

    void applyRule5_4(Element element, InputStream value, FixedArrayTypeDefinition definition) throws RuleException {

        if (definition.getIdentification().equals(UUID_UL)) {

            try {
                MXFInputStream kis = new MXFInputStream(value);

                UUID uuid = kis.readUUID();

                element.setTextContent(uuid.toString());

            } catch (IOException e) {
                throw new RuleException(e);
            }
        } else {

            Definition typedef = findBaseDefinition(defresolver.getDefinition(definition.getElementType()));

            applyCoreRule5_4(element, value, typedef, definition.getElementCount());

        }
    }

    void applyCoreRule5_4(Element element, InputStream value, Definition typedef, int elementcount) throws RuleException {

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

    void applyRule5_5(Element element, InputStream value, IndirectTypeDefinition definition) throws RuleException {

        /* INFO: Indirect type is not used in MXF (ST 377-1) */
        
        throw new RuleException("Indirect type not supported.");

    }

    void applyRule5_6(Element element, InputStream value, IntegerTypeDefinition definition) throws RuleException {

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

            value.read(val);

            BigInteger bi = definition.isSigned() ? new BigInteger(val) : new BigInteger(1, val);
            element.setTextContent(bi.toString());

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException ioe) {
            throw new RuleException(ioe);
        }

    }

    void applyRule5_7(Element element, InputStream value, OpaqueTypeDefinition definition) throws RuleException {

        /* NOTE: Opaque Types are not used in MXF */
        
        throw new RuleException("Opaque types are not supported.");
        
    }
    
    String generateISO8601Time(int hour, int minutes, int seconds, int millis) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(String.format("%02d:%02d:%02d", hour, minutes, seconds));
        
        if (millis != 0) sb.append(String.format(".%03dZ", millis));
        
        return  sb.toString();
    }
    
    String generateISO8601Date(int year, int month, int day) {
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    void applyRule5_8(Element element, InputStream value, RecordTypeDefinition definition) throws RuleException {

        try {

            MXFInputStream kis = new MXFInputStream(value);

            if (definition.getIdentification().equals(AUID_UL)) {

                AUID auid = kis.readAUID();

                element.setTextContent(auid.toString());

            } else if (definition.getIdentification().equals(DateStruct_UL)) {

                int year = kis.readUnsignedShort();
                int month = kis.readUnsignedByte();
                int day = kis.readUnsignedByte();
                
                element.setTextContent(generateISO8601Date(year, month, day));

            } else if (definition.getIdentification().equals(PackageID_UL)) {

                UMID umid = kis.readUMID();

                element.setTextContent(umid.toString());

            } else if (definition.getIdentification().equals(Rational_UL)) {

                int numerator = kis.readInt();
                int denominator = kis.readInt();

                element.setTextContent(String.format("%d/%d", numerator, denominator));

            } else if (definition.getIdentification().equals(TimeStruct_UL)) {

                /*INFO: ST 2001-1 and ST 377-1 diverge on the meaning of 'fraction'.
                fraction is msec/4 according to 377-1 */
                
                int hour = kis.readUnsignedByte();
                int minute = kis.readUnsignedByte();
                int second = kis.readUnsignedByte();
                int fraction = kis.readUnsignedByte();

                /*LocalTime lt = LocalTime.of(hour, minute, second, fraction * 4000000);

                OffsetTime ot = OffsetTime.of(lt, ZoneOffset.UTC);

                element.setTextContent(ot.toString());*/
                
                element.setTextContent(generateISO8601Time(hour, minute, second, 4 * fraction));

            } else if (definition.getIdentification().equals(TimeStamp_UL)) {

                int year = kis.readUnsignedShort();
                int month = kis.readUnsignedByte();
                int day = kis.readUnsignedByte();
                int hour = kis.readUnsignedByte();
                int minute = kis.readUnsignedByte();
                int second = kis.readUnsignedByte();
                int fraction = kis.readUnsignedByte();

                /*LocalDateTime ldt = LocalDateTime.of(year, month, day, hour, minute, second, fraction * 4000000);

                OffsetDateTime odt = OffsetDateTime.of(ldt, ZoneOffset.UTC);

                element.setTextContent(odt.toString());*/
                
                element.setTextContent(generateISO8601Date(year, month, day) + "T" + generateISO8601Time(hour, minute, second, 4 * fraction));

            } else if (definition.getIdentification().equals(VersionType_UL)) {

                /* EXCEPTION: registers used Int8 but MXF specifies UInt8 */
                int major = kis.readUnsignedByte();
                int minor = kis.readUnsignedByte();

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

        } catch (IOException ioe) {
            throw new RuleException(ioe);
        }

    }

    void applyRule5_9(Element element, InputStream value, RenameTypeDefinition definition) throws RuleException {

        Definition rdef = defresolver.getDefinition(definition.getRenamedType());

        applyRule5(element, value, rdef);

    }

    void applyRule5_10(Element element, InputStream value, SetTypeDefinition definition) throws RuleException {

        Definition typedef = findBaseDefinition(defresolver.getDefinition(definition.getElementType()));

        try {

            DataInputStream dis = new DataInputStream(value);

            long itemcount = dis.readInt() & 0xfffffffL;
            long itemlength = dis.readInt() & 0xfffffffL;

            applyCoreRule5_4(element, value, typedef, (int) itemcount);

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException ioe) {
            /*throw new RuleException(String.format("Value too short for %s", definition.getSymbol()), ioe);*/

            Comment comment = element.getOwnerDocument().createComment(
                    String.format(
                            "Value too short for Type %s",
                            typedef.getSymbol()
                    )
            );

            element.appendChild(comment);
        }

    }

    void applyRule5_11(Element element, InputStream value, StreamTypeDefinition definition) throws RuleException {

        throw new RuleException("Rule 5.11 is not supported yet.");

    }

    void applyRule5_12(Element element, InputStream value, StringTypeDefinition definition) throws RuleException {

        /* Rule 5.12 */
        
        Definition chrdef = findBaseDefinition(defresolver.getDefinition(definition.getElementType()));
        
        /* NOTE: ST 2001-1 implies that integer-based strings are supported, but
           does not described semantics.
        */

        if (!(chrdef instanceof CharacterTypeDefinition)) {
            throw new RuleException(
                    String.format(
                            "String type %s does not have a Character Type as element.",
                            definition.getIdentification().toString()
                    )
            );
        }

        StringBuilder sb = new StringBuilder();

        readCharacters(value, (CharacterTypeDefinition) chrdef, sb);
        
        /* remove trailing zeroes if any */
        
        int nullpos = sb.indexOf("\0");
        
        if (nullpos > -1) sb.setLength(nullpos);

        element.setTextContent(sb.toString());

    }

    void applyRule5_13(Element element, InputStream value, StrongReferenceTypeDefinition definition) throws RuleException {

        Definition typedef = findBaseDefinition(defresolver.getDefinition(definition.getReferenceType()));

        if (!(typedef instanceof ClassDefinition)) {
            throw new RuleException("Rule 5.13 applied to non class.");
        }

        try {

            MXFInputStream kis = new MXFInputStream(value);

            UUID uuid = kis.readUUID();

            Group g = setresolver.get(uuid);

            if (g != null) {

                applyRule3(element, g);

            } else {
                LOG.warning(
                        String.format(
                                "Strong Reference %s not found at %s",
                                uuid.toString(),
                                definition.getSymbol()
                        )
                );

                Comment comment = element.getOwnerDocument().createComment(
                        String.format(
                                "Strong Reference %s not found",
                                uuid.toString()
                        )
                );

                element.appendChild(comment);
            }

        } catch (IOException ioe) {
            throw new RuleException(ioe);
        }
    }

    void applyRule5_alpha(Element element, InputStream value, FloatTypeDefinition definition) throws RuleException {

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
        } catch (IOException ioe) {
            throw new RuleException(ioe);
        }

    }

    void applyRule5_beta(Element element, InputStream value, LensSerialFloatTypeDefinition definition) throws RuleException {

        throw new RuleException("Lens serial floats not supported.");

    }

    Definition findBaseDefinition(Definition definition) {

        while (definition instanceof RenameTypeDefinition) {
            definition = defresolver.getDefinition(((RenameTypeDefinition) definition).getRenamedType());
        }

        return definition;
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

    void applyRule5_14(Element element, InputStream value, VariableArrayTypeDefinition definition) throws RuleException {

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
        } catch (IOException ioe) {
            throw new RuleException(ioe);
        }

    }

    void applyRule5_15(Element element, InputStream value, WeakReferenceTypeDefinition definition) throws RuleException {

        /* INFO: assume that the weak reference is a AUID. */
        
        try {

            MXFInputStream kis = new MXFInputStream(value);

            AUID auid = kis.readAUID();

            element.setTextContent(auid.toString());

        } catch (IOException ioe) {
            throw new RuleException(ioe);
        }
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
