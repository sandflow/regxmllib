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

import com.sandflow.smpte.klv.exception.KLVException;
import com.sandflow.smpte.regxml.FragmentBuilder;
import com.sandflow.smpte.regxml.XMLSchemaBuilder;
import com.sandflow.smpte.regxml.dict.IllegalDefinitionException;
import com.sandflow.smpte.regxml.dict.IllegalDictionaryException;
import com.sandflow.smpte.regxml.dict.MetaDictionary;
import com.sandflow.smpte.regxml.dict.MetaDictionaryGroup;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
public class GenerateDictionaryXMLSchema {

     private final static String USAGE = "Generate XML Schema for RegXML Metadictionaries.\n"
            + "  Usage:\n"
            + "     GenerateDictionaryXMLSchema -d regxmldictionary1 regxmldictionary2 regxmldictionary3 ... -o outputdir\n"
            + "     GenerateDictionaryXMLSchema -?\n";

    public static void main(String[] args) throws IOException, EOFException, KLVException, ParserConfigurationException, JAXBException, FragmentBuilder.RuleException, TransformerException, IllegalDefinitionException, IllegalDictionaryException, Exception {

        if (args.length < 4
                || "-?".equals(args[0])
                || (!"-d".equals(args[0]))
                || (!"-o".equals(args[args.length - 2]))) {

            System.out.println(USAGE);

            return;
        }

        /* load the metadictionaries */
        MetaDictionaryGroup mds = new MetaDictionaryGroup();

        for (int i = 1; i < args.length - 2; i++) {

            /* load the regxml metadictionary */
            FileReader fr = new FileReader(args[i]);

            MetaDictionary md = MetaDictionary.fromXML(fr);

            /* add it to the dictionary group */
            mds.addDictionary(md);

        }


        /* create the fragment builder */
        XMLSchemaBuilder sb = new XMLSchemaBuilder();

        sb.setDefinitionResolver(mds);

        for (MetaDictionary md : mds.getDictionaries()) {
            String fname = md.getSchemeURI().getAuthority() + md.getSchemeURI().getPath();

            File f = new File(args[args.length - 1], fname.replaceAll("[^a-zA-Z0-9]", "-") + ".xsd");

            Document doc = sb.xmlSchemaFromDictionary(md);

            Transformer tr = TransformerFactory.newInstance().newTransformer();

            tr.setOutputProperty(OutputKeys.INDENT, "yes");

            tr.transform(
                    new DOMSource(doc),
                    new StreamResult(f)
            );

        }
    }

}
