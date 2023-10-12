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
package com.sandflow.smpte.regxml;

import com.sandflow.smpte.klv.exceptions.KLVException;
import com.sandflow.smpte.register.ElementsRegister;
import com.sandflow.smpte.register.GroupsRegister;
import com.sandflow.smpte.register.TypesRegister;
import com.sandflow.smpte.register.exceptions.DuplicateEntryException;
import com.sandflow.smpte.regxml.dict.MetaDictionary;
import com.sandflow.smpte.regxml.dict.MetaDictionaryCollection;
import static com.sandflow.smpte.regxml.dict.importers.RegisterImporter.fromRegister;
import com.sandflow.util.events.Event;
import com.sandflow.util.events.EventHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static org.junit.Assert.*;


/**
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
@RunWith(Parameterized.class)
public class XMLSchemaBuilderTest {

    private final static Logger LOG = Logger.getLogger(XMLSchemaBuilderTest.class.getName());

    private final MetaDictionaryCollection mds;

    private final static String registers_dir_path = "registers";

    @Parameters(name = "Release: {0}")
    public static Iterable<? extends Object> data() throws URISyntaxException {
        File f = new File(ClassLoader.getSystemResource(XMLSchemaBuilderTest.registers_dir_path).toURI());

        return Arrays.asList(f.list());
     }

    public XMLSchemaBuilderTest(String register_name) throws JAXBException, IOException, DuplicateEntryException, Exception {

        final String register_dir = XMLSchemaBuilderTest.registers_dir_path + "/" + register_name + "/";
        
        /* load the registers */

        Reader fe = new InputStreamReader(ClassLoader.getSystemResourceAsStream(register_dir + "Elements.xml"));
        assertNotNull(fe);

        Reader fg = new InputStreamReader(ClassLoader.getSystemResourceAsStream(register_dir + "Groups.xml"));
        assertNotNull(fg);

        Reader ft = new InputStreamReader(ClassLoader.getSystemResourceAsStream(register_dir + "Types.xml"));
        assertNotNull(ft);

        ElementsRegister ereg = ElementsRegister.fromXML(fe);
        assertNotNull(ereg);

        GroupsRegister greg = GroupsRegister.fromXML(fg);
        assertNotNull(greg);

        TypesRegister treg = TypesRegister.fromXML(ft);
        assertNotNull(treg);

        /* build the dictionaries */

        EventHandler evthandler = new EventHandler() {

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
                }
                return true;
            }
        };

        this.mds = fromRegister(treg, greg, ereg, evthandler);

        assertNotNull(mds);
    }

    @Test
    public void testGenerateXMLSchema() throws ParserConfigurationException, KLVException, XMLSchemaBuilder.RuleException, SAXException, IOException {

        /* create the fragment builder */
        XMLSchemaBuilder sb = new XMLSchemaBuilder(
            this.mds,
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

        for (MetaDictionary md : this.mds.getDictionaries()) {

            Document doc = sb.fromDictionary(md);

            assertNotNull(doc);
        }

    }

}
