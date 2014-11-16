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
import com.sandflow.smpte.register.exception.InvalidEntryException;
import com.sandflow.smpte.register.LabelEntry;
import com.sandflow.smpte.register.LabelsRegister;
import static com.sandflow.smpte.register.LabelEntry.Kind.NODE;
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
public class ExcelLabelsRegister {

    private final static Logger LOGGER = Logger.getLogger(ExcelElementsRegister.class.getName());
   
    static public LabelsRegister fromXLS(InputStream xlsfile) throws ExcelCSVParser.SyntaxException, IOException, InvalidEntryException, DuplicateEntryException {
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

        LabelsRegister reg = new LabelsRegister();

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

                LabelEntry label = new LabelEntry();

                if (fields.get(c.get("n:node")) == null) {

                    throw new InvalidEntryException("Invalid n:node "
                            + fields.get(c.get("n:urn"))
                            + " / "
                            + fields.get(c.get("n:sym")));
                }

                if ("Node".equalsIgnoreCase(fields.get(c.get("n:node")))) {
                    label.setKind(LabelEntry.Kind.NODE);
                } else {
                    label.setKind(LabelEntry.Kind.LEAF);
                }
                
                UL ul = UL.fromDotValue(fields.get(c.get("n:urn")));
                
                if (ul == null) {
                                       LOGGER.warning(String.format("Bad UL at %s -> %s",
                           fields.get(c.get("n:sym")), fields.get(c.get("n:urn"))
                    ));
                    continue;
                }

                label.setUL(UL.fromDotValue(fields.get(c.get("n:urn"))));

                label.setSymbol(fields.get(c.get("n:sym")));
                
                label.setName(fields.get(c.get("n:name")));

                label.setDefinition(fields.get(c.get("n:detail")));

                label.setDefiningDocument(fields.get(c.get("n:docs")));


                label.setNotes(fields.get(c.get("i:notes")));

                label.setDeprecated(!("No".equalsIgnoreCase(fields.get(c.get("n:deprecated")))));
if (label.getKind() == NODE) {
                    try {
                        if (fields.get(c.get("n:ns_uri")) != null) {

                            label.setNamespaceName(new URI(fields.get(c.get("n:ns_uri"))));

                        } else {
                            label.setNamespaceName(new URI("http://www.smpte-ra.org/reg/XXXX/2012"));
                        }
                    } catch (URISyntaxException ex) {
                        throw new InvalidEntryException("Invalid URI at "
                                + label.getUL()
                                + " -> "
                                + fields.get(c.get("n:ns_uri")), ex);
                    }
                }

                try {
                    reg.addEntry(label);
                } catch (DuplicateEntryException e) {
                    LOGGER.warning("Duplicate Entry:" +  e.getMessage());
                }

            }

        }

        return reg;
    }

    public static void main(String args[]) throws FileNotFoundException, ExcelCSVParser.SyntaxException, IOException, InvalidEntryException, DuplicateEntryException, PropertyException, JAXBException {
        FileInputStream f = new FileInputStream("\\\\SERVER\\Business\\sandflow-consulting\\projects\\imf\\regxml\\register-format\\input\\labels-smpte-ra-frozen-20140304.2118.csv");

        LabelsRegister reg = ExcelLabelsRegister.fromXLS(f);

        BufferedWriter writer = null;
        writer = new BufferedWriter(new FileWriter("\\\\SERVER\\Business\\sandflow-consulting\\projects\\imf\\regxml\\register-format\\output\\labels-register.xml"));

        JAXBContext ctx = JAXBContext.newInstance(LabelsRegister.class, LabelEntry.class);

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
