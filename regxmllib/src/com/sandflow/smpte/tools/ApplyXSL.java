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

import com.sandflow.smpte.register.exception.DuplicateEntryException;
import com.sandflow.smpte.register.exception.InvalidEntryException;
import com.sandflow.smpte.util.ExcelCSVParser;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
public class ApplyXSL {

    private final static String USAGE = "Applies an XSL stylesheet to a document.\n"
            + "  Usage: ApplyXSLTTransform -x xslfile -i infile -o outfile\n"
            + "         ApplyXSLTTransform -?";

    public static void main(String[] args) throws FileNotFoundException, ExcelCSVParser.SyntaxException, JAXBException, IOException, InvalidEntryException, DuplicateEntryException, Exception {
        if (args.length != 6
                || (!"-x".equals(args[0]))
                || (!"-i".equals(args[2]))
                || (!"-o".equals(args[4]))
                || "-?".equals(args[0])) {

            System.out.println(USAGE);

            return;
        }

        /* mute logging */
        /* TODO: add switch to enable warnings */
        Logger.getLogger("").setLevel(Level.OFF);
        Source xsls = new StreamSource(args[1]);
        Source is = new StreamSource(args[3]);
        StreamResult or = new StreamResult(args[5]);
               
        TransformerFactory tf = TransformerFactory.newInstance();

        tf.setAttribute("debug", Boolean.TRUE);
        
         Transformer tr =  tf.newTransformer(xsls);
          

        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.transform(is, or); 

    }
}
