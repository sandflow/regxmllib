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
import com.sandflow.smpte.regxml.dict.exceptions.IllegalDefinitionException;
import com.sandflow.smpte.regxml.dict.exceptions.IllegalDictionaryException;
import static com.sandflow.smpte.regxml.dict.importers.RegisterImporter.fromRegister;
import com.sandflow.util.events.Event;
import com.sandflow.util.events.EventHandler;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import junit.framework.TestCase;
import org.xml.sax.SAXException;

/**
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
public class XMLSchemaBuilderTest extends TestCase {

    private final static Logger LOG = Logger.getLogger(XMLSchemaBuilderTest.class.getName());

    private MetaDictionaryCollection mds_catsup;
    private MetaDictionaryCollection mds_brown_sauce;
    private MetaDictionaryCollection mds_ponzu;
    private MetaDictionaryCollection mds_snapshot;

    public XMLSchemaBuilderTest(String testName) {
        super(testName);
    }

    private MetaDictionaryCollection buildDictionaryCollection(
        String er_path,
        String gr_path,
        String tr_path
    ) throws JAXBException, IOException, DuplicateEntryException, Exception {

        /* load the registers */
        Reader fe = new InputStreamReader(ClassLoader.getSystemResourceAsStream(er_path));
        assertNotNull(fe);

        Reader fg = new InputStreamReader(ClassLoader.getSystemResourceAsStream(gr_path));
        assertNotNull(fg);

        Reader ft = new InputStreamReader(ClassLoader.getSystemResourceAsStream(tr_path));
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

        return fromRegister(treg, greg, ereg, evthandler);

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        /* build the dictionaries */
        mds_catsup = buildDictionaryCollection(
            "resources/registers/catsup/Elements.xml",
            "resources/registers/catsup/Groups.xml",
            "resources/registers/catsup/Types.xml"
        );

        assertNotNull(mds_catsup);

        /* build the dictionaries */
        mds_brown_sauce = buildDictionaryCollection(
            "resources/registers/brown_sauce/Elements.xml",
            "resources/registers/brown_sauce/Groups.xml",
            "resources/registers/brown_sauce/Types.xml"
        );

        assertNotNull(mds_brown_sauce);

        /* build the dictionaries */
        mds_ponzu = buildDictionaryCollection(
            "resources/registers/ponzu/Elements.xml",
            "resources/registers/ponzu/Groups.xml",
            "resources/registers/ponzu/Types.xml"
        );

        assertNotNull(mds_ponzu);

        /* build the dictionaries */
        mds_snapshot = buildDictionaryCollection(
            "resources/registers/snapshot/Elements.xml",
            "resources/registers/snapshot/Groups.xml",
            "resources/registers/snapshot/Types.xml"
        );

        assertNotNull(mds_snapshot);

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private void generateXMLSchema(MetaDictionaryCollection mds) throws ParserConfigurationException, KLVException, XMLSchemaBuilder.RuleException, SAXException, IOException {

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

            sb.fromDictionary(md);
        }

    }

    public void testAgainstBrownSauce() throws IOException, EOFException, KLVException, ParserConfigurationException, JAXBException, FragmentBuilder.RuleException, TransformerException, IllegalDefinitionException, IllegalDictionaryException, Exception {

        generateXMLSchema(mds_brown_sauce);

    }

    public void testAgainstCatsup() throws IOException, EOFException, KLVException, ParserConfigurationException, JAXBException, FragmentBuilder.RuleException, TransformerException, IllegalDefinitionException, IllegalDictionaryException, Exception {

        generateXMLSchema(mds_catsup);

    }

    public void testAgainstPonzu() throws IOException, EOFException, KLVException, ParserConfigurationException, JAXBException, FragmentBuilder.RuleException, TransformerException, IllegalDefinitionException, IllegalDictionaryException, Exception {

        generateXMLSchema(mds_ponzu);

    }

    public void testAgainstSnapshot() throws IOException, EOFException, KLVException, ParserConfigurationException, JAXBException, FragmentBuilder.RuleException, TransformerException, IllegalDefinitionException, IllegalDictionaryException, Exception {

        generateXMLSchema(mds_snapshot);

    }

}
