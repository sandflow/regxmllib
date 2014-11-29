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
import com.sandflow.smpte.register.GroupEntry;
import com.sandflow.smpte.register.GroupsRegister;
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
import java.util.HashSet;
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
public class ExcelGroupsRegister {
    
    private final static Logger LOGGER = Logger.getLogger(ExcelElementsRegister.class.getName());
    
    public final static String SMPTE_NAMESPACE = "http://www.smpte-ra.org/reg/395/2014";

    static final UL PARTITION_PACK_UL = UL.fromDotValue("06.0E.2B.34.02.7F.01.01.0D.01.02.01.01.00.00.00");
    static final UL HeaderPartitionPack_UL = UL.fromURN("urn:smpte:ul:060e2b34.027f0101.0d010201.01020000");
    static final UL BodyPartitionPack_UL = UL.fromURN("urn:smpte:ul:060e2b34.027f0101.0d010201.01030000");
    static final UL FooterPartitionPack_UL = UL.fromURN("urn:smpte:ul:060e2b34.027f0101.0d010201.01040000");

    static public GroupsRegister fromXLS(InputStream xlsfile) throws ExcelCSVParser.SyntaxException, IOException, InvalidEntryException, DuplicateEntryException {
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

        GroupsRegister reg = new GroupsRegister();

        GroupEntry lastleaf = null;

        for (AbstractList<String> fields; (fields = p.getLine()) != null;) {

            if ("_rxi".equalsIgnoreCase(fields.get(0))) {

                for (int i = 0; i < fields.size(); i++) {
                    c.put(fields.get(i), i);
                }

            } else if (fields.get(0) == null
                    || (fields.get(0).startsWith("#"))) {

                if (!("Groups".equalsIgnoreCase(fields.get(c.get("n:reg"))))) {
                    continue;
                }

                if ("Link".equalsIgnoreCase(fields.get(c.get("n:node")))) {

                    if (lastleaf == null) {
                        continue;
                    }

                    /* deal with children */
                    GroupEntry.Record r = new GroupEntry.Record();

                    r.setItem(UL.fromDotValue(fields.get(c.get("n:urn"))));

                    if (fields.get(c.get("n:tag")) != null) {
                        r.setLocalTag(Long.parseUnsignedLong(fields.get(c.get("n:tag")), 16));
                    }

                    if (fields.get(c.get("n:maxLen")) != null) {
                        r.setLimitLength(Long.parseUnsignedLong(fields.get(c.get("n:maxLen"))));
                    }

                    if (fields.get(c.get("n:minOccurs")) != null) {
                        r.setOptional("0".equals(fields.get(c.get("n:minOccurs"))));
                    } else {
                        r.setOptional(true);
                    }

                    if (fields.get(c.get("n:isUnique")) != null) {
                        r.setUniqueID(true);
                    } else {
                        r.setUniqueID(false);
                    }

                    if (fields.get(c.get("n:isDefault")) != null) {
                        throw new InvalidEntryException("Default found!");
                    }

                    if (fields.get(c.get("n:value")) != null) {
                        throw new InvalidEntryException("Default value found!");
                    }

                    if (fields.get(c.get("n:isIgnorable")) != null) {
                        throw new InvalidEntryException("Ignorable found!");
                    }

                    lastleaf.getContents().add(r);

                } else {

                    GroupEntry group = new GroupEntry();

                    if (fields.get(c.get("n:urn")) == null) {

                        throw new InvalidEntryException("Invalid Group UL:"
                                + fields.get(c.get("n:urn"))
                                + " / "
                                + fields.get(c.get("n:sym")));
                    }

                    group.setUL(UL.fromDotValue(fields.get(c.get("n:urn"))));

                    /* TODO: renumber octets! */
                    
                    /* skip all Class 13 but Pro MPEG */
                    if (group.getUL().isClass13() && group.getUL().getValueOctet(9) != 1) {
                        lastleaf = null;
                        continue;
                    }

                    /* skip Class 14 and Class 15 */
                    if (group.getUL().isClass14() || group.getUL().isClass15()) {
                        lastleaf = null;
                        continue;
                    }

                    group.setName(fields.get(c.get("n:name")));

                    if (group.getName() == null) {

                        LOGGER.warning(String.format(
                                        "Name missing from Group %s",
                                        group.getUL()
                                )
                        );
                    }

                    group.setDefinition(fields.get(c.get("n:detail")));

                    if (group.getDefinition() == null) {

                        LOGGER.warning(
                                String.format(
                                        "Definition missing from Group %s",
                                        group.getUL()
                                )
                        );
                    }

                    group.setApplications(fields.get(c.get("i:app")));

                    group.setNotes(fields.get(c.get("i:notes")));

                    group.setDeprecated(!("No".equalsIgnoreCase(fields.get(c.get("n:deprecated")))));

                    if (fields.get(c.get("n:deprecated")) == null) {

                        LOGGER.warning(
                                String.format(
                                        "Deprecated missing from Group %s",
                                        group.getUL()
                                )
                        );
                    }

                    group.setSymbol(fields.get(c.get("n:sym")));

                    try {
                        if (fields.get(c.get("n:ns_uri")) != null) {

                            group.setNamespaceName(new URI(fields.get(c.get("n:ns_uri"))));

                        } else if (group.getUL().getValueOctet(8) <= 12) {

                            group.setNamespaceName(new URI(SMPTE_NAMESPACE));
                        } else {
                            group.setNamespaceName(new URI(SMPTE_NAMESPACE + "/" + group.getUL().getValueOctet(8) + "/" + group.getUL().getValueOctet(9))); 
                        }
                    } catch (URISyntaxException ex) {
                        throw new InvalidEntryException("Invalid URI at "
                                + group.getUL()
                                + " -> "
                                + fields.get(c.get("n:ns_uri")), ex);
                    }

                    if ("Leaf".equalsIgnoreCase(fields.get(c.get("n:node")))
                            || fields.get(c.get("n:node")) == null) {

                        /* BUG */
                        if (group.getSymbol().equals("UTF8TextBasedSet")) {
                            group.setUL(UL.fromDotValue("06.0E.2B.34.02.7F.01.01.0D.01.04.01.04.02.02.00"));
                        } else if (group.getSymbol().equals("UTF16TextBasedSet")) {
                            group.setUL(UL.fromDotValue("06.0E.2B.34.02.7F.01.01.0D.01.04.01.04.02.03.00"));
                        }

                        /* BUG */
                        if (fields.get(c.get("n:node")) == null) {

                            if (group.getUL().equals(PARTITION_PACK_UL)) {
                                group.setKind(GroupEntry.Kind.NODE);
                            }
                            if (group.getUL().equals(HeaderPartitionPack_UL)) {
                                group.setKind(GroupEntry.Kind.LEAF);
                            }
                            if (group.getUL().equals(BodyPartitionPack_UL)) {
                                group.setKind(GroupEntry.Kind.LEAF);
                            }
                            if (group.getUL().equals(FooterPartitionPack_UL)) {
                                group.setKind(GroupEntry.Kind.LEAF);
                            }

                        } else {
                            group.setKind(GroupEntry.Kind.LEAF);
                        }
                        group.setDefiningDocument(fields.get(c.get("n:docs")));

                        if (fields.get(c.get("n:parent_urn")) != null) {
                            group.setParent(UL.fromDotValue(fields.get(c.get("n:parent_urn"))));
                        }

                        group.setConcrete(!("abstract".equalsIgnoreCase(fields.get(c.get("n:isAbstract")))));
                        
                                          
                        if (fields.get(c.get("n:coding")) != null) {

                            for (String s : fields.get(c.get("n:coding")).split(" ")) {

                                group.getKlvSyntax().add(Byte.parseByte(s, 16));

                            }

                        } else {

                            LOGGER.warning(
                                    String.format(
                                            "Coding missing from Group %s",
                                            group.getUL()
                                    )
                            );
                        }

                        lastleaf = group;

                    } else {

                        group.setKind(GroupEntry.Kind.NODE);

                        lastleaf = null;

                    }

                    reg.addEntry(group);

                }

            }

        }

        return reg;
    }

    public static void main(String args[]) throws FileNotFoundException, ExcelCSVParser.SyntaxException, IOException, InvalidEntryException, PropertyException, JAXBException, DuplicateEntryException {
        FileInputStream f = new FileInputStream("\\\\SERVER\\Business\\sandflow-consulting\\projects\\imf\\regxml\\register-format\\input\\groups-smpte-ra-frozen-20140304.2118.csv");

        GroupsRegister reg = ExcelGroupsRegister.fromXLS(f);

        BufferedWriter writer = null;
        writer = new BufferedWriter(new FileWriter("\\\\SERVER\\Business\\sandflow-consulting\\projects\\imf\\regxml\\register-format\\output\\groups-register.xml"));

        JAXBContext ctx = JAXBContext.newInstance(GroupsRegister.class, GroupEntry.class);

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
