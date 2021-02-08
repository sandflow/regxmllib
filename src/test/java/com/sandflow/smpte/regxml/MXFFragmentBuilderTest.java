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
import com.sandflow.smpte.regxml.dict.MetaDictionaryCollection;
import static com.sandflow.smpte.regxml.dict.importers.RegisterImporter.fromRegister;
import com.sandflow.smpte.util.UL;
import com.sandflow.util.events.Event;
import com.sandflow.util.events.EventHandler;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static org.junit.Assert.*;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
@RunWith(Parameterized.class)
public class MXFFragmentBuilderTest {

    private final static Logger LOG = Logger.getLogger(MXFFragmentBuilderTest.class.getName());

    private static final UL PREFACE_KEY
        = UL.fromURN("urn:smpte:ul:060e2b34.027f0101.0d010101.01012f00");

    private final static String registers_dir_path = "registers";
    private final static String mxf_files_dir_path = "mxf-files";
    private final static String ref_files_dir_path = "regxml-files";
    
    private final MetaDictionaryCollection mds;
    private final String ref_file_name;
    private final DocumentBuilder db;

    @Parameters(name = "Test file {1} against Release {0}")
    public static Iterable<? extends Object[]> data() throws URISyntaxException {

        File ref_files_dir = new File(ClassLoader.getSystemResource(MXFFragmentBuilderTest.ref_files_dir_path).toURI());

        ArrayList<String[]> params = new ArrayList<>();

        for (String ref_file_name : ref_files_dir.list()) {

            params.add(new String[]{"snapshot", ref_file_name});

        }

        return params;
     }

    public MXFFragmentBuilderTest(String register_codename, String ref_file_name) throws Exception {

        final String register_dir = MXFFragmentBuilderTest.registers_dir_path + "/" + register_codename + "/";
        
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
                    case FATAL:
                        LOG.severe(msg);
                        break;
                    case ERROR:
                    case INFO:
                    case WARN:
                        break;
                }
                return true;
            }
        };

        this.mds = fromRegister(treg, greg, ereg, evthandler);

        assertNotNull(mds);

        /* setup the doc builder */

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setCoalescing(true);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setIgnoringComments(true);

        this.db = dbf.newDocumentBuilder();

        assertNotNull(this.db);

        /* remember the test file name */

        this.ref_file_name = ref_file_name;
    }

    @Test
    public void testGeneratedAgainstReference() throws IOException, SAXException, KLVException, MXFFragmentBuilder.MXFException, ParserConfigurationException, FragmentBuilder.RuleException {

        /* get the sample files */
        final String mxf_file_name = this.ref_file_name.substring(0, this.ref_file_name.lastIndexOf('.')) + ".mxf";

        InputStream sampleis = ClassLoader.getSystemResourceAsStream(MXFFragmentBuilderTest.mxf_files_dir_path + "/" + mxf_file_name);

        assertNotNull(sampleis);

        /* build the regxml fragment */
        Document gendoc = this.db.newDocument();

        assertNotNull(gendoc);

        EventHandler evthandler = new EventHandler() {

            @Override
            public boolean handle(Event evt) {

                String msg = evt.getCode().getClass().getCanonicalName() + "::" + evt.getCode().toString() + " " + evt.getMessage();

                switch (evt.getSeverity()) {
                    case FATAL:
                        LOG.severe(msg);
                        return false;
                    case INFO:
                        LOG.info(msg);
                        break;
                    case ERROR:
                    case WARN:
                        LOG.warning(msg);
                }
                return true;
            }
        };

        DocumentFragment gendf = MXFFragmentBuilder.fromInputStream(sampleis, mds, null, evthandler, PREFACE_KEY, gendoc);

        assertNotNull(gendf);

        gendoc.appendChild(gendf);

        /* load the reference document */

        InputStream refis = ClassLoader.getSystemResourceAsStream(MXFFragmentBuilderTest.ref_files_dir_path + "/" + this.ref_file_name);
        assertNotNull(refis);

        Document refdoc = db.parse(refis);
        assertNotNull(refdoc);

        /* compare the ref vs the generated */
        assertTrue(compareDOMElement(gendoc.getDocumentElement(), refdoc.getDocumentElement()));

    }

    static Map<String, String> getAttributes(Element e) {

        NodeList nl = e.getChildNodes();
        HashMap<String, String> m = new HashMap<>();

        for (int i = 0; i < nl.getLength(); i++) {

            if (nl.item(i).getNodeType() == Node.ATTRIBUTE_NODE) {
                m.put(nl.item(i).getNodeName(), nl.item(i).getNodeValue());
            }

        }

        return m;
    }

    static List<Element> getElements(Element e) {

        NodeList nl = e.getChildNodes();
        ArrayList<Element> m = new ArrayList<>();

        for (int i = 0; i < nl.getLength(); i++) {

            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                m.add((Element) nl.item(i));
            }

        }

        return m;
    }

    String getFirstTextNodeText(Element e) {
        for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.TEXT_NODE) {
                return n.getNodeValue();
            }
        }

        return "";
    }

    boolean compareDOMElement(Element el1, Element el2) {

        List<Element> elems1 = getElements(el1);
        List<Element> elems2 = getElements(el2);

        if (elems1.size() != elems2.size()) {

            System.out.println(
                String.format(
                    "Sub element count of %s does not match reference.",
                    el1.getLocalName())
            );

            System.out.println("Left:");
            System.out.println(elems1);
            System.out.println("Right:");
            System.out.println(elems2);

            return false;
        }

        Map<String, String> attrs1 = getAttributes(el1);
        Map<String, String> attrs2 = getAttributes(el2);

        for (Entry<String, String> entry : attrs1.entrySet()) {
            if (!entry.getValue().equals(attrs2.get(entry.getKey()))) {

                System.out.println(
                    String.format(
                        "Attribute %s with value %s does not match reference.",
                        entry.getKey(),
                        entry.getValue())
                );

                return false;
            }
        }

        for (int i = 0; i < elems1.size(); i++) {

            if (!elems1.get(i).getNodeName().equals(elems2.get(i).getNodeName())) {

                System.out.println(
                    String.format(
                        "Element %s does not match reference.",
                        elems1.get(i).getNodeName())
                );

                return false;
            }

            String txt1 = getFirstTextNodeText(elems1.get(i)).trim();
            String txt2 = getFirstTextNodeText(elems2.get(i)).trim();

            if (!txt1.equals(txt2)) {
                System.out.println(
                    String.format(
                        "Text content at %s ('%s') does not match reference ('%s')",
                        elems1.get(i).getNodeName(),
                        txt1,
                        txt2)
                );
                return false;
            }

            if (!compareDOMElement(elems1.get(i), elems2.get(i))) {
                return false;
            }
        }

        return true;

    }

}
