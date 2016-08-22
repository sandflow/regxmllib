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
package com.sandflow.smpte.tools;

import com.sandflow.smpte.register.exceptions.DuplicateEntryException;
import com.sandflow.smpte.register.exceptions.InvalidEntryException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
public class GenerateXMLSchemaDocuments {

    private final static String USAGE = "Generates XML Schema documents for JAXB-annotated classes.\n"
            + "  Usage: GenerateXMLSchemaDocuments -cp classpath -d outdir\n"
            + "         GenerateXMLSchemaDocuments -?";

    public static void main(String[] args) throws FileNotFoundException, JAXBException, IOException, InvalidEntryException, DuplicateEntryException, Exception {
        if (args.length != 4
                || "-?".equals(args[0])) {

            System.out.println(USAGE);

            return;
        }

        /* NOTE: to mute logging: Logger.getLogger("").setLevel(Level.OFF); */
        
        Class c = Class.forName(args[1]);

        JAXBContext ctx = JAXBContext.newInstance(c);

        File baseDir = new File(args[3]);

        final DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final ArrayList<Document> docs = new ArrayList<>();

        ctx.generateSchema(new SchemaOutputResolver() {
            
            @Override
            public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
                Document doc = docBuilder.newDocument();
                docs.add(doc);
                Date now = new java.util.Date();
                doc.appendChild(doc.createComment("Created: " + now.toString()));
                doc.appendChild(doc.createComment("By: regxmllib build " + BuildVersionSingleton.getBuildVersion()));
                doc.appendChild(doc.createComment("See: https://github.com/sandflow/regxmllib"));
                return new DOMResult(doc, suggestedFileName);
            }
        });

        Transformer tr = TransformerFactory.newInstance().newTransformer();

        tr.setOutputProperty(OutputKeys.INDENT, "yes");

        for (int i = 0; i < docs.size(); i++) {
            
           tr.transform(
                    new DOMSource(docs.get(i)),
                    new StreamResult(new File(baseDir, c.getName()+ (i == 0 ? "" : "." + i) + ".xsd"))
            );
        }

    }
}
