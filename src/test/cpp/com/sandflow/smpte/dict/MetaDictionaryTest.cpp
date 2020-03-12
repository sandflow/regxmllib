/*
* Copyright (c), Pierre-Anthony Lemieux (pal@palemieux.com)
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

#include <com/sandflow/smpte/regxml/dict/MetaDictionaryCollection.h>
#include <xercesc/util/PlatformUtils.hpp>
#include <xercesc/sax/ErrorHandler.hpp>
#include "xercesc/sax/SAXParseException.hpp"
#include "xercesc/framework/StdOutFormatTarget.hpp"
#include "xercesc/framework/LocalFileFormatTarget.hpp"
#include <xercesc/parsers/XercesDOMParser.hpp>
#include <com/sandflow/smpte/regxml/dict/importers/XMLImporter.h>
#include "com/sandflow/smpte/regxml/MXFFragmentBuilder.h"
#include "xercesc/dom/DOMErrorHandler.hpp"
#include <string>
#include <fstream>


using namespace rxml;

const XMLCh* _getFirstTextNodeText(xercesc::DOMElement *e) {

	for (xercesc::DOMNode *n = e->getFirstChild(); n != NULL; n = n->getNextSibling()) {

		if (n->getNodeType() == xercesc::DOMNode::TEXT_NODE) {

			return n->getNodeValue();

		}

	}

	return NULL;

}

bool _compareDOMElements(xercesc::DOMElement *e1, xercesc::DOMElement *e2) {

	if (xercesc::XMLString::compareIString(e1->getNamespaceURI(), e2->getNamespaceURI()) != 0) {
		return false;
	}

	if (xercesc::XMLString::compareIString(e1->getLocalName(), e2->getLocalName()) != 0) {
		return false;
	}

	xercesc::DOMNamedNodeMap *attrs1 = e1->getAttributes();
	xercesc::DOMNamedNodeMap *attrs2 = e2->getAttributes();

	if (attrs1->getLength() != attrs2->getLength()) {
		return false;
	}

	for (XMLSize_t i = 0; i < attrs1->getLength(); i++) {

		xercesc::DOMAttr* a1 = (xercesc::DOMAttr*) attrs1->item(i);

		xercesc::DOMAttr* a2 = (xercesc::DOMAttr*)attrs2->getNamedItemNS(a1->getNamespaceURI(), a1->getLocalName());

		if (a2 == NULL) {
			return false;
		}

		if (xercesc::XMLString::compareIString(a1->getValue(), a2->getValue()) != 0) {
			return false;
		}

	}

	xercesc::DOMElement *c1 = e1->getFirstElementChild();
	xercesc::DOMElement *c2 = e2->getFirstElementChild();

	/* TODO: detect mixed contents */

	if (c1 == NULL && c2 == NULL) {

		if (xercesc::XMLString::compareIString(e1->getTextContent(), e2->getTextContent()) != 0) {
			return false;
		}

	} else {

		while (c1 || c2) {

			if (c1 && (!c2)) return false;

			if ((!c1) && c2) return false;

			if (!_compareDOMElements(c1, c2)) return false;

			c1 = c1->getNextElementSibling();
			c2 = c2->getNextElementSibling();
		}

	}

	return true;
		 
}

class MyErrorHandler : public xercesc::ErrorHandler {
public:


	// Inherited via ErrorHandler
	virtual void warning(const xercesc::SAXParseException & exc) {
		std::cout << DOMHelper::toUTF8(exc.getMessage());
	}

	virtual void error(const xercesc::SAXParseException & exc) {
		std::cout << DOMHelper::toUTF8(exc.getMessage());
	}

	virtual void fatalError(const xercesc::SAXParseException & exc) {
		std::cout << DOMHelper::toUTF8(exc.getMessage()) ;
	}

	virtual void resetErrors() {}

};

class MyEventHandler : public EventHandler {
public:
	virtual bool info(const std::string &code, const std::string &reason, const std::string &where) {
		std::cerr << code << ": " << reason << " at " << where << std::endl;
		return true;
	}

	virtual bool warn(const std::string &code, const std::string &reason, const std::string &where) {
		std::cerr << code << ": " << reason << " at " << where << std::endl;
		return true;
	}

	virtual bool error(const std::string &code, const std::string &reason, const std::string &where) {
		std::cerr << code << ": " << reason << " at " << where << std::endl;
		return true;
	}

	virtual bool fatal(const std::string &code, const std::string &reason, const std::string &where) {
		std::cerr << code << ": " << reason << " at " << where << std::endl;
		return true;
	}

};

int main(int argc, char **argv) {

	int ret_val = 0;

	char const *dicts_fname[] = { "www-ebu-ch-metadata-schemas-ebucore-smpte-class13-element.xml",
		"www-ebu-ch-metadata-schemas-ebucore-smpte-class13-group.xml",
		"www-ebu-ch-metadata-schemas-ebucore-smpte-class13-type.xml",
		"www-smpte-ra-org-reg-335-2012.xml",
		"www-smpte-ra-org-reg-335-2012-13-1-aaf.xml",
		"www-smpte-ra-org-reg-335-2012-13-1-amwa-as10.xml",
		"www-smpte-ra-org-reg-335-2012-13-1-amwa-as11.xml",
		"www-smpte-ra-org-reg-335-2012-13-1-amwa-as12.xml",
		"www-smpte-ra-org-reg-335-2012-13-1-amwa-rules.xml",
		"www-smpte-ra-org-reg-335-2012-13-4-archive.xml",
		"www-smpte-ra-org-reg-335-2012-13-12-as11.xml",
		"www-smpte-ra-org-reg-335-2012-13-13.xml",
		"www-smpte-ra-org-reg-335-2012-14-9.xml",
		"www-smpte-ra-org-reg-395-2014.xml",
		"www-smpte-ra-org-reg-395-2014-13-1-aaf.xml",
		"www-smpte-ra-org-reg-395-2014-13-1-amwa-as10.xml",
		"www-smpte-ra-org-reg-395-2014-13-1-amwa-as11.xml",
		"www-smpte-ra-org-reg-395-2014-13-1-amwa-as12.xml",
		"www-smpte-ra-org-reg-395-2014-13-1-amwa-as-common.xml",
		"www-smpte-ra-org-reg-395-2014-13-4-archive.xml",
		"www-smpte-ra-org-reg-395-2014-13-12-as11.xml",
		"www-smpte-ra-org-reg-395-2014-13-13.xml",
		"www-smpte-ra-org-reg-395-2014-14-9.xml",
		"www-smpte-ra-org-reg-2003-2012.xml",
		"www-smpte-ra-org-reg-2003-2012-13-1-amwa-as11.xml",
		"www-smpte-ra-org-reg-2003-2012-13-1-amwa-as12.xml",
		"www-smpte-ra-org-reg-2003-2012-13-4-archive.xml",
		"www-smpte-ra-org-reg-2003-2012-13-12-as11.xml" };

	xercesc::XMLPlatformUtils::Initialize();

	xercesc::XercesDOMParser *parser = new xercesc::XercesDOMParser();

	MyErrorHandler handler;
	parser->setErrorHandler(&handler);

	parser->setDoNamespaces(true);

	MetaDictionaryCollection mds;

	MyEventHandler evthandler;

	for (int i = 0; i < 26; i++) {

		std::string dict_path = "resources/regxml-dicts/";

		dict_path += dicts_fname[i];

		std::cout << dicts_fname[i] << std::endl;

		parser->parse(dict_path.c_str());

		xercesc::DOMDocument *doc = parser->getDocument();

		MetaDictionary *md = new MetaDictionary();

		XMLImporter::fromDOM(*doc, *md, &evthandler);

		mds.addDictionary(md);

	}

	XMLCh tempStr[3] = { xercesc::chLatin_L, xercesc::chLatin_S, xercesc::chNull };
	xercesc::DOMImplementation *impl = xercesc::DOMImplementationRegistry::getDOMImplementation(tempStr);
	xercesc::DOMLSSerializer   *ser = ((xercesc::DOMImplementationLS*)impl)->createLSSerializer();
	xercesc::DOMLSOutput       *output = ((xercesc::DOMImplementationLS*)impl)->createLSOutput();
	
	/*XMLFormatTarget *ft = new StdOutFormatTarget();*/

	char const *test_names[] = {
		"audio1",
		"audio2",
		"video1",
		"video2",
		"indirect",
		"utf8_embedded_text",
		"class14",
		"escape-chars"
	};

	for (size_t i = 0; i < sizeof(test_names) / sizeof(char*); i++) {

		std::cout << "Processing input file: " << test_names[i] << std::endl;

		std::string out_fname = std::string(test_names[i]) + ".mxf.xml";
		std::string ref_fname = "resources/regxml-files/" + std::string(test_names[i]) + ".xml";
		std::string in_fname = "resources/mxf-files/" + std::string(test_names[i]) + ".mxf";


		xercesc::LocalFileFormatTarget *ft = new xercesc::LocalFileFormatTarget(out_fname.c_str());

		output->setByteStream(ft);

		if (ser->getDomConfig()->canSetParameter(xercesc::XMLUni::fgDOMWRTFormatPrettyPrint, true))
			ser->getDomConfig()->setParameter(xercesc::XMLUni::fgDOMWRTFormatPrettyPrint, true);

		xercesc::DOMDocument *doc = impl->createDocument();

		std::ifstream f(in_fname.c_str(), std::ifstream::in | std::ifstream::binary);

		if (!f.good()) {

			std::cout << "Cannot read input file: " << test_names[i] << std::endl;

			ret_val |= 1;

			continue;
		}

		xercesc::DOMDocumentFragment* frag = MXFFragmentBuilder::fromInputStream(f, mds, NULL, NULL, *doc, &evthandler);

		doc->appendChild(frag);

		/* write genearated fragment */

		ser->write(doc, output);

		/* compare to reference file */

		parser->parse(ref_fname.c_str());

		xercesc::DOMDocument *ref_doc = parser->getDocument();

		if (!_compareDOMElements(ref_doc->getDocumentElement(), doc->getDocumentElement())) {

			std::cout << "Comparison failed: " << test_names[i] << std::endl;

			ret_val |= 1;

		}

		doc->release();

		delete ft;
	}

	/* free heap */
	
	for (std::map<std::string, MetaDictionary*>::const_iterator it = mds.getDictionatries().begin();
		it != mds.getDictionatries().end();
		it++) {
		delete it->second;
	}
	
	output->release();
	ser->release();
	delete parser;
	
	xercesc::XMLPlatformUtils::Terminate();

	return ret_val;
}