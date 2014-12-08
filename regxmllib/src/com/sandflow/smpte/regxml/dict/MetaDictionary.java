/*
 * Copyright (c) 2014, pal
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
package com.sandflow.smpte.regxml.dict;

import com.sandflow.smpte.regxml.definition.OpaqueTypeDefinition;
import com.sandflow.smpte.regxml.definition.VariableArrayTypeDefinition;
import com.sandflow.smpte.regxml.definition.IndirectTypeDefinition;
import com.sandflow.smpte.regxml.definition.ClassDefinition;
import com.sandflow.smpte.regxml.definition.RenameTypeDefinition;
import com.sandflow.smpte.regxml.definition.CharacterTypeDefinition;
import com.sandflow.smpte.regxml.definition.StreamTypeDefinition;
import com.sandflow.smpte.regxml.definition.PropertyDefinition;
import com.sandflow.smpte.regxml.definition.StrongReferenceTypeDefinition;
import com.sandflow.smpte.regxml.definition.StringTypeDefinition;
import com.sandflow.smpte.regxml.definition.Definition;
import com.sandflow.smpte.regxml.definition.WeakReferenceTypeDefinition;
import com.sandflow.smpte.regxml.definition.SetTypeDefinition;
import com.sandflow.smpte.regxml.definition.ExtendibleEnumerationTypeDefinition;
import com.sandflow.smpte.regxml.definition.EnumerationTypeDefinition;
import com.sandflow.smpte.regxml.definition.PropertyAliasDefinition;
import com.sandflow.smpte.regxml.definition.IntegerTypeDefinition;
import com.sandflow.smpte.regxml.definition.FixedArrayTypeDefinition;
import com.sandflow.smpte.regxml.definition.RecordTypeDefinition;
import com.sandflow.smpte.util.AUID;
import com.sandflow.smpte.util.UL;
import com.sandflow.smpte.util.UUID;
import com.sandflow.smpte.util.xml.UUIDAdapter;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author pal
 */
@XmlRootElement(name = "Baseline")
@XmlAccessorType(XmlAccessType.NONE)
public class MetaDictionary implements DefinitionResolver {

    public final static String ST2001_1_NS = "http://www.smpte-ra.org/schemas/2001-1b/2013/metadict";

    @XmlAttribute(name = "rootElement", required = true)
    private final static String rootElement = "MXF";

    @XmlAttribute(name = "rootObject", required = true)
    private final static String rootObject = "Preface";

    @XmlJavaTypeAdapter(value = UUIDAdapter.class)
    @XmlElement(name = "SchemeID", required = true)
    private UUID schemeID;

    @XmlElement(name = "SchemeURI", required = true)
    private URI schemeURI;

    @XmlElement(name = "Description")
    private String description;

    private final ArrayList<Definition> definitions = new ArrayList<>();
    private final HashMap<AUID, Definition> definitionsByAUID = new HashMap<>();
    private final HashMap<String, Definition> definitionsBySymbol = new HashMap<>();
    private final HashMap<AUID, Set<AUID>> membersOf = new HashMap<>();
    private final HashMap<AUID, Set<AUID>> subclassesOf = new HashMap<>();

    private MetaDictionary() {
    }

    public MetaDictionary(URI scheme) {
        /* BUG: ST 2001-1 does not allow label to be used in multiple enumerations */

        /* TODO: refactor to UUID class */
        MessageDigest digest;

        UUID nsid = UUID.fromURN("urn:uuid:6ba7b810-9dad-11d1-80b4-00c04fd430c8");

        try {
            digest = MessageDigest.getInstance("SHA-1");
            digest.update(nsid.getValue());
            digest.update(scheme.toString().getBytes("ASCII"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }

        byte[] result = digest.digest();

        result[6] = (byte) ((result[6] & 0x0f) | 0xaf);
        result[8] = (byte) ((result[8] & 0x3f) | 0x7f);

        this.schemeID = new UUID(result);
        this.schemeURI = scheme;
    }

    public void add(Definition def) throws IllegalDefinitionException {

        if (!def.getNamespace().equals(this.getSchemeURI())) {
            throw new IllegalDefinitionException("Namespace does not match Metadictionary Scheme URI: " + def.getSymbol());
        }

        AUID defid = createNormalizedAUID(def.getIdentification());

        if (this.definitionsByAUID.put(defid, def) != null) {
            throw new IllegalDefinitionException("Duplicate AUID: " + def.getIdentification());
        }

        if (this.definitionsBySymbol.put(def.getSymbol(), def) != null) {
            throw new IllegalDefinitionException("Duplicate Symbol: " + def.getSymbol());
        }

            if (def instanceof PropertyDefinition) {

                AUID parentauid = createNormalizedAUID(((PropertyDefinition) def).getMemberOf());
                
                
                
                Set<AUID> hs = this.membersOf.get(parentauid);
                
                if (hs == null) {
                   hs = new HashSet<>();
                   this.membersOf.put(parentauid, hs);
                }
                
                hs.add(defid);

            }

            if (def instanceof ClassDefinition && ((ClassDefinition) def).getParentClass() != null) {

                AUID parentauid = createNormalizedAUID(((ClassDefinition) def).getParentClass());

                Set<AUID> hs = this.subclassesOf.get(parentauid);
                
                if (hs == null) {
                   hs = new HashSet<>();
                   this.subclassesOf.put(parentauid, hs);
                }
                
                hs.add(defid);

            }

        this.definitions.add(def);
    }

    public static String getRootElement() {
        return rootElement;
    }

    public static String getRootObject() {
        return rootObject;
    }

    public UUID getSchemeID() {
        return schemeID;
    }

    public URI getSchemeURI() {
        return schemeURI;
    }

    public String getDescription() {
        return description;
    }

    @XmlElementWrapper(name = "MetaDefinitions")
    @XmlElements(value = {
        @XmlElement(name = "ClassDefinition",
                type = ClassDefinition.class),
        @XmlElement(name = "PropertyDefinition",
                type = PropertyDefinition.class),
        @XmlElement(name = "PropertyAliasDefinition",
                type = PropertyAliasDefinition.class),
        @XmlElement(name = "TypeDefinitionCharacter",
                type = CharacterTypeDefinition.class),
        @XmlElement(name = "TypeDefinitionEnumeration",
                type = EnumerationTypeDefinition.class),
        @XmlElement(name = "TypeDefinitionExtendibleEnumeration",
                type = ExtendibleEnumerationTypeDefinition.class),
        @XmlElement(name = "TypeDefinitionFixedArray",
                type = FixedArrayTypeDefinition.class),
        @XmlElement(name = "TypeDefinitionIndirect",
                type = IndirectTypeDefinition.class),
        @XmlElement(name = "TypeDefinitionInteger",
                type = IntegerTypeDefinition.class),
        @XmlElement(name = "TypeDefinitionOpaque",
                type = OpaqueTypeDefinition.class),
        @XmlElement(name = "TypeDefinitionRecord",
                type = RecordTypeDefinition.class),
        @XmlElement(name = "TypeDefinitionRename",
                type = RenameTypeDefinition.class),
        @XmlElement(name = "TypeDefinitionSet",
                type = SetTypeDefinition.class),
        @XmlElement(name = "TypeDefinitionStream",
                type = StreamTypeDefinition.class),
        @XmlElement(name = "TypeDefinitionString",
                type = StringTypeDefinition.class),
        @XmlElement(name = "TypeDefinitionStrongObjectReference",
                type = StrongReferenceTypeDefinition.class),
        @XmlElement(name = "TypeDefinitionVariableArray",
                type = VariableArrayTypeDefinition.class),
        @XmlElement(name = "TypeDefinitionWeakObjectReference",
                type = WeakReferenceTypeDefinition.class)
    })
    public ArrayList<Definition> getDefinitions() {
        return definitions;
    }

    public Definition getDefinition(AUID id) {
        return definitionsByAUID.get(createNormalizedAUID(id));
    }

    public Definition getDefinition(String symbol) {
        return definitionsBySymbol.get(symbol);
    }

    public static AUID createNormalizedAUID(AUID auid) {

        if (auid.isUL()) {

            return new AUID(createNormalizedUL(auid.asUL()));
        } else {
            return auid;
        }
    }

    public static UL createNormalizedUL(UL ul) {
        byte[] value = ul.getValue().clone();
        /* set version to 0 */

        value[7] = 0;

        if (ul.isGroup()) {

            /* set byte 6 to 0x7f */
            value[5] = 0x7f;

        }

        return new UL(value);
    }

    public static String createQualifiedSymbol(String namespace, String symbol) {
        if (namespace == null || namespace.length() == 0) {
            return symbol;
        } else {
            return namespace + " " + symbol;
        }
    }

    public void toXML(Writer writer) throws JAXBException, IOException {

        JAXBContext ctx = JAXBContext.newInstance(MetaDictionary.class);

        Marshaller m = ctx.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(this, writer);
        writer.close();
    }

    public static MetaDictionary fromXML(Reader reader) throws JAXBException, IOException, IllegalDefinitionException {

        JAXBContext ctx = JAXBContext.newInstance(MetaDictionary.class);

        Unmarshaller m = ctx.createUnmarshaller();
        MetaDictionary md = (MetaDictionary) m.unmarshal(reader);

        for (Definition def : md.definitions) {

            /* TODO: can this be factored out? */
            def.setNamespace(md.getSchemeURI());

            AUID defid = createNormalizedAUID(def.getIdentification());

            if (md.definitionsByAUID.put(defid, def) != null) {
                throw new IllegalDefinitionException("Duplicate AUID: " + def.getIdentification());
            }

            if (md.definitionsBySymbol.put(def.getSymbol(), def) != null) {
                throw new IllegalDefinitionException("Duplicate Symbol: " + def.getSymbol());
            }

            if (def instanceof PropertyDefinition) {

                AUID parentauid = createNormalizedAUID(((PropertyDefinition) def).getMemberOf());
                
                
                
                Set<AUID> hs = md.membersOf.get(parentauid);
                
                if (hs == null) {
                   hs = new HashSet<>();
                   md.membersOf.put(parentauid, hs);
                }
                
                hs.add(defid);

            }

            if (def instanceof ClassDefinition && ((ClassDefinition) def).getParentClass() != null) {

                AUID parentauid = createNormalizedAUID(((ClassDefinition) def).getParentClass());

                Set<AUID> hs = md.subclassesOf.get(parentauid);
                
                if (hs == null) {
                   hs = new HashSet<>();
                   md.subclassesOf.put(parentauid, hs);
                }
                
                hs.add(defid);

            }
        }

        return md;
    }

    @Override
    public Collection<AUID> getSubclassesOf(ClassDefinition parent) {

        return subclassesOf.get(createNormalizedAUID(parent.getIdentification()));
    }

    @Override
    public Collection<AUID> getMembersOf(ClassDefinition parent) {
       return membersOf.get(createNormalizedAUID(parent.getIdentification()));
    }

}
