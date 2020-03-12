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
import com.sandflow.smpte.regxml.dict.MetaDictionaryCollection;
import static com.sandflow.smpte.regxml.dict.importers.RegisterImporter.fromRegister;
import com.sandflow.smpte.util.UL;
import com.sandflow.util.events.Event;
import com.sandflow.util.events.EventHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.TestCase;
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
public class MXFFragmentBuilderTest extends TestCase {

    private final static Logger LOG = Logger.getLogger(MXFFragmentBuilderTest.class.getName());

    private static final UL PREFACE_KEY
        = UL.fromURN("urn:smpte:ul:060e2b34.027f0101.0d010101.01012f00");

    private MetaDictionaryCollection mds_catsup;
    private MetaDictionaryCollection mds_brown_sauce;
    private MetaDictionaryCollection mds_snapshot;
    private MetaDictionaryCollection mds_ponzu;

    private DocumentBuilder db;

    public MXFFragmentBuilderTest(String testName) {
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

        return fromRegister(treg, greg, ereg, evthandler);

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        /* build the dictionaries */
        mds_catsup = buildDictionaryCollection(
            "registers/catsup/Elements.xml",
            "registers/catsup/Groups.xml",
            "registers/catsup/Types.xml"
        );

        assertNotNull(mds_catsup);

        /* build the dictionaries */
        mds_brown_sauce = buildDictionaryCollection(
            "registers/brown_sauce/Elements.xml",
            "registers/brown_sauce/Groups.xml",
            "registers/brown_sauce/Types.xml"
        );

        assertNotNull(mds_brown_sauce);

        /* build the dictionaries */
        mds_snapshot = buildDictionaryCollection(
            "registers/snapshot/Elements.xml",
            "registers/snapshot/Groups.xml",
            "registers/snapshot/Types.xml"
        );

        assertNotNull(mds_snapshot);

        mds_ponzu = buildDictionaryCollection(
            "registers/ponzu/Elements.xml",
            "registers/ponzu/Groups.xml",
            "registers/ponzu/Types.xml"
        );

        assertNotNull(mds_ponzu);


        /* setup the doc builder */
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setCoalescing(true);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setIgnoringComments(true);
        db = dbf.newDocumentBuilder();

        assertNotNull(db);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private void compareGeneratedVsRef(MetaDictionaryCollection mds, String spath, String refpath) throws IOException, SAXException, KLVException, MXFFragmentBuilder.MXFException, ParserConfigurationException, FragmentBuilder.RuleException {

        /* get the sample files */
        InputStream sampleis = ClassLoader.getSystemResourceAsStream(spath);
        assertNotNull(sampleis);

        /* build the regxml fragment */
        Document gendoc = db.newDocument();

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
        InputStream refis = ClassLoader.getSystemResourceAsStream(refpath);
        assertNotNull(refis);

        Document refdoc = db.parse(refis);
        assertNotNull(refdoc);

        /* compare the ref vs the generated */
        assertTrue(compareDOMElement(gendoc.getDocumentElement(), refdoc.getDocumentElement()));

    }

    public void testAudio1AgainstCatsup() throws Exception {

        compareGeneratedVsRef(mds_catsup, "mxf-files/audio1.mxf", "regxml-files/audio1.xml");

    }

    public void testAudio2AgainstCatsup() throws Exception {

        compareGeneratedVsRef(mds_catsup, "mxf-files/audio2.mxf", "regxml-files/audio2.xml");

    }

    public void testVideo1AgainstCatsup() throws Exception {

        compareGeneratedVsRef(mds_catsup, "mxf-files/video1.mxf", "regxml-files/video1.xml");

    }

    public void testVideo2AgainstCatsup() throws Exception {

        compareGeneratedVsRef(mds_catsup, "mxf-files/video2.mxf", "regxml-files/video2.xml");

    }

    public void testIndirectAgainstCatsup() throws Exception {

        compareGeneratedVsRef(mds_catsup, "mxf-files/indirect.mxf", "regxml-files/indirect.xml");

    }

    public void testUTF8AgainstCatsup() throws Exception {

        compareGeneratedVsRef(mds_catsup, "mxf-files/utf8_embedded_text.mxf", "regxml-files/utf8_embedded_text.xml");

    }

    public void testEscapeCharsAgainstCatsup() throws Exception {

        compareGeneratedVsRef(mds_catsup, "mxf-files/escape-chars.mxf", "regxml-files/escape-chars.xml");

    }

    public void testAudio1AgainstBrownSauce() throws Exception {

        compareGeneratedVsRef(mds_brown_sauce, "mxf-files/audio1.mxf", "regxml-files/audio1.xml");

    }

    public void testAudio2AgainstBrownSauce() throws Exception {

        compareGeneratedVsRef(mds_brown_sauce, "mxf-files/audio2.mxf", "regxml-files/audio2.xml");

    }

    public void testVideo1AgainstBrownSauce() throws Exception {

        compareGeneratedVsRef(mds_brown_sauce, "mxf-files/video1.mxf", "regxml-files/video1.xml");

    }

    public void testVideo2AgainstBrownSauce() throws Exception {

        compareGeneratedVsRef(mds_brown_sauce, "mxf-files/video2.mxf", "regxml-files/video2.xml");

    }

    public void testIndirectAgainstBrownSauce() throws Exception {

        compareGeneratedVsRef(mds_brown_sauce, "mxf-files/indirect.mxf", "regxml-files/indirect.xml");

    }

    public void testUTF8AgainstBrownSauce() throws Exception {

        compareGeneratedVsRef(mds_brown_sauce, "mxf-files/utf8_embedded_text.mxf", "regxml-files/utf8_embedded_text.xml");

    }

    public void testEscapeCharsAgainstBrownSauce() throws Exception {

        compareGeneratedVsRef(mds_brown_sauce, "mxf-files/escape-chars.mxf", "regxml-files/escape-chars.xml");

    }

    public void testClass14AgainstSnapshot() throws Exception {

        compareGeneratedVsRef(mds_snapshot, "mxf-files/class14.mxf", "regxml-files/class14.xml");
    }

    public void testAudio1AgainstPonzu() throws Exception {

        compareGeneratedVsRef(mds_ponzu, "mxf-files/audio1.mxf", "regxml-files/audio1.xml");

    }

    public void testAudio2AgainstPonzu() throws Exception {

        compareGeneratedVsRef(mds_ponzu, "mxf-files/audio2.mxf", "regxml-files/audio2.xml");

    }

    public void testVideo1AgainstPonzu() throws Exception {

        compareGeneratedVsRef(mds_ponzu, "mxf-files/video1.mxf", "regxml-files/video1.xml");

    }

    public void testVideo2AgainstPonzu() throws Exception {

        compareGeneratedVsRef(mds_ponzu, "mxf-files/video2.mxf", "regxml-files/video2.xml");

    }

    public void testIndirectAgainstPonzu() throws Exception {

        compareGeneratedVsRef(mds_ponzu, "mxf-files/indirect.mxf", "regxml-files/indirect.xml");

    }

    public void testUTF8AgainstPonzu() throws Exception {

        compareGeneratedVsRef(mds_ponzu, "mxf-files/utf8_embedded_text.mxf", "regxml-files/utf8_embedded_text.xml");

    }

    public void testEscapeCharsAgainstPonzu() throws Exception {

        compareGeneratedVsRef(mds_ponzu, "mxf-files/escape-chars.mxf", "regxml-files/escape-chars.xml");

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
