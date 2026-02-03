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
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import static org.w3c.dom.Node.ELEMENT_NODE;

/**
 * Record Type Definition as defined in ST 2001-1
 */
@XmlAccessorType(XmlAccessType.NONE)
public class RecordTypeDefinition extends Definition {

    @XmlJavaTypeAdapter(value = RecordMemberAdapter.class)
    @XmlAnyElement(lax = false)
    ArrayList<Member> members = new ArrayList<>();

    public RecordTypeDefinition() {
    }

    @Override
    public void accept(DefinitionVisitor visitor) throws DefinitionVisitor.VisitorException {
        visitor.visit(this);
    }

    public void addMember(Member record) {
        members.add(record);
    }

    public Collection<Member> getMembers() {
        return members;
    }
    
  
    @XmlAccessorType(XmlAccessType.NONE)
    public static class Member {
        @XmlElement(name = "Name")
        private String name;
        @XmlJavaTypeAdapter(value = AUIDAdapter.class)
        @XmlElement(name = "Type")
        private AUID type;
        @XmlElement(name = "Description")
        private String description;
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }

        public AUID getType() {
            return type;
        }

        public void setType(AUID type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    private static class RecordMemberAdapter extends XmlAdapter<Object, ArrayList<RecordTypeDefinition.Member>> {

        public ArrayList<RecordTypeDefinition.Member> unmarshal(Object v) throws Exception {

            ArrayList<RecordTypeDefinition.Member> al = new ArrayList<>();

            org.w3c.dom.Node node = ((org.w3c.dom.Element) v).getFirstChild();

            while (node != null) {

                if (node.getNodeType() == ELEMENT_NODE) {

                    org.w3c.dom.Element elem = (org.w3c.dom.Element) node;

                    if ("Name".equals(elem.getNodeName())) {
                        
                        al.add(new RecordTypeDefinition.Member());
                        al.get(al.size() - 1).setName(elem.getTextContent());
                        
                    } else if ("Type".equals(elem.getNodeName())) {
                        
                        al.get(al.size() - 1).setType(AUID.fromURN(elem.getTextContent()));
                        
                    }
                }

                node = node.getNextSibling();
            }

            return al;
        }

        public Object marshal(ArrayList<RecordTypeDefinition.Member> v) throws Exception {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            org.w3c.dom.Element elem = doc.createElementNS(MetaDictionary.XML_NS, "Members");

            for (RecordTypeDefinition.Member e : v) {

                org.w3c.dom.Element e1 = doc.createElementNS(MetaDictionary.XML_NS, "Name");

                e1.setTextContent(e.getName());

                elem.appendChild(e1);

                e1 = doc.createElementNS(MetaDictionary.XML_NS, "Type");

                e1.setTextContent(e.getType().toString());

                elem.appendChild(e1);

            }

            return elem;
        }
    }

}
