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
import com.sandflow.smpte.regxml.MXFFragmentBuilder;
import com.sandflow.smpte.regxml.dict.MetaDictionary;
import com.sandflow.smpte.regxml.dict.MetaDictionaryCollection;
import com.sandflow.smpte.regxml.dict.exceptions.IllegalDefinitionException;
import com.sandflow.smpte.regxml.dict.exceptions.IllegalDictionaryException;
import com.sandflow.smpte.util.UL;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

/**
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
public class RegXMLDump {

    private final static Logger LOG = Logger.getLogger(RegXMLDump.class.getName());

    private static final UL ESSENCE_DESCRIPTOR_KEY
            = new UL(new byte[]{0x06, 0x0e, 0x2b, 0x34, 0x02, 0x01, 0x01, 0x01, 0x0D, 0x01, 0x01, 0x01, 0x01, 0x01, 0x24, 0x00});

    private static final UL PREFACE_KEY
            = UL.fromURN("urn:smpte:ul:060e2b34.027f0101.0d010101.01012f00");
    

    private final static String USAGE = "Dump header metadata of an MXF file as a RegXML structure.\n"
            + "  Usage:\n"
            + "     RegXMLDump ( -all | -ed ) -d regxmldictionary1 regxmldictionary2 regxmldictionary3 ... -i mxffile\n"
            + "     RegXMLDump -?\n"
            + "  Where:\n"
            + "     -all: dumps all header metadata\n"
            + "     -ed: dumps only the first essence descriptor found\n";

    public static void main(String[] args) throws IOException, EOFException, KLVException, ParserConfigurationException, JAXBException, FragmentBuilder.RuleException, TransformerException, IllegalDefinitionException, IllegalDictionaryException {

        if (args.length < 5
                || "-?".equals(args[0])
                || (!"-d".equals(args[1]))
                || (!"-i".equals(args[args.length - 2]))) {

            System.out.println(USAGE);

            return;
        }

        MetaDictionaryCollection mds = new MetaDictionaryCollection();

        for (int i = 2; i < args.length - 2; i++) {

            /* load the regxml metadictionary */
            FileReader fr = new FileReader(args[i]);

            /* add it to the dictionary group */
            mds.addDictionary(MetaDictionary.fromXML(fr));

        }

        /* retrieve the mxf file */
        FileInputStream f = new FileInputStream(args[args.length - 1]);
        
        /* create dom */
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        doc.setXmlStandalone(true);

        /* date and build version */
        Date now = new java.util.Date();
        doc.appendChild(doc.createComment("Created: " + now.toString()));
        doc.appendChild(doc.createComment("From: " + args[args.length - 1]));
        doc.appendChild(doc.createComment("By: regxmllib build " + BuildVersionSingleton.getBuildVersion()));
        doc.appendChild(doc.createComment("See: https://github.com/sandflow/regxmllib"));
        
        try {
            
            UL root = "-ed".equals(args[0]) ? ESSENCE_DESCRIPTOR_KEY : PREFACE_KEY;
            
            DocumentFragment df = MXFFragmentBuilder.fromInputStream(f, mds, root, doc);
            
            doc.appendChild(df);

        } catch (MXFFragmentBuilder.MXFException | FragmentBuilder.RuleException | KLVException | ParserConfigurationException e) {
            LOG.severe(e.getMessage());
        }
        
        /* write DOM to file */
        Transformer tr = TransformerFactory.newInstance().newTransformer();

        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        tr.transform(
                new DOMSource(doc),
                new StreamResult(System.out)
        );

    }
}
