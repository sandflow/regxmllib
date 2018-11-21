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

import com.sandflow.smpte.register.exceptions.DuplicateEntryException;
import com.sandflow.smpte.util.UL;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * Elements Register as defined in SMPTE ST 335
 */
@XmlTransient
public abstract class ElementsRegister {

    private final HashMap<QualifiedSymbol, Entry> entriesBySymbol = new HashMap<>();

    private final HashMap<UL, Entry> entriesByUL = new HashMap<>();

    protected ElementsRegister() {
    }

    public Entry getEntryByUL(UL ul) {
        return entriesByUL.get(ul);
    }

    public Entry getEntryBySymbol(QualifiedSymbol qs) {
        return entriesBySymbol.get(qs);
    }

    public abstract Collection<? extends Entry> getEntries();

    public void toXML(Writer writer) throws JAXBException, IOException {

        JAXBContext ctx = JAXBContext.newInstance(this.getClass());

        Marshaller m = ctx.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(this, writer);
        writer.close();
    }

    public static ElementsRegister fromXML(Reader reader) throws JAXBException, IOException, DuplicateEntryException {

        JAXBContext ctx = JAXBContext.newInstance(com.sandflow.smpte.register.catsup.ElementsRegisterModel.class);

        Unmarshaller m = ctx.createUnmarshaller();
        ElementsRegister reg = (ElementsRegister) m.unmarshal(reader);

        for (Entry te : reg.getEntries()) {
            QualifiedSymbol sym = new QualifiedSymbol(te.getSymbol(), te.getNamespaceName());

            if (reg.getEntryByUL(te.getUL()) != null) {
                throw new DuplicateEntryException(
                        String.format("UL = %s is already present (symbol = %s).",
                                te.getUL(),
                                te.getSymbol()
                        )
                );
            }

            if (reg.entriesBySymbol.get(sym) != null) {
                throw new DuplicateEntryException(
                        String.format(
                                "Symbol = %s  is already present (UL = %s).",
                                te.getSymbol(),
                                te.getUL()
                        )
                );
            }

            reg.entriesByUL.put(te.getUL(), te);
            reg.entriesBySymbol.put(sym, te);
        }

        return reg;

    }

    /**
     * Single Entry in an Elements Register (SMPTE ST 335)
     */
    public static interface Entry {

        String getApplications();

        String getContextScope();

        String getDefiningDocument();

        String getDefinition();

        Kind getKind();

        String getName();

        URI getNamespaceName();

        String getNotes();

        String getSymbol();

        UL getTypeUL();

        UL getUL();

        String getUnitOfMeasure();

        String getValueLength();

        String getValueRange();

        boolean isDeprecated();

        @XmlType(name = "")
        public static enum Kind {
            NODE, LEAF
        }
    }
}
