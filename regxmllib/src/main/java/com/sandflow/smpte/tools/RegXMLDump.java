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

import com.sandflow.smpte.mxf.MXFFiles;
import com.sandflow.smpte.register.LabelsRegister;
import com.sandflow.smpte.regxml.FragmentBuilder;
import com.sandflow.smpte.regxml.MXFFragmentBuilder;
import com.sandflow.smpte.regxml.dict.MetaDictionary;
import com.sandflow.smpte.regxml.dict.MetaDictionaryCollection;
import com.sandflow.smpte.util.AUID;
import com.sandflow.smpte.util.UL;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
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
            + "     RegXMLDump ( -all | -ed ) ( -header | -footer | -auto ) (-l labelsregister) -d regxmldictionary1 regxmldictionary2 regxmldictionary3 ... -i mxffile\n"
            + "     RegXMLDump -?\n"
            + "  Where:\n"
            + "     -all: dumps all header metadata (default)\n"
            + "     -ed: dumps only the first essence descriptor found\n"
            + "     -l labelsregister: given a SMPTE labels register, inserts the symbol of labels as XML comment\n"
            + "     -header: dumps metadata from the header partition (default)\n"
            + "     -footer: dumps metadata from the footer partition\n"
            + "     -auto: dumps metadata from the footer partition if available and from the header if not\n";

    private enum TargetPartition {

        HEADER,
        FOOTER,
        AUTO
    }

    public static void main(String[] args) throws Exception {

        boolean error = false;
        TargetPartition selectedpartition = null;
        Boolean isEssenceDescriptorOnly = null;
        MetaDictionaryCollection mds = null;
        SeekableByteChannel f = null;
        FileReader labelreader = null;
        Path p = null;

        for (int i = 0; i < args.length;) {

            if ("-?".equals(args[i])) {

                error = true;
                break;

            } else if ("-ed".equals(args[i])) {

                if (isEssenceDescriptorOnly != null) {
                    error = true;
                    break;
                }

                isEssenceDescriptorOnly = true;

                i++;

            } else if ("-all".equals(args[i])) {

                if (isEssenceDescriptorOnly != null) {
                    error = true;
                    break;

                }

                isEssenceDescriptorOnly = false;

                i++;

            } else if ("-footer".equals(args[i])) {

                if (selectedpartition != null) {
                    error = true;
                    break;
                }

                selectedpartition = TargetPartition.FOOTER;

                i++;

            } else if ("-auto".equals(args[i])) {

                if (selectedpartition != null) {
                    error = true;
                    break;
                }

                selectedpartition = TargetPartition.AUTO;

                i++;

            } else if ("-header".equals(args[i])) {

                if (selectedpartition != null) {
                    error = true;
                    break;
                }

                selectedpartition = TargetPartition.HEADER;

                i++;

            } else if ("-d".equals(args[i])) {

                if (mds != null) {
                    error = true;
                    break;
                }

                i++;

                mds = new MetaDictionaryCollection();

                for (; i < args.length && args[i].charAt(0) != '-'; i++) {

                    /* load the regxml metadictionary */
                    FileReader fr = new FileReader(args[i]);

                    /* add it to the dictionary group */
                    mds.addDictionary(MetaDictionary.fromXML(fr));

                }

                if (mds.getDictionaries().isEmpty()) {
                    error = true;
                    break;
                }

            } else if ("-l".equals(args[i])) {

                if (labelreader != null) {
                    error = true;
                    break;
                }

                i++;

                labelreader = new FileReader(args[i]);

                i++;

            } else if ("-i".equals(args[i])) {

                i++;

                if (f != null || i >= args.length || args[i].charAt(0) == '-') {

                    error = true;
                    break;

                }

                /* retrieve the mxf file */
                p = Paths.get(args[i++]);

                if (p == null) {
                    error = true;
                    break;
                }

                f = Files.newByteChannel(p);

            } else {

                error = true;
                break;

            }

        }

        if (selectedpartition == null) {
            selectedpartition = TargetPartition.HEADER;
        }

        if (isEssenceDescriptorOnly == null) {
            isEssenceDescriptorOnly = false;
        }

        if (error || f == null || mds == null || p == null) {
            System.out.println(USAGE);
            return;
        }
        
        /* create an enum name resolver, if available */
        
        final LabelsRegister lr;
        
        if (labelreader != null) {
        
            lr = LabelsRegister.fromXML(labelreader);
        
        } else {
            
            lr = null;
            
        }
        
        FragmentBuilder.AUIDNameResolver anr = null;
              
        if (lr != null) {

            anr = new FragmentBuilder.AUIDNameResolver() {

                @Override
                public String getLocalName(AUID enumid) {
                    LabelsRegister.Entry e = lr.getEntryByUL(enumid.asUL());
                    
                    return e == null ? null : e.getSymbol();
                }

            };
        }

        /* create DOM */
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        doc.setXmlStandalone(true);

        UL root = isEssenceDescriptorOnly ? ESSENCE_DESCRIPTOR_KEY : PREFACE_KEY;

        DocumentFragment df = null;

        TargetPartition actualpartition
                = TargetPartition.AUTO.equals(selectedpartition)
                        ? TargetPartition.FOOTER : selectedpartition;

        boolean retry = true;
        
        /*
           if selectedpartition is AUTO, then try FOOTER first and then HEADER 
           if any exceptions occur
        */

        while (retry) {

            try {

                switch (actualpartition) {
                    case FOOTER:

                        if (MXFFiles.seekFooterPartition(f) < 0) {
                            throw new Exception("Footer partition not found");
                        }

                        break;

                    case HEADER:

                        if (MXFFiles.seekHeaderPartition(f) < 0) {
                            throw new Exception("Header partition not found");
                        }

                        break;

                }

                InputStream is = Channels.newInputStream(f);
               
                df = MXFFragmentBuilder.fromInputStream(is, mds, anr, root, doc);

            } catch (Exception e) {

                if (TargetPartition.AUTO.equals(selectedpartition)) {

                    /* if an exception occurred and the target partition is AUTO,
                     try again with the header partition */
                    
                    actualpartition = TargetPartition.HEADER;
                    
                    f.position(0);

                } else {
                    
                    /* otherwise give up */
                    
                    LOG.severe(e.getMessage());

                    throw e;
                }

            } finally {
                
                retry = false;
                
            }

        }

        /* date and build version */
        Date now = new java.util.Date();
        doc.appendChild(doc.createComment("Created: " + now.toString()));
        doc.appendChild(doc.createComment("From: " + p.getFileName().toString()));
        doc.appendChild(doc.createComment("Partition: " + actualpartition.name()));
        doc.appendChild(doc.createComment("By: regxmllib build " + BuildVersionSingleton.getBuildVersion()));
        doc.appendChild(doc.createComment("See: https://github.com/sandflow/regxmllib"));

        /* add regxml fragment */
        doc.appendChild(df);

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
