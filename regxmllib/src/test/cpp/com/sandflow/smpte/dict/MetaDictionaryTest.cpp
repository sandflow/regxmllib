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

#include <com/sandflow/smpte/regxml/dict/MetaDictionary.h>
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
//#include <unistd.h>
//#include <direct.h>

XERCES_CPP_NAMESPACE_USE

class MyErrorHandler : public ErrorHandler {
public:


	// Inherited via ErrorHandler
	virtual void warning(const SAXParseException & exc) {
		std::cout << DOMHelper::toUTF8(exc.getMessage());
	}

	virtual void error(const SAXParseException & exc) {
		std::cout << DOMHelper::toUTF8(exc.getMessage());
	}

	virtual void fatalError(const SAXParseException & exc) {
		std::cout << DOMHelper::toUTF8(exc.getMessage()) ;
	}

	virtual void resetErrors() {}

};

int main(int argc, void **argv) {

	char const *dicts_fname[] = { "www-smpte-ra-org-reg-335-2012-13-1-amwa-as12.xml"
,"www-smpte-ra-org-reg-335-2012-13-1-amwa-rules.xml"
,"www-smpte-ra-org-reg-335-2012-13-4-archive.xml"
,"www-smpte-ra-org-reg-335-2012-13-12-as11.xml"
,"www-smpte-ra-org-reg-335-2012-13-13.xml"
,"www-smpte-ra-org-reg-395-2014.xml"
,"www-smpte-ra-org-reg-395-2014-13-1-aaf.xml"
,"www-smpte-ra-org-reg-395-2014-13-1-amwa-as10.xml"
,"www-smpte-ra-org-reg-395-2014-13-1-amwa-as11.xml"
,"www-smpte-ra-org-reg-395-2014-13-1-amwa-as12.xml"
,"www-smpte-ra-org-reg-395-2014-13-1-amwa-as-common.xml"
,"www-smpte-ra-org-reg-395-2014-13-4-archive.xml"
,"www-smpte-ra-org-reg-395-2014-13-12-as11.xml"
,"www-smpte-ra-org-reg-395-2014-13-13.xml"
,"www-smpte-ra-org-reg-2003-2012.xml"
,"www-smpte-ra-org-reg-2003-2012-13-1-amwa-as11.xml"
,"www-smpte-ra-org-reg-2003-2012-13-1-amwa-as12.xml"
,"www-smpte-ra-org-reg-2003-2012-13-4-archive.xml"
,"www-smpte-ra-org-reg-2003-2012-13-12-as11.xml"
,"www-ebu-ch-metadata-schemas-ebucore-smpte-class13-element.xml"
,"www-ebu-ch-metadata-schemas-ebucore-smpte-class13-group.xml"
,"www-ebu-ch-metadata-schemas-ebucore-smpte-class13-type.xml"
,"www-smpte-ra-org-reg-335-2012.xml"
,"www-smpte-ra-org-reg-335-2012-13-1-aaf.xml"
,"www-smpte-ra-org-reg-335-2012-13-1-amwa-as10.xml"
,"www-smpte-ra-org-reg-335-2012-13-1-amwa-as11.xml" };

	XMLPlatformUtils::Initialize();

	XercesDOMParser *parser = new XercesDOMParser();

	MyErrorHandler handler;
	parser->setErrorHandler(&handler);

	parser->setDoNamespaces(true);

	MetaDictionaryCollection mds;

	for (int i = 0; i < 26; i++) {

		std::string dict_path = "resources/regxml-dicts/";

		dict_path += dicts_fname[i];

		std::cout << dicts_fname[i] << std::endl;

		parser->parse(dict_path.c_str());

		DOMDocument *doc = parser->getDocument();

		MetaDictionary *md = new MetaDictionary();

		XMLImporter::fromDOM(*doc, *md);

		mds.addDictionary(md);

	}

	XMLCh tempStr[3] = { chLatin_L, chLatin_S, chNull };
	DOMImplementation *impl = DOMImplementationRegistry::getDOMImplementation(tempStr);
	DOMLSSerializer   *ser = ((DOMImplementationLS*)impl)->createLSSerializer();
	DOMLSOutput       *output = ((DOMImplementationLS*)impl)->createLSOutput();
	
	/*XMLFormatTarget *ft = new StdOutFormatTarget();*/
	
	LocalFileFormatTarget *ft = new LocalFileFormatTarget("audio1.mxf.xml");


	output->setByteStream(ft);

	if (ser->getDomConfig()->canSetParameter(XMLUni::fgDOMWRTFormatPrettyPrint, true))
		ser->getDomConfig()->setParameter(XMLUni::fgDOMWRTFormatPrettyPrint, true);

	DOMDocument *doc = impl->createDocument();

	std::ifstream f("resources/sample-files/audio1.mxf", std::ifstream::in | std::ifstream::binary);

	if (!f.good()) exit(1);

	AUID rootclasskey = "urn:smpte:ul:060e2b34.027f0101.0d010101.01012400";

	DOMDocumentFragment* frag = MXFFragmentBuilder::fromInputStream(f, mds, NULL, &rootclasskey, *doc);

	doc->appendChild(frag);

	ser->write(doc, output);
	
	for (std::map<std::string, MetaDictionary*>::const_iterator it = mds.getDictionatries().begin();
		it != mds.getDictionatries().end();
		it++) {
		delete it->second;
	}
	
	output->release();
	ser->release();
	doc->release();
	
	XMLPlatformUtils::Terminate();
}