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
package com.sandflow.smpte.register;

import com.sandflow.smpte.register.exception.DuplicateEntryException;
import com.sandflow.smpte.util.UL;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
@XmlRootElement(name = "GroupsRegister", namespace = GroupsRegister.XML_NAMESPACE)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "")
@XmlSeeAlso(value = GroupEntry.class)
public class GroupsRegister {

    public final static String XML_NAMESPACE = "http://www.smpte-ra.org/schemas/395/2014";

    private final HashMap<QualifiedSymbol, GroupEntry> entriesBySymbol = new HashMap<>();

    private final HashMap<UL, GroupEntry> entriesByUL = new HashMap<>();

    @XmlElement(name = "Entry", namespace = XML_NAMESPACE)
    @XmlElementWrapper(name = "Entries", namespace = XML_NAMESPACE)
    private final ArrayList<GroupEntry> entries = new ArrayList<>();

    public GroupsRegister() {
    }

    public GroupEntry getEntryByUL(UL ul) {
        return entriesByUL.get(ul);
    }

    public void addEntry(GroupEntry entry) throws DuplicateEntryException {

        QualifiedSymbol sym = new QualifiedSymbol(entry.getSymbol(), entry.getNamespaceName());

        if (entriesByUL.containsKey(entry.getUL())) {
            throw new DuplicateEntryException(String.format("UL = %s is already present (symbol = %s).", entry.getUL(), entry.getSymbol()));
        }

        if (entriesBySymbol.containsKey(sym)) {
            throw new DuplicateEntryException(String.format("Symbol = %s  is already present (UL = %s).", entry.getSymbol(), entry.getUL()));
        }

        entries.add(entry);
        entriesByUL.put(entry.getUL(), entry);
        entriesBySymbol.put(sym, entry);
    }

    public Collection<GroupEntry> getEntries() {
        return entries;
    }

    public void toXML(Writer writer) throws JAXBException, IOException {

        JAXBContext ctx = JAXBContext.newInstance(GroupsRegister.class);

        Marshaller m = ctx.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(this, writer);
        writer.close();
    }

    public static GroupsRegister fromXML(Reader reader) throws JAXBException, IOException {

        JAXBContext ctx = JAXBContext.newInstance(GroupsRegister.class);

        Unmarshaller m = ctx.createUnmarshaller();

        GroupsRegister reg = (GroupsRegister) m.unmarshal(reader);

        for (GroupEntry te : reg.entries) {
            QualifiedSymbol sym = new QualifiedSymbol(te.getSymbol(), te.getNamespaceName());
            reg.entriesByUL.put(te.getUL(), te);
            reg.entriesBySymbol.put(sym, te);
        }

        return reg;

    }

}
