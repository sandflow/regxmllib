/*
 * Copyright (c) 2015, Pierre-Anthony Lemieux (pal@sandflow.com)
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

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.sandflow.smpte.klv.exceptions.KLVException;
import com.sandflow.smpte.regxml.FragmentBuilder;
import com.sandflow.smpte.regxml.dict.exceptions.IllegalDefinitionException;
import com.sandflow.smpte.regxml.dict.exceptions.IllegalDictionaryException;

import junit.framework.TestCase;

/**
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
public class RegXMLDumpTest extends TestCase {

    public RegXMLDumpTest(String testName) {
        super(testName);
    }

    public void testMetadictionaryDirectory() throws IOException, EOFException, KLVException, ParserConfigurationException, JAXBException, FragmentBuilder.RuleException, TransformerException, IllegalDefinitionException, IllegalDictionaryException, Exception {

        String args[] = new String[] {
            "-all",
            "-d",
            Paths.get(ClassLoader.getSystemResource("regxml-dicts").toURI()).toString(),
            "-i",
            Paths.get(ClassLoader.getSystemResource("mxf-files/video1.mxf").toURI()).toString()
        };

        final PrintStream oldStdout = System.out;
        final PrintStream nullStdout = new PrintStream(new ByteArrayOutputStream());
        System.setOut(nullStdout);
        RegXMLDump.main(args);
        System.setOut(oldStdout);

    }

    public void testMetadictionaryFiles() throws IOException, EOFException, KLVException, ParserConfigurationException, JAXBException, FragmentBuilder.RuleException, TransformerException, IllegalDefinitionException, IllegalDictionaryException, Exception {

        ArrayList<String> args = new ArrayList<String>();

        args.add("-all");

        args.add("-d");

        File mds[] = (new File(ClassLoader.getSystemResource("regxml-dicts").toURI())).listFiles();

        for(int i = 0; i < mds.length; i++) {

            args.add(mds[i].getAbsolutePath());

        }

        args.add("-i");

        args.add(Paths.get(ClassLoader.getSystemResource("mxf-files/video1.mxf").toURI()).toString());

        final PrintStream oldStdout = System.out;
        final PrintStream nullStdout = new PrintStream(new ByteArrayOutputStream());
        System.setOut(nullStdout);
        RegXMLDump.main(args.toArray(new String[args.size()]));
        System.setOut(oldStdout);

    }

}
