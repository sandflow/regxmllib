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
import com.sandflow.smpte.regxml.dict.exceptions.DuplicateSymbolException;
import com.sandflow.smpte.regxml.dict.exceptions.IllegalDefinitionException;
import com.sandflow.smpte.util.AUID;
import com.sandflow.smpte.util.UL;
import com.sandflow.smpte.util.UUID;
import com.sandflow.smpte.util.xml.UUIDAdapter;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;

/**
 * A single RegXML Metadictionary as specified in SMPTE ST 2001-1
 */
@XmlRootElement(name = "Extension")
@XmlAccessorType(XmlAccessType.NONE)
public class MetaDictionary implements DefinitionResolver {

    /**
     * XML Schema Namespace for the XML representation of a RegXML Metadictionary 
     */
    public final static String XML_NS = "http://www.smpte-ra.org/schemas/2001-1b/2013/metadict";

    static AUID createNormalizedAUID(AUID auid) {
        if (auid.isUL()) {
            
            return new AUID(createNormalizedUL(auid.asUL()));
        } else {
            return auid;
        }
    }

    static UL createNormalizedUL(UL ul) {
        byte[] value = ul.getValue().clone();
        /* set version to 0 */
        
        value[7] = 0;
        
        if (ul.isGroup()) {
            
            /* set byte 6 to 0x7f */
            value[5] = 0x7f;
            
        }
        
        return new UL(value);
    }

    static String createQualifiedSymbol(String namespace, String symbol) {
        if (namespace == null || namespace.length() == 0) {
            return symbol;
        } else {
            return namespace + " " + symbol;
        }
    }
    
    /**
     * Reads a MetaDictionary from an XML document.
     * 
     * @param reader Reader from which a single MetaDictionary in XML form will be read
     * @return a MetaDictionary
     * @throws JAXBException
     * @throws IOException
     * @throws IllegalDefinitionException 
     */
    public static MetaDictionary fromXML(Reader reader) throws JAXBException, IOException, IllegalDefinitionException {
        JAXBContext ctx = JAXBContext.newInstance(MetaDictionary.class);
        
        Unmarshaller m = ctx.createUnmarshaller();
        MetaDictionary md = (MetaDictionary) m.unmarshal(reader);
        
        for (Definition def : md.definitions) {
            
            def.setNamespace(md.getSchemeURI());
            
            md.indexDefinition(def);
        }

        return md;
    }

    

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

    /**
     * Instantiates a MetaDictionary.
     * @param schemeURI Scheme URI of the MetaDictionary
     */
    public MetaDictionary(URI schemeURI) {
        /* NOTE: ST 2001-1 does not allow label to be used in multiple enumerations */

        this.schemeID = UUID.fromURIName(schemeURI);
        this.schemeURI = schemeURI;
    }
    
    void indexDefinition(Definition def) throws IllegalDefinitionException {
        AUID defid = createNormalizedAUID(def.getIdentification());
        
        if (def.getClass() != PropertyAliasDefinition.class) {
            
            if (this.definitionsByAUID.containsKey(defid)) {
                throw new IllegalDefinitionException("Duplicate AUID: " + def.getIdentification());
            }
            
            if (this.definitionsBySymbol.containsKey(def.getSymbol())) {
                throw new DuplicateSymbolException("Duplicate Symbol: " + def.getSymbol());
            }
            
            this.definitionsByAUID.put(defid, def);
            this.definitionsBySymbol.put(def.getSymbol(), def);
            
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
    }

    /**
     * Adds a definition to the MetaDictionary
     * 
     * @param def Definition to be added
     * @throws IllegalDefinitionException 
     */
    public void add(Definition def) throws IllegalDefinitionException {

        if (!def.getNamespace().equals(this.getSchemeURI())) {
            throw new IllegalDefinitionException("Namespace does not match Metadictionary Scheme URI: " + def.getSymbol());
        }

        indexDefinition(def);

        this.definitions.add(def);
    }

    /**
     * @return SchemeID field of the MetaDictionay
     */
    public UUID getSchemeID() {
        return schemeID;
    }

    /**
     * @return SchemeURI field of the MetaDictionay
     */
    public URI getSchemeURI() {
        return schemeURI;
    }

    /**
     * @return Description field of the MetaDictionay
     */
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
                type = WeakReferenceTypeDefinition.class),
        @XmlElement(name = "TypeDefinitionFloat",
                type = FloatTypeDefinition.class),
        @XmlElement(name = "TypeDefinitionLenseSerialFloat",
                type = LensSerialFloatTypeDefinition.class)
    })
    public ArrayList<Definition> getDefinitions() {
        return definitions;
    }

    @Override
    public Definition getDefinition(AUID id) {
        return definitionsByAUID.get(createNormalizedAUID(id));
    }

    /**
     * Retrieves a Definition based on its symbol 
     * @param symbol Symbol of the definition to be retrieved
     * @return Definition, or null if no definition exists with the specified symbol
     */
    public Definition getDefinition(String symbol) {
        return definitionsBySymbol.get(symbol);
    }

    /**
     * Generates an XML representation of the MetaDictionary according to SMPTE ST 2001-1
     * @return The XML DOM
     */
    public Document toXML() {

        Document doc = null;

        try {

            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Marshaller m = JAXBContext.newInstance(MetaDictionary.class).createMarshaller();
            m.marshal(this, doc);

        } catch (JAXBException | ParserConfigurationException e) {

            throw new RuntimeException(e);

        }

        return doc;
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
