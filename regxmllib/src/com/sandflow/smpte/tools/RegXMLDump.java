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

import com.sandflow.smpte.klv.Group;
import com.sandflow.smpte.klv.KLVInputStream;
import com.sandflow.smpte.klv.LocalSet;
import com.sandflow.smpte.klv.LocalSetRegister;
import com.sandflow.smpte.klv.Triplet;
import com.sandflow.smpte.klv.exception.KLVException;
import com.sandflow.smpte.mxf.FillItem;
import com.sandflow.smpte.mxf.PartitionPack;
import com.sandflow.smpte.regxml.FragmentBuilder;
import com.sandflow.smpte.mxf.PrimerPack;
import com.sandflow.smpte.regxml.definition.ClassDefinition;
import com.sandflow.smpte.regxml.definition.Definition;
import com.sandflow.smpte.regxml.dict.IllegalDefinitionException;
import com.sandflow.smpte.regxml.dict.IllegalDictionaryException;
import com.sandflow.smpte.regxml.dict.MetaDictionary;
import com.sandflow.smpte.regxml.dict.MetaDictionaryGroup;
import com.sandflow.smpte.util.AUID;
import com.sandflow.smpte.util.CountingInputStream;
import com.sandflow.smpte.util.UL;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
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

    private static final UL ESSENCE_DESCRIPTOR_UL
            = new UL(new byte[]{0x06, 0x0e, 0x2b, 0x34, 0x02, 0x01, 0x01, 0x01, 0x0D, 0x01, 0x01, 0x01, 0x01, 0x01, 0x24, 0x00});

    private static final UL INDEX_TABLE_SEGMENT_UL
            = UL.fromURN("urn:smpte:ul:060e2b34.02530101.0d010201.01100100");

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

        MetaDictionaryGroup mds = new MetaDictionaryGroup();

        for (int i = 2; i < args.length - 2; i++) {

            /* load the regxml metadictionary */
            FileReader fr = new FileReader(args[i]);

            /* add it to the dictionary group */
            mds.addDictionary(MetaDictionary.fromXML(fr));

        }


        /* create the fragment builder */
        FragmentBuilder fb = new FragmentBuilder();

        fb.setDefinitionResolver(mds);

        /* retrieve the mxf file */
        FileInputStream f = new FileInputStream(args[args.length - 1]);
        
        CountingInputStream cis = new CountingInputStream(f);

        /* look for the partition pack */
        KLVInputStream kis = new KLVInputStream(cis);

        PartitionPack pp = null;

        for (Triplet t; (t = kis.readTriplet()) != null;) {

            if ((pp = PartitionPack.fromTriplet(t)) != null) {
                break;
            }
        }

        if (pp == null) {
            System.err.println("No Partition Pack found.");
            return;
        }
        
        /* start counting header metadata bytes */
         cis.resetCount();
        
        /* look for the primer pack */
        LocalSetRegister localreg = null;

        for (Triplet t; (t = kis.readTriplet()) != null; cis.resetCount()) {
            
            /* skip fill items, if any */
            if (! t.getKey().equalsIgnoreVersion(FillItem.LABEL)) {
                localreg = PrimerPack.createLocalSetRegister(t);
                break;
            }

        }

        if (localreg == null) {
            System.err.println("No Primer Pack found");
        }

        fb.setLocaltags(localreg);

        /* capture all local sets within the header metadata */
        ArrayList<Group> gs = new ArrayList<>();

        for (Triplet t;
                cis.getCount() < pp.getHeaderByteCount()
                && (t = kis.readTriplet()) != null;) {
            
            if (t.getKey().equalsIgnoreVersion(INDEX_TABLE_SEGMENT_UL)) {
                
                /* stop if Index Table reached */
                
                LOG.warning("Index Table Segment encountered before Header Byte Count bytes read.");
                break;
            } else if (t.getKey().equalsIgnoreVersion(FillItem.LABEL)) {
                
                /* skip fill items */
                
                continue;
            }
            
            Group g = LocalSet.fromTriplet(t, localreg);

            if (g != null) {

                gs.add(g);

                fb.addGroup(g);
            } else {
                 LOG.log(Level.WARNING, "Failed to read Group: {0}", t.getKey().toString());        
            }
        }

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
            if ("-ed".equals(args[0])) {

                Group ed = null;

                Iterator<Group> iter = gs.iterator();

                /* find first essence descriptor */
                while (ed == null && iter.hasNext()) {

                    Group g = iter.next();

                    AUID tmpauid = new AUID(g.getKey());

                    /* go up the class hierarchy */
                    while (ed == null && tmpauid != null) {

                        Definition def = mds.getDefinition(tmpauid);

                        /* skip if not a class instance */
                        if (!(def instanceof ClassDefinition)) {
                            break;
                        }

                        /* is this an essence descriptor */
                        UL deful = def.getIdentification().asUL();

                        if (deful.equals(ESSENCE_DESCRIPTOR_UL, 0b1111101011111111 /*11111010 11111111*/)) {
                            ed = g;

                        } else {

                            /* get parent class */
                            tmpauid = ((ClassDefinition) def).getParentClass();
                        }
                    }

                }

                if (ed == null) {
                    System.err.println("No Essence Descriptor found");
                    return;
                }

                /* generate fragment */
                DocumentFragment df = fb.fragmentFromTriplet(ed, doc);

                // root elements
                doc.appendChild(df);

            } else {

                /* generate fragment */
                DocumentFragment df = fb.fragmentFromTriplet(gs.get(0), doc);

                // root elements
                doc.appendChild(df);

            }

        } catch (FragmentBuilder.RuleException | KLVException | ParserConfigurationException e) {
            LOG.severe(e.getMessage());
        }

        /* write DOM to file */
        Transformer tr = TransformerFactory.newInstance().newTransformer();

        tr.setOutputProperty(OutputKeys.INDENT, "yes");

        tr.transform(
                new DOMSource(doc),
                new StreamResult(System.out)
        );

    }
}
