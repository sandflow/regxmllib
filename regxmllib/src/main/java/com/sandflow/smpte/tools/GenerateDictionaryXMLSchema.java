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

import com.sandflow.smpte.klv.exceptions.KLVException;
import com.sandflow.smpte.regxml.FragmentBuilder;
import com.sandflow.smpte.regxml.XMLSchemaBuilder;
import com.sandflow.smpte.regxml.dict.MetaDictionary;
import com.sandflow.smpte.regxml.dict.MetaDictionaryCollection;
import com.sandflow.smpte.regxml.dict.exceptions.IllegalDefinitionException;
import com.sandflow.smpte.regxml.dict.exceptions.IllegalDictionaryException;
import com.sandflow.util.events.Event;
import com.sandflow.util.events.EventHandler;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
public class GenerateDictionaryXMLSchema {

    private final static Logger LOG = Logger.getLogger(GenerateDictionaryXMLSchema.class.getName());

    protected final static String USAGE = "Generate XML Schema for RegXML Metadictionaries.\n"
        + "  Usage:\n"
        + "     GenerateDictionaryXMLSchema -d regxmldictionary1 regxmldictionary2 regxmldictionary3 ... -o outputdir\n"
        + "     GenerateDictionaryXMLSchema -?\n";

    private final static String XMLSCHEMA_NS = "http://www.w3.org/2001/XMLSchema";

    /**
     * Usage is specified at {@link #USAGE}
     */
    public static void main(String[] args) throws IOException, EOFException, KLVException, ParserConfigurationException, JAXBException, FragmentBuilder.RuleException, TransformerException, IllegalDefinitionException, IllegalDictionaryException, Exception {

        if (args.length < 4
            || "-?".equals(args[0])
            || (!"-d".equals(args[0]))
            || (!"-o".equals(args[args.length - 2]))) {

            System.out.println(USAGE);

            return;
        }

        /* load the metadictionaries */
        MetaDictionaryCollection mds = new MetaDictionaryCollection();

        for (int i = 1; i < args.length - 2; i++) {

            /* load the regxml metadictionary */
            FileReader fr = new FileReader(args[i]);

            MetaDictionary md = MetaDictionary.fromXML(fr);

            /* add it to the dictionary group */
            mds.addDictionary(md);

        }


        /* generate a schema document that includes all registers */
        Document masterxsd = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        Element masterxsd_root = masterxsd.createElementNS(XMLSCHEMA_NS, "schema");

        masterxsd.appendChild(masterxsd_root);

        /* generate the common xsd declarations */
        InputStream regis = GenerateDictionaryXMLSchema.class.getResourceAsStream("/resources/reg.xsd");

        Files.copy(regis, Paths.get(args[args.length - 1], "reg.xsd"), StandardCopyOption.REPLACE_EXISTING);

        Element masterxsd_import = masterxsd.createElementNS(XMLSCHEMA_NS, "import");

        masterxsd_import.setAttribute("namespace", XMLSchemaBuilder.REGXML_NS);

        masterxsd_import.setAttribute("schemaLocation", "reg.xsd");

        masterxsd_root.appendChild(masterxsd_import);

        /* create transformer to ouput a concrete representation of our DOMs */
        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "yes");


        /* create the fragment builder */
        XMLSchemaBuilder sb = new XMLSchemaBuilder(
            mds,
            new EventHandler() {

                @Override
                public boolean handle(Event evt) {
                    String msg = evt.getCode().getClass().getCanonicalName() + "::" + evt.getCode().toString() + " " + evt.getMessage();

                    switch (evt.getSeverity()) {
                        case ERROR:
                        case FATAL:
                            LOG.severe(msg);
                            break;
                        case INFO:
                            LOG.info(msg);
                            break;
                        case WARN:
                            LOG.warning(msg);
                            break;
                    }
                    return true;
                }
            }
        );

        for (MetaDictionary md : mds.getDictionaries()) {

            String fname = (md.getSchemeURI().getAuthority() + md.getSchemeURI().getPath()).replaceAll("[^a-zA-Z0-9]", "-")
                + ".xsd";

            /* add to master include xsd */
            masterxsd_import = masterxsd.createElementNS(XMLSCHEMA_NS, "import");

            masterxsd_import.setAttribute("namespace", md.getSchemeURI().toString());

            masterxsd_import.setAttribute("schemaLocation", fname);

            masterxsd_root.appendChild(masterxsd_import);

            /* create the XSD file */
            File f = new File(args[args.length - 1], fname);

            Document doc = sb.fromDictionary(md);

            /* date and build version */
            Date now = new java.util.Date();
            doc.insertBefore(
                doc.createComment("Created: " + now.toString()),
                doc.getDocumentElement()
            );
            doc.insertBefore(
                doc.createComment("By: regxmllib build " + BuildVersionSingleton.getBuildVersion()),
                doc.getDocumentElement()
            );
            doc.insertBefore(
                doc.createComment("See: https://github.com/sandflow/regxmllib"),
                doc.getDocumentElement()
            );

            tr.transform(
                new DOMSource(doc),
                new StreamResult(f)
            );

        }

        File includefile = new File(args[args.length - 1], "include.xsd");

        tr.transform(new DOMSource(masterxsd),
            new StreamResult(includefile)
        );

    }

}
