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
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
            + "     RegXMLDump ( -all | -ed ) ( -header | -footer ) -d regxmldictionary1 regxmldictionary2 regxmldictionary3 ... -i mxffile\n"
            + "     RegXMLDump -?\n"
            + "  Where:\n"
            + "     -all: dumps all header metadata (default) \n"
            + "     -ed: dumps only the first essence descriptor found\n"
            + "     -header: dumps metadata from the header partition\n"
            + "     -footer: dumps metadata from the footer partition\n";

    public static void main(String[] args) throws IOException, EOFException, KLVException, ParserConfigurationException, JAXBException, FragmentBuilder.RuleException, TransformerException, IllegalDefinitionException, IllegalDictionaryException {
        
        boolean error = false;
        Boolean isFooterPartition = null;
        Boolean isEssenceDescriptorOnly = null;
        MetaDictionaryCollection mds = null;
        SeekableByteChannel f = null;
        
        for(int i = 0; i < args.length;) {
            
            if ( "-?".equals(args[i]) ) {
                
                    error = true;
                    break;
                
            } else if ("-ed".equals(args[i])) {
                
                if(isEssenceDescriptorOnly != null) {
                    error = true;
                    break;
                }
                
                isEssenceDescriptorOnly = true;
                
                i++;
                
            } else if ("-all".equals(args[i])) {
                
                if(isEssenceDescriptorOnly != null) {
                    error = true;
                    break;

                }
                
                isEssenceDescriptorOnly = false;
                
                i++;
                
            } else if ("-footer".equals(args[i])) {
                
                if(isFooterPartition != null) {
                    error = true;
                    break;
                }
                
                isFooterPartition = true;
                
                i++;
                
            } else if ("-header".equals(args[i])) {
                
                if(isFooterPartition != null) {
                    error = true;
                    break;
                }
                
                isFooterPartition = false;   
                
                i++;
                
            } else if ("-d".equals(args[i])) {
                
                if (mds != null) {
                    error = true;
                    break;
                }
                
                i++;
                
                mds = new MetaDictionaryCollection();

                for (; i < args.length && args[i].charAt(0) != '-' ; i++) {
                    
                    /* load the regxml metadictionary */
                    FileReader fr = new FileReader(args[i]);

                    /* add it to the dictionary group */
                    mds.addDictionary(MetaDictionary.fromXML(fr));

                }
                
                if (mds.getDictionaries().isEmpty()) {
                    error = true;
                    break;
                }
                                
            } else if ("-i".equals(args[i])) {
                
                i++;
                
                if (f != null || i >= args.length || args[i].charAt(0) == '-') {
                    
                    error = true;
                    break;
                    
                }
                
                /* retrieve the mxf file */
                
                f = Files.newByteChannel(Paths.get(args[i++]));
                
                
            } else {
                
                error = true;
                break;
                
            }
            
            
        }
        
        if (isFooterPartition == null) {
            isFooterPartition = false;
        }
        
        if (isEssenceDescriptorOnly == null) {
            isEssenceDescriptorOnly = false;
        }
        
        if (error || f == null || mds == null) {
            System.out.println(USAGE);
            return;   
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
            
            UL root = isEssenceDescriptorOnly ? ESSENCE_DESCRIPTOR_KEY : PREFACE_KEY;
                        
            if (isFooterPartition) {
               
                MXFFragmentBuilder.seekFooterPartition(f);
            
            } else {
                
                MXFFragmentBuilder.seekHeaderPartition(f);
                
            }
                        
            InputStream is = Channels.newInputStream(f);
                        
            DocumentFragment df = MXFFragmentBuilder.fromInputStream(is, mds, root, doc);
            
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
