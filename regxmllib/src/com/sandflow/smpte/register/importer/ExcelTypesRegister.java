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
import com.sandflow.smpte.register.TypeEntry;
import com.sandflow.smpte.register.TypesRegister;
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
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

/**
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
public class ExcelTypesRegister {
    
    private final static Logger LOGGER = Logger.getLogger(ExcelElementsRegister.class.getName());

    static final UL AUID_TYPE_UL = UL.fromURN("urn:smpte:ul:060E2B34.01040101.01030100.00000000");
    static final UL EIDRIdentifierType_TYPE_UL = UL.fromURN("urn:smpte:ul:060e2b34.01040101.01200800.00000000");
    static final UL CanonicalDOINameType_TYPE_UL = UL.fromURN("urn:smpte:ul:060e2b34.01040101.01200700.00000000");
    static final UL BAD_UUID_TYPE_UL = UL.fromURN("urn:smpte:ul:060E2B34.01040101.04011100.00000000");

    static public TypesRegister fromXLS(InputStream xlsfile) throws ExcelCSVParser.SyntaxException, IOException, InvalidEntryException, DuplicateEntryException {
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

        TypesRegister reg = new TypesRegister();

        TypeEntry lasttype = null;

        for (AbstractList<String> fields; (fields = p.getLine()) != null;) {

            if ("_rxi".equalsIgnoreCase(fields.get(0))) {

                /* read headers */
                for (int i = 0; i < fields.size(); i++) {
                    c.put(fields.get(i), i);
                }

            } else if (fields.get(0) == null
                    || (!fields.get(0).startsWith("_"))) {

                if (lasttype != null && "Link".equalsIgnoreCase(fields.get(c.get("n:node")))) {

                    /* CHILD */
                    TypeEntry.Facet f = new TypeEntry.Facet();

                    f.setApplications(fields.get(c.get("i:app")));

                    f.setNotes(fields.get(c.get("i:notes")));

                    f.setDeprecated(!("No".equalsIgnoreCase(fields.get(c.get("n:deprecated")))));

                    f.setDefinition(fields.get(c.get("n:detail")));

                    switch (lasttype.getTypeKind()) {
                        case Record:
                            f.setSymbol(fields.get(c.get("n:sym")));
                            f.setName(fields.get(c.get("n:name")));

                            if (fields.get(c.get("n:type_urn")) != null) {
                                f.setType(UL.fromURN(fields.get(c.get("n:type_urn"))));
                            } else {
                                throw new InvalidEntryException(
                                        String.format(
                                                "Missing n:type_urn from Record face %s",
                                                fields.get(c.get("a:urn"))
                                        )
                                );
                            }

                            break;
                        case Enumerated:

                            if (lasttype.getBaseType().equals(AUID_TYPE_UL)) {
                                f.setUL(UL.fromDotValue(fields.get(c.get("n:urn"))));
                            } else {
                                f.setSymbol(fields.get(c.get("n:sym")));
                                f.setName(fields.get(c.get("n:name")));
                                f.setValue(fields.get(c.get("n:value")));
                            }

                            break;
                        case WeakReference:

                            f.setUL(UL.fromDotValue(fields.get(c.get("n:target_urn"))));

                            break;

                    }


                    /* Cannot use "n:parent_urn" since it is wrong for Weak Reference entries  */
                    lasttype.getFacets().add(f);

                } else {

                    /* NODE and LEAF */
                    TypeEntry type = new TypeEntry();

                    if (fields.get(c.get("n:urn")) == null) {

                        throw new InvalidEntryException("Invalid Type UL:"
                                + fields.get(c.get("n:urn"))
                                + " / "
                                + fields.get(c.get("n:sym"))
                        );
                    }

                    type.setUL(UL.fromDotValue(fields.get(c.get("n:urn"))));

                    /* BUG: Bad UUID type */
                    if (type.getUL().isClass14() || type.getUL().isClass13() || type.getUL().isClass15() || type.getUL().equals(BAD_UUID_TYPE_UL)) {
                        lasttype = null;
                        continue;
                    }

                    type.setName(fields.get(c.get("n:name")));

                    type.setDefinition(fields.get(c.get("n:detail")));

                    type.setApplications(fields.get(c.get("i:app")));

                    type.setNotes(fields.get(c.get("i:notes")));

                    type.setDeprecated(!("No".equalsIgnoreCase(fields.get(c.get("n:deprecated")))));

                    type.setSymbol(fields.get(c.get("n:sym")));
                    
                    /* BUG: there is no StrongReferenceNameValue type */
                    
                    if (type.getSymbol().equals("StrongReferenceSetNameValue")) continue;

                    type.setDefiningDocument(fields.get(c.get("n:docs")));

                    if ("Leaf".equalsIgnoreCase(fields.get(c.get("n:node")))) {

                        /* LEAF */
                        type.setKind(TypeEntry.Kind.LEAF);

                        String kind = fields.get(c.get("n:kind"));

                        String qualif = fields.get(c.get("n:qualif"));

                        UL target_urn = null;

                        if (fields.get(c.get("n:target_urn")) != null) {
                            target_urn = UL.fromURN(fields.get(c.get("n:target_urn")));

                            if (target_urn == null) {
                                target_urn = UL.fromDotValue(fields.get(c.get("n:target_urn")));
                            }
                        }

                        if ("integer".equalsIgnoreCase(kind)) {

                            /* INTEGER */
                            type.setTypeKind(TypeEntry.TypeKind.Integer);
                            type.setTypeSize(Long.parseLong(fields.get(c.get("n:qualif"))));

                            if ("True".equals(fields.get(c.get("n:value")))) {
                                type.getTypeQualifiers().add(TypeEntry.TypeQualifiers.isSigned);
                            }
                            type.getTypeQualifiers().add(TypeEntry.TypeQualifiers.isNumeric);

                            if (target_urn != null) {

                                throw new InvalidEntryException(
                                        String.format(
                                                "Integer type %s has n:target_urn defined.",
                                                type.getUL()
                                        )
                                );

                            }

                        } else if ("rename".equalsIgnoreCase(kind)) {

                            type.setTypeKind(TypeEntry.TypeKind.Rename);

                            if (target_urn != null) {

                                type.setBaseType(target_urn);

                            } else if (type.getUL().equals(EIDRIdentifierType_TYPE_UL)) {

                                /* BUG: EIDRIdentifierType is missing target_urn */
                                type.setBaseType(CanonicalDOINameType_TYPE_UL);
                            } else {
                                throw new InvalidEntryException(
                                        String.format(
                                                "Rename type %s is missing n:target_urn.",
                                                type.getUL()
                                        )
                                );
                            }

                        } else if ("record".equalsIgnoreCase(kind)) {

                            type.setTypeKind(TypeEntry.TypeKind.Record);

                        } else if ("array".equalsIgnoreCase(kind)) {

                            type.setTypeKind(TypeEntry.TypeKind.Multiple);

                            if (target_urn != null) {

                                type.setBaseType(target_urn);

                            } else {
                                throw new InvalidEntryException(
                                        String.format(
                                                "Type %s is missing n:target_urn.",
                                                type.getUL()
                                        )
                                );
                            }

                            if ("fixed".equals(qualif)) {

                                type.setTypeSize(Long.parseLong(fields.get(c.get("n:minOccurs"))));

                                type.getTypeQualifiers().add(TypeEntry.TypeQualifiers.isSizeImplicit);
                                type.getTypeQualifiers().add(TypeEntry.TypeQualifiers.isOrdered);

                            } else if ("varying".equals(qualif)) {

                                type.setTypeSize(0L);
                                type.getTypeQualifiers().add(TypeEntry.TypeQualifiers.isSizeImplicit);
                                type.getTypeQualifiers().add(TypeEntry.TypeQualifiers.isOrdered);

                            } else if ("strong".equals(qualif)) {

                                if (type.getSymbol().startsWith("StrongReferenceVector")) {

                                    String symbol = "StrongReference" + type.getSymbol().substring("StrongReferenceVector".length());

                                    TypeEntry realtype = reg.getEntryBySymbol(symbol, type.getNamespaceName());

                                    type.setBaseType(realtype.getUL());
                                }

                                type.setTypeSize(0L);
                                type.getTypeQualifiers().add(TypeEntry.TypeQualifiers.isSizeImplicit);
                                type.getTypeQualifiers().add(TypeEntry.TypeQualifiers.isOrdered);
                                type.getTypeQualifiers().add(TypeEntry.TypeQualifiers.isIdentified);

                            } else if ("weak".equals(qualif)) {

                                type.setTypeSize(0L);
                                type.getTypeQualifiers().add(TypeEntry.TypeQualifiers.isSizeImplicit);
                                type.getTypeQualifiers().add(TypeEntry.TypeQualifiers.isOrdered);
                                type.getTypeQualifiers().add(TypeEntry.TypeQualifiers.isIdentified);

                            } else {

                                throw new InvalidEntryException(
                                        String.format(
                                                "Array type %s has unknown n:qualif.",
                                                type.getUL()
                                        )
                                );

                            }

                        } else if ("character".equalsIgnoreCase(kind)) {

                            type.setTypeKind(TypeEntry.TypeKind.Character);
                            type.setTypeSize(Long.parseLong(fields.get(c.get("n:qualif"))));

                        } else if ("string".equalsIgnoreCase(kind)) {

                            type.setTypeKind(TypeEntry.TypeKind.String);

                            if (target_urn != null) {

                                type.setBaseType(target_urn);

                            } else {
                                throw new InvalidEntryException(
                                        String.format(
                                                "Type %s is missing n:target_urn.",
                                                type.getUL()
                                        )
                                );
                            }

                        } else if ("enumeration".equalsIgnoreCase(kind)) {

                            type.setTypeKind(TypeEntry.TypeKind.Enumerated);

                            if (target_urn != null) {

                                type.setBaseType(target_urn);

                            } else {
                                throw new InvalidEntryException(
                                        String.format(
                                                "Type %s is missing n:target_urn.",
                                                type.getUL()
                                        )
                                );
                            }

                        } else if ("extendible".equalsIgnoreCase(kind)) {

                            type.setTypeKind(TypeEntry.TypeKind.Enumerated);

                            type.setBaseType(UL.fromURN("urn:smpte:ul:060E2B34.01040101.01030100.00000000"));

                        } else if ("set".equalsIgnoreCase(kind)) {

                            type.setTypeKind(TypeEntry.TypeKind.Multiple);
                            type.setTypeSize(0L);

                            type.getTypeQualifiers().add(TypeEntry.TypeQualifiers.isSizeImplicit);
                            type.getTypeQualifiers().add(TypeEntry.TypeQualifiers.isIdentified);
                            
                            /* BUG: StrongReferenceSets do no have entries of type Strong Reference */

                            if (type.getSymbol().startsWith("StrongReferenceSet")) {

                                String symbol = "StrongReference" + type.getSymbol().substring("StrongReferenceSet".length());

                                TypeEntry realtype = reg.getEntryBySymbol(symbol, type.getNamespaceName());

                                type.setBaseType(realtype.getUL());
                            } else if (target_urn != null) {

                                type.setBaseType(target_urn);

                            } else {
                                throw new InvalidEntryException(
                                        String.format(
                                                "Type %s is missing n:target_urn.",
                                                type.getUL()
                                        )
                                );
                            }

                        } else if ("stream".equalsIgnoreCase(kind)) {

                            type.setTypeKind(TypeEntry.TypeKind.Stream);

                        } else if ("indirect".equalsIgnoreCase(kind)) {

                            type.setTypeKind(TypeEntry.TypeKind.Indirect);

                        } else if ("opaque".equalsIgnoreCase(kind)) {

                            type.setTypeKind(TypeEntry.TypeKind.Opaque);

                        } else if ("formal".equalsIgnoreCase(kind)) {

                            /* BUG: what is 'formal; */
                            continue;

                        } else if ("reference".equalsIgnoreCase(kind)) {

                            type.setBaseType(target_urn);

                            if ("strong".equalsIgnoreCase(qualif)) {
                                type.setTypeKind(TypeEntry.TypeKind.StrongReference);

                            } else if ("weak".equalsIgnoreCase(qualif)) {
                                type.setTypeKind(TypeEntry.TypeKind.WeakReference);
                            } else {
                                throw new InvalidEntryException(
                                        String.format(
                                                "Array type %s has unknown n:qualif.",
                                                type.getUL()
                                        )
                                );
                            }

                        } else {
                            throw new InvalidEntryException(
                                    String.format(
                                            "Type %s is missing n:kind.",
                                            type.getUL()
                                    )
                            );
                        }

                        lasttype = type;

                    } else {

                        /* NODE */
                        type.setKind(TypeEntry.Kind.NODE);

                        /* BUG: ns:uri is empty */
                        try {
                            if (fields.get(c.get("n:ns_uri")) != null) {

                                type.setNamespaceName(new URI(fields.get(c.get("n:ns_uri"))));

                            } else {
                                type.setNamespaceName(new URI("http://www.smpte-ra.org/reg/2003/2012"));

                            }
                        } catch (URISyntaxException ex) {
                            throw new InvalidEntryException(
                                    String.format(
                                            "Invalid URI %s at Type %s",
                                            fields.get(c.get("n:ns_uri")),
                                            type.getUL()
                                    ), ex);
                        }

                        lasttype = null;

                    }

                    reg.addEntry(type);


                }

            }

        }

        return reg;

    }


    public static void main(String args[]) throws FileNotFoundException, ExcelCSVParser.SyntaxException, IOException, InvalidEntryException, JAXBException, DuplicateEntryException {
        FileInputStream f = new FileInputStream("\\\\SERVER\\Business\\sandflow-consulting\\projects\\imf\\regxml\\register-format\\input\\types-smpte-ra-frozen-20140304.2118.csv");

        TypesRegister reg = ExcelTypesRegister.fromXLS(f);

        BufferedWriter writer = null;
        writer = new BufferedWriter(new FileWriter("\\\\SERVER\\Business\\sandflow-consulting\\projects\\imf\\regxml\\register-format\\output\\types-register.xml"));

        JAXBContext ctx = JAXBContext.newInstance(TypesRegister.class);

        Marshaller m = ctx.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(reg, writer);
        writer.close();

        File baseDir = new File("C:\\Users\\pal\\Documents\\");

        ctx.generateSchema(new SchemaOutputResolver() {

            @Override
            public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
                return new StreamResult(new File(baseDir, suggestedFileName));
            }
        });

    }

    HashMap<UL, TypeEntry> types = new HashMap<>();

}
