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
package com.sandflow.smpte.regxml.dict.definitions;

import com.sandflow.smpte.regxml.dict.MetaDictionary;
import com.sandflow.smpte.util.AUID;
import com.sandflow.smpte.util.xml.AUIDAdapter;
import java.util.ArrayList;
import java.util.Collection;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import static org.w3c.dom.Node.ELEMENT_NODE;

/**
 * Enumeration Type Definition as defined in ST 2001-1
 */
@XmlAccessorType(XmlAccessType.NONE)
public class EnumerationTypeDefinition extends Definition {

    private static class EnumerationElementAdapter extends XmlAdapter<Object, ArrayList<Element>> {

        @Override
        public ArrayList<Element> unmarshal(Object v) throws Exception {

            ArrayList<Element> al = new ArrayList<>();

            org.w3c.dom.Node node = ((org.w3c.dom.Element) v).getFirstChild();

            while (node != null) {

                if (node.getNodeType() == ELEMENT_NODE) {

                    org.w3c.dom.Element elem = (org.w3c.dom.Element) node;

                    if ("Name".equals(elem.getNodeName())) {
                        
                        al.add(new Element());
                        al.get(al.size() - 1).setName(elem.getTextContent());
                        
                    } else if ("Value".equals(elem.getNodeName())) {
                        
                        al.get(al.size() - 1).setValue(Integer.parseInt(elem.getTextContent()));
                        
                    } else if ("Description".equals(elem.getNodeName())) {
                        
                        al.get(al.size() - 1).setDescription(elem.getTextContent());
                        
                    }
                }

                node = node.getNextSibling();
            }

            return al;
        }

        @Override
        public Object marshal(ArrayList<Element> v) throws Exception {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            org.w3c.dom.Element elem = doc.createElementNS(MetaDictionary.XML_NS, "Elements");

            for (Element e : v) {

                org.w3c.dom.Element e1 = doc.createElementNS(MetaDictionary.XML_NS, "Name");

                e1.setTextContent(e.getName());

                elem.appendChild(e1);

                e1 = doc.createElementNS(MetaDictionary.XML_NS, "Value");

                e1.setTextContent(Integer.toString(e.getValue()));

                elem.appendChild(e1);

                if (e.getDescription() != null) {
                    e1 = doc.createElementNS(MetaDictionary.XML_NS, "Description");

                    e1.setTextContent(e.getDescription());

                    elem.appendChild(e1);
                }

            }

            return elem;
        }
    }

    @XmlJavaTypeAdapter(value = AUIDAdapter.class)
    @XmlElement(name = "ElementType")
    private AUID elementType;

    @XmlJavaTypeAdapter(value = EnumerationElementAdapter.class)
    @XmlAnyElement(lax = false)
    private ArrayList<Element> elements;

    public EnumerationTypeDefinition() {
    }

    public EnumerationTypeDefinition(Collection<Element> elements) {
        this.elements = new ArrayList<>(elements);
    }

    public AUID getElementType() {
        return elementType;
    }

    public void setElementType(AUID elementType) {
        this.elementType = elementType;
    }

    @Override
    public void accept(DefinitionVisitor visitor) throws DefinitionVisitor.VisitorException {
        visitor.visit(this);
    }

    public Collection<Element> getElements() {
        return elements;
    }

    @XmlType(name = "")
    @XmlAccessorType(value = XmlAccessType.NONE)
    public static class Element {

        @XmlElement(name = "Name")
        private String name;
        @XmlElement(name = "Value")
        private int value;
        @XmlElement(name = "Description")
        private String description;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

}
