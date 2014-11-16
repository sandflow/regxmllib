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
package com.sandflow.smpte.register.importer;

import com.sandflow.smpte.register.exception.DuplicateEntryException;
import com.sandflow.smpte.register.ElementEntry;
import com.sandflow.smpte.register.ElementsRegister;
import com.sandflow.smpte.register.exception.InvalidEntryException;
import com.sandflow.smpte.util.ExcelCSVParser;
import com.sandflow.smpte.util.UL;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

/**
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
public class ExcelElementsRegister {

    private final static Logger LOGGER = Logger.getLogger(ExcelElementsRegister.class.getName());

    static final UL ApplicationSchemeBatch_UL = UL.fromDotValue("06.0E.2B.34.01.01.01.0C.01.02.02.10.02.03.00.00");

    static final UL DUP_AudioChannelIdentifiers_UL = UL.fromDotValue("06.0E.2B.34.01.01.01.0E.01.03.07.02.00.00.00.00");

    static final UL Manufacturing_Organisations_Identifiers_UL = UL.fromDotValue("06.0E.2B.34.01.01.01.02.01.0A.01.00.00.00.00.00");

    static final UL Region_Code_UL = UL.fromDotValue("06.0E.2B.34.01.01.01.03.03.01.01.01.02.00.00.00");

    static final UL ISO6391LanguageCode_ISO7_UL = UL.fromDotValue("06.0E.2B.34.01.01.01.01.03.01.01.02.01.00.00.00");

    static final UL SecondaryOriginalExtendedSpokenLanguageCode_UL = UL.fromURN("urn:smpte:ul:060e2b34.01010107.03010102.03140000");

    static public ElementsRegister fromXLS(InputStream xlsfile) throws ExcelCSVParser.SyntaxException, IOException, InvalidEntryException, DuplicateEntryException {
        InputStreamReader isr;

        try {

            /* this should really never happen */
            isr = new InputStreamReader(xlsfile, "US-ASCII");

        } catch (UnsupportedEncodingException ex) {

            throw new RuntimeException(ex);

        }

        BufferedReader br = new BufferedReader(isr);

        ExcelCSVParser p = new ExcelCSVParser(br);

        HashMap<String, Integer> c = new HashMap<>();

        ElementsRegister reg = new ElementsRegister();

        for (AbstractList<String> fields; (fields = p.getLine()) != null;) {

            if ("_rxi".equalsIgnoreCase(fields.get(0))) {

                for (int i = 0; i < fields.size(); i++) {
                    c.put(fields.get(i), i);
                }

            } else if (fields.get(0) != null && fields.get(0).startsWith("_")) {

                continue;

            } else {

                if (fields.get(c.get("n:urn")) == null) {

                    throw new InvalidEntryException("Invalid UL:"
                            + fields.get(c.get("n:urn"))
                            + " / "
                            + fields.get(c.get("n:sym")));
                }

                ElementEntry element = new ElementEntry();

                if (fields.get(c.get("n:node")) == null) {

                    throw new InvalidEntryException("Invalid n:node "
                            + fields.get(c.get("n:urn"))
                            + " / "
                            + fields.get(c.get("n:sym")));
                }

                if ("Node".equalsIgnoreCase(fields.get(c.get("n:node")))) {
                    element.setKind(ElementEntry.Kind.NODE);
                } else {
                    element.setKind(ElementEntry.Kind.LEAF);
                }

                UL ul = UL.fromDotValue(fields.get(c.get("n:urn")));

                if (ul == null) {
                    LOGGER.warning(String.format("Bad UL at %s -> %s",
                            fields.get(c.get("n:sym")), fields.get(c.get("n:urn"))
                    ));
                    continue;
                }

                element.setUL(UL.fromDotValue(fields.get(c.get("n:urn"))));

                /* BUG: skip Class 13 and Class 14 since no namespace */
                if (element.getUL().isClass13() || element.getUL().isClass14()) {
                    continue;
                }

                /* BUG */
                if (element.getUL().equals(DUP_AudioChannelIdentifiers_UL)) {
                    continue;
                }

                if (element.getUL().equals(ApplicationSchemeBatch_UL)) {

                    /* BUG */
                    element.setSymbol("ApplicationSchemeBatch");
                } else if (element.getUL().equals(Manufacturing_Organisations_Identifiers_UL)) {

                    /* BUG */
                    element.setSymbol("ManufacturingOrganisationsIdentifier");

                } else if (element.getUL().equals(Region_Code_UL)) {

                    /* BUG */
                    element.setSymbol("RegionCode");

                } else if (element.getUL().equals(ISO6391LanguageCode_ISO7_UL)) {

                    /* BUG */
                    element.setSymbol("ISO6391LanguageCode_ISO7");

                } else if (element.getUL().equals(SecondaryOriginalExtendedSpokenLanguageCode_UL)) {

                    /* BUG */
                    element.setSymbol(fields.get(c.get("c:sym")));

                } else {

                    /* BUG */
                    if (element.getKind() == ElementEntry.Kind.NODE) {

                        element.setSymbol(fields.get(c.get("c:node_sym")));
                    } else {

                        element.setSymbol(fields.get(c.get("n:sym")));

                    }

                }

                element.setName(fields.get(c.get("n:name")));

                element.setDefinition(fields.get(c.get("n:detail")));

                element.setDefiningDocument(fields.get(c.get("n:docs")));

                element.setApplications(fields.get(c.get("i:app")));

                element.setNotes(fields.get(c.get("i:notes")));

                element.setDeprecated(!("No".equalsIgnoreCase(fields.get(c.get("n:deprecated")))));

                try {
                    if (fields.get(c.get("n:ns_uri")) != null) {

                        element.setNamespaceName(new URI(fields.get(c.get("n:ns_uri"))));

                    } else {

                        if (element.getUL().getValueOctet(8) < 8) {
                            element.setNamespaceName(new URI("http://www.smpte-ra.org/reg/335/2012"));
                        } else {
                            element.setNamespaceName(null);
                        }
                    }
                } catch (URISyntaxException ex) {
                    throw new InvalidEntryException("Invalid URI at "
                            + element.getUL()
                            + " -> "
                            + fields.get(c.get("n:ns_uri")), ex);
                }

                if (element.getKind() == ElementEntry.Kind.LEAF) {

                    if (fields.get(c.get("n:type_urn")) == null) {

                        LOGGER.warning("Bad Type UL at "
                                + element.getSymbol()
                                + "/"
                                + element.getUL()
                                + " -> "
                                + fields.get(c.get("n:type_urn")));

                        continue;
                    }

                    element.setTypeUL(UL.fromURN(fields.get(c.get("n:type_urn"))));

                    element.setValueLength(fields.get(c.get("s:length")));

                    element.setValueRange(fields.get(c.get("s:range")));

                    element.setValueRange(fields.get(c.get("n:units")));

                    element.setContextScope(fields.get(c.get("i:context")));

                } else {

                }

                try {
                    reg.addEntry(element);
                } catch (DuplicateEntryException e) {
                    LOGGER.warning(e.getMessage());
                }

            }

        }

        return reg;
    }

    public static void main(String args[]) throws FileNotFoundException, ExcelCSVParser.SyntaxException, IOException, InvalidEntryException, DuplicateEntryException, PropertyException, JAXBException {
        FileInputStream f = new FileInputStream("\\\\SERVER\\Business\\sandflow-consulting\\projects\\imf\\regxml\\register-format\\input\\elements-smpte-ra-frozen-20140304.2118.csv");

        ElementsRegister reg = ExcelElementsRegister.fromXLS(f);

        BufferedWriter writer = new BufferedWriter(new FileWriter("\\\\SERVER\\Business\\sandflow-consulting\\projects\\imf\\regxml\\register-format\\output\\elements-register.xml"));

        JAXBContext ctx = JAXBContext.newInstance(ElementsRegister.class, ElementEntry.class);

        Marshaller m = ctx.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(reg, writer);
        writer.close();

        File baseDir = new File("C:\\Users\\pal\\Documents\\");

        ctx.generateSchema(new SchemaOutputResolver() {

            public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
                return new StreamResult(new File(baseDir, suggestedFileName));
            }
        });
    }


}
