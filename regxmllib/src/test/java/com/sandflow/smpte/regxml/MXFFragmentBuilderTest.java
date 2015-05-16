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

import com.sandflow.smpte.register.ElementsRegister;
import com.sandflow.smpte.register.GroupsRegister;
import com.sandflow.smpte.register.TypesRegister;
import com.sandflow.smpte.regxml.dict.MetaDictionaryCollection;
import static com.sandflow.smpte.regxml.dict.importers.RegisterImporter.fromRegister;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
public class MXFFragmentBuilderTest extends TestCase {

    public MXFFragmentBuilderTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of fromInputStream method, of class MXFFragmentBuilder.
     */
    public void testFromInputStream() throws Exception {

        //System.out.println(MXFFragmentBuilderTest.class.getResource("/resources/sample-files/audio1.mxf"));
        /* get the sample files */
        InputStream audio1is = MXFFragmentBuilderTest.class.getResourceAsStream("/resources/sample-files/audio1.mxf");
        assertNotNull(audio1is);

        InputStream video1is = getClass().getResourceAsStream("/resources/sample-files/video1.mxf");
        assertNotNull(video1is);

        /* load the registers */
        Reader fe = new InputStreamReader(getClass().getResourceAsStream("/resources/reference-registers/Elements.xml"));
        assertNotNull(fe);

        Reader fg = new InputStreamReader(getClass().getResourceAsStream("/resources/reference-registers/Groups.xml"));
        assertNotNull(fg);

        Reader ft = new InputStreamReader(getClass().getResourceAsStream("/resources/reference-registers/Types.xml"));
        assertNotNull(ft);

        ElementsRegister ereg = ElementsRegister.fromXML(fe);
        assertNotNull(ereg);

        GroupsRegister greg = GroupsRegister.fromXML(fg);
        assertNotNull(greg);

        TypesRegister treg = TypesRegister.fromXML(ft);
        assertNotNull(treg);

        /* build the dictionaries */
        MetaDictionaryCollection mds = fromRegister(treg, greg, ereg);

        assertNotNull(mds);

        /* generate the fragment */
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setCoalescing(true);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setIgnoringComments(true);
        DocumentBuilder db = dbf.newDocumentBuilder();

        /* audio fragment */
        Document audio1doc = db.newDocument();

        DocumentFragment dfaudio1 = MXFFragmentBuilder.fromInputStream(audio1is, mds, null, audio1doc);

        assertNotNull(dfaudio1);

        audio1doc.appendChild(dfaudio1);

        /* video fragment */
        Document video1doc = db.newDocument();

        DocumentFragment dfvideo1 = MXFFragmentBuilder.fromInputStream(video1is, mds, null, video1doc);

        assertNotNull(dfvideo1);

        video1doc.appendChild(dfvideo1);

        /* load the reference document */
        InputStream audio1refis = getClass().getResourceAsStream("/resources/reference-files/audio1.xml");
        assertNotNull(audio1refis);

        InputStream video1refis = getClass().getResourceAsStream("/resources/reference-files/video1.xml");
        assertNotNull(video1refis);

        Document audio1ref = db.parse(audio1refis);
        assertNotNull(audio1ref);

        Document video1ref = db.parse(video1refis);
        assertNotNull(video1ref);

        assertTrue(compareDOMElement(audio1doc.getDocumentElement(), audio1ref.getDocumentElement()));

        assertTrue(compareDOMElement(video1doc.getDocumentElement(), video1ref.getDocumentElement()));
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

    boolean compareDOMElement(Element el1, Element el2) {

        List<Element> elems1 = getElements(el1);
        List<Element> elems2 = getElements(el2);

        if (elems1.size() != elems2.size()) {
            return false;
        }

        Map<String, String> attrs1 = getAttributes(el1);
        Map<String, String> attrs2 = getAttributes(el2);

        for (Entry<String, String> entry : attrs1.entrySet()) {
            if (!entry.getValue().equals(attrs2.get(entry.getKey()))) {
                return false;
            }
        }

        for (int i = 0; i < elems1.size(); i++) {

            if (!elems1.get(i).getNodeName().equals(elems2.get(i).getNodeName())) {
                return false;
            }

            if (!compareDOMElement(elems1.get(i), elems2.get(i))) {
                return false;
            }
        }

        return true;

    }

}
