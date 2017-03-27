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

#include "XMLImporter.h"
#include "com/sandflow/util/DOMHelper.h"
#include "xercesc/util/TransService.hpp"
#include "xercesc/util/XMLString.hpp"
#include "com/sandflow/smpte/regxml/definitions/PropertyDefinition.h"
#include "com/sandflow/smpte/regxml/definitions/PropertyAliasDefinition.h"
#include "com/sandflow/smpte/regxml/definitions/ClassDefinition.h"
#include "com/sandflow/smpte/regxml/definitions/CharacterTypeDefinition.h"
#include "com/sandflow/smpte/regxml/definitions/EnumerationTypeDefinition.h"
#include "com/sandflow/smpte/regxml/definitions/ExtendibleEnumerationTypeDefinition.h"
#include "com/sandflow/smpte/regxml/definitions/FixedArrayTypeDefinition.h"
#include "com/sandflow/smpte/regxml/definitions/IndirectTypeDefinition.h"
#include "com/sandflow/smpte/regxml/definitions/IntegerTypeDefinition.h"
#include "com/sandflow/smpte/regxml/definitions/OpaqueTypeDefinition.h"
#include "com/sandflow/smpte/regxml/definitions/RecordTypeDefinition.h"
#include "com/sandflow/smpte/regxml/definitions/RenameTypeDefinition.h"
#include "com/sandflow/smpte/regxml/definitions/SetTypeDefinition.h"
#include "com/sandflow/smpte/regxml/definitions/StreamTypeDefinition.h"
#include "com/sandflow/smpte/regxml/definitions/StringTypeDefinition.h"
#include "com/sandflow/smpte/regxml/definitions/StrongReferenceTypeDefinition.h"
#include "com/sandflow/smpte/regxml/definitions/VariableArrayTypeDefinition.h"
#include "com/sandflow/smpte/regxml/definitions/WeakReferenceTypeDefinition.h"
#include "com/sandflow/smpte/regxml/definitions/LensSerialFloatTypeDefinition.h"
#include "com/sandflow/util/strformat.h"


const std::string XML_NS = "http://www.smpte-ra.org/schemas/2001-1b/2013/metadict";
const std::string NAME = "Extension";


void xmlAdapter(const char *str, int &num) {
	num = strtol(str, NULL, 10);
}

void xmlAdapter(const char *str, bool &b) {
	static const std::string TRUE = "true";

	b = (TRUE == str);
}

void xmlAdapter(const char *str, unsigned int &num) {
	num = (unsigned int)strtol(str, NULL, 10);
}

void xmlAdapter(const char *str, unsigned char &num) {
	num = (unsigned char)strtol(str, NULL, 10);
}

void xmlAdapter(const char *str, AUID &t) {
	t = AUID(str);
}

template<typename T> void xmlAdapter(const char *str, T &t) {
	t = str;
}

template<typename T> void xmlAdapter(const char *str, Optional<T> &t) {
	T tmp;

	xmlAdapter(str, tmp);

	t.set(tmp);
}

/**
 *
 * @throws XMLImporter::Exception
 */
template<typename T> void _readPropertyOfElement(
	DOMElement *parent,
	const char *namespaceURI,
	const char *localName,
	T& field,
	void(*xmladapter)(const char *, T&) = &xmlAdapter) {

	const XMLCh *text = DOMHelper::getElementTextContentByTagNameNS(
		parent,
		DOMHelper::fromUTF8(namespaceURI),
		DOMHelper::fromUTF8(localName)
	);

	if (!text) {

		throw XMLImporter::Exception(strf::fmt("Required property {} is missing", std::string(localName)));

	}

	xmladapter(DOMHelper::toUTF8(text), field);

}

template<typename T> void _readPropertyOfElement(
	DOMElement *parent,
	const char *namespaceURI,
	const char *localName,
	Optional<T>& field,
	void(*xmladapter)(const char *, Optional<T>&) = &xmlAdapter) {

	const XMLCh *text = DOMHelper::getElementTextContentByTagNameNS(
		parent,
		DOMHelper::fromUTF8(XML_NS),
		DOMHelper::fromUTF8(localName));

	if (text) {

		xmladapter(DOMHelper::toUTF8(text), field);

	} else {
		field.clear();
	}

}

void _readDefinition(DOMElement *element, Definition &def) {


	_readPropertyOfElement(element, XML_NS.c_str(), "Identification", def.identification);
	_readPropertyOfElement(element, XML_NS.c_str(), "Symbol", def.symbol);
	_readPropertyOfElement(element, XML_NS.c_str(), "Description", def.description);
	_readPropertyOfElement(element, XML_NS.c_str(), "Name", def.name);

}

void _readClassDefinition(DOMElement *element, ClassDefinition &def) {

	_readDefinition(element, def);

	_readPropertyOfElement(element, XML_NS.c_str(), "ParentClass", def.parentClass);
	_readPropertyOfElement(element, XML_NS.c_str(), "IsConcrete", def.concrete);

}

void _readPropertyDefinition(DOMElement *element, PropertyDefinition &def) {

	_readDefinition(element, def);

	if (def.symbol == "FormatVersion") {
		int b = 0;
	}

	_readPropertyOfElement(element, XML_NS.c_str(), "Type", def.type);
	_readPropertyOfElement(element, XML_NS.c_str(), "MemberOf", def.memberOf);
	_readPropertyOfElement(element, XML_NS.c_str(), "LocalIdentification", def.localIdentification);
	_readPropertyOfElement(element, XML_NS.c_str(), "IsUniqueIdentifier", def.uniqueIdentifier);
	_readPropertyOfElement(element, XML_NS.c_str(), "IsOptional", def.optional);

}

void _readPropertyAliasDefinition(DOMElement *element, PropertyAliasDefinition &def) {

	_readPropertyDefinition(element, def);

	_readPropertyOfElement(element, XML_NS.c_str(), "OriginalProperty", def.originalProperty);

}

void _readIntegerTypeDefinition(DOMElement *element, IntegerTypeDefinition &def) {

	_readDefinition(element, def);

	_readPropertyOfElement(element, XML_NS.c_str(), "Size", def.size);
	_readPropertyOfElement(element, XML_NS.c_str(), "IsSigned", def.isSigned);

}


void _readRenameTypeDefinition(DOMElement *element, RenameTypeDefinition &def) {

	_readDefinition(element, def);

	_readPropertyOfElement(element, XML_NS.c_str(), "RenamedType", def.renamedType);

}

void _readSetTypeDefinition(DOMElement *element, SetTypeDefinition &def) {

	_readDefinition(element, def);

	_readPropertyOfElement(element, XML_NS.c_str(), "ElementType", def.elementType);

}

void _readStringTypeDefinition(DOMElement *element, StringTypeDefinition &def) {

	_readDefinition(element, def);

	_readPropertyOfElement(element, XML_NS.c_str(), "ElementType", def.elementType);

}


void _readStrongReferenceTypeDefinition(DOMElement *element, StrongReferenceTypeDefinition &def) {

	_readDefinition(element, def);

	_readPropertyOfElement(element, XML_NS.c_str(), "ReferencedType", def.referencedType);

}

void _readVariableTypeDefinition(DOMElement *element, VariableArrayTypeDefinition &def) {

	_readDefinition(element, def);

	_readPropertyOfElement(element, XML_NS.c_str(), "ElementType", def.elementType);

}

void _readTypeDefinitionCharacter(DOMElement *element, CharacterTypeDefinition &def) {

	_readDefinition(element, def);

}

void _readFixedArrayTypeDefinition(DOMElement *element, FixedArrayTypeDefinition &def) {

	_readDefinition(element, def);

	_readPropertyOfElement(element, XML_NS.c_str(), "ElementType", def.elementType);
	_readPropertyOfElement(element, XML_NS.c_str(), "ElementCount", def.elementCount);

}

void _readRecordTypeDefinition(DOMElement *element, RecordTypeDefinition &def) {


	_readDefinition(element, def);

	/* read Members */

	DOMElement *membersElem = DOMHelper::getElementByTagNameNS(
		element,
		DOMHelper::fromUTF8(XML_NS),
		DOMHelper::fromUTF8("Members")
	);

	if (!membersElem) {

		throw new XMLImporter::Exception("Elements property missing");

	}

	DOMElement *memberElem = membersElem->getFirstElementChild();

	while (memberElem) {



		if (XMLString::compareIString(DOMHelper::fromUTF8("Name"), memberElem->getLocalName()) == 0) {

			def.members.resize(def.members.size() + 1);

			xmlAdapter(DOMHelper::toUTF8(memberElem->getTextContent()), def.members.back().name);

		} else if (XMLString::compareIString(DOMHelper::fromUTF8("Type"), memberElem->getLocalName()) == 0) {

			xmlAdapter(DOMHelper::toUTF8(memberElem->getTextContent()), def.members.back().type);

		}



		memberElem = memberElem->getNextElementSibling();
	}

}

void _readWeakReferenceTypeDefinition(DOMElement *element, WeakReferenceTypeDefinition &def) {

	_readDefinition(element, def);

	_readPropertyOfElement(element, XML_NS.c_str(), "ReferencedType", def.referencedType);

	DOMElement *tsetelem = DOMHelper::getElementByTagNameNS(
		element,
		DOMHelper::fromUTF8(XML_NS),
		DOMHelper::fromUTF8("TargetSet")
	);

	if (!tsetelem) {

		throw XMLImporter::Exception("TargetSet property missing");

	}

	/* check if there is any text content */
	/* xerces fails on transcoding zero-length streams in some versions */

	if (XMLString::stringLen(tsetelem->getTextContent()) != 0) {

		std::istringstream ss(DOMHelper::toUTF8(tsetelem->getTextContent()).c_str());

		std::string auid_str;

		while (ss >> auid_str)
			def.targetSet.push_back(AUID(auid_str));
		
	}

}


void _readEnumerationTypeDefinition(DOMElement *element, EnumerationTypeDefinition &def) {


	_readDefinition(element, def);

	_readPropertyOfElement(element, XML_NS.c_str(), "ElementType", def.elementType);

	/* read Elements */

	DOMElement *elementsElem = DOMHelper::getElementByTagNameNS(
		element,
		DOMHelper::fromUTF8(XML_NS),
		DOMHelper::fromUTF8("Elements")
	);

	if (!elementsElem) {

		throw new XMLImporter::Exception("Elements property missing");

	}

	DOMElement *elementElem = elementsElem->getFirstElementChild();

	while (elementElem) {



		if (XMLString::compareIString(DOMHelper::fromUTF8("Name"), elementElem->getLocalName()) == 0) {

			def.elements.resize(def.elements.size() + 1);

			xmlAdapter(DOMHelper::toUTF8(elementElem->getTextContent()), def.elements.back().name);

		} else if (XMLString::compareIString(DOMHelper::fromUTF8("Value"), elementElem->getLocalName()) == 0) {

			xmlAdapter(DOMHelper::toUTF8(elementElem->getTextContent()), def.elements.back().value);

		} else if (XMLString::compareIString(DOMHelper::fromUTF8("Description"), elementElem->getLocalName()) == 0) {

			xmlAdapter(DOMHelper::toUTF8(elementElem->getTextContent()), def.elements.back().description);

		}



		elementElem = elementElem->getNextElementSibling();
	}

}

void XMLImporter::fromDOM(DOMDocument & dom, MetaDictionary &md, EventHandler * ev)
{
	DOMElement *root = dom.getDocumentElement();

	if (NAME != DOMHelper::toUTF8(root->getLocalName()).c_str() ||
		XML_NS != DOMHelper::toUTF8(root->getNamespaceURI()).c_str()) {
		// FATAL
	}

	/* read SchemeID */

	DOMElement *schemeIDElement = DOMHelper::getElementByTagNameNS(root,
		DOMHelper::fromUTF8(XML_NS),
		DOMHelper::fromUTF8("SchemeID")
	);

	if (!schemeIDElement) {
		ev->fatal("SCHEME_ID_MISSING", "SchemeID element missing from the MetaDictionary");

		return;
	}

	AUID schemeID = TranscodeToStr(schemeIDElement->getTextContent(), "utf-8").str();

	md.setSchemeID(schemeID);

	/* read SchemeURI */

	DOMElement *schemeURIElement = DOMHelper::getElementByTagNameNS(
		root,
		DOMHelper::fromUTF8(XML_NS),
		DOMHelper::fromUTF8("SchemeURI")
	);

	if (!schemeURIElement) {
		ev->fatal("SCHEME_URI_MISSING", "SchemeURI element missing from the MetaDictionary");

		return;
	}

	std::string schemeURI = DOMHelper::toUTF8(schemeURIElement->getTextContent()).c_str();

	md.setSchemeURI(schemeURI);

	/* read and index definitions */

	DOMElement *definitions = DOMHelper::getElementByTagNameNS(
		root,
		DOMHelper::fromUTF8(XML_NS),
		DOMHelper::fromUTF8("MetaDefinitions")
	);

	if (!definitions) {
		ev->fatal("METADEFINITIONS_MISSING", "MetaDefinitions element missing from the MetaDictionary");

		return;
	}

	DOMElement *curelement = definitions->getFirstElementChild();

	while (curelement) {

		try {

			std::string localname = DOMHelper::toUTF8(curelement->getLocalName()).c_str();

			if (localname == "ClassDefinition") {

				ClassDefinition def;

				_readClassDefinition(curelement, def);

				def.ns = schemeURI;

				md.addDefinition(def);

			} else if (localname == "PropertyDefinition") {

				PropertyDefinition def;

				_readPropertyDefinition(curelement, def);

				def.ns = schemeURI;

				md.addDefinition(def);

			} else if (localname == "PropertyAliasDefinition") {

				PropertyAliasDefinition def;

				_readPropertyAliasDefinition(curelement, def);

				def.ns = schemeURI;

				md.addDefinition(def);

			} else if (localname == "TypeDefinitionCharacter") {

				CharacterTypeDefinition def;

				_readDefinition(curelement, def);

				def.ns = schemeURI;

				md.addDefinition(def);

			} else if (localname == "TypeDefinitionEnumeration") {

				EnumerationTypeDefinition def;

				_readEnumerationTypeDefinition(curelement, def);

				def.ns = schemeURI;

				md.addDefinition(def);

			} else if (localname == "TypeDefinitionExtendibleEnumeration") {

				ExtendibleEnumerationTypeDefinition def;

				_readDefinition(curelement, def);

				def.ns = schemeURI;

				md.addDefinition(def);

			} else if (localname == "TypeDefinitionFixedArray") {

				FixedArrayTypeDefinition def;

				_readFixedArrayTypeDefinition(curelement, def);

				def.ns = schemeURI;

				md.addDefinition(def);

			} else if (localname == "TypeDefinitionIndirect") {

				IndirectTypeDefinition def;

				_readDefinition(curelement, def);

				def.ns = schemeURI;

				md.addDefinition(def);

			} else if (localname == "TypeDefinitionInteger") {

				IntegerTypeDefinition def;

				_readIntegerTypeDefinition(curelement, def);

				def.ns = schemeURI;

				md.addDefinition(def);

			} else if (localname == "TypeDefinitionOpaque") {

				OpaqueTypeDefinition def;

				_readDefinition(curelement, def);

				def.ns = schemeURI;

				md.addDefinition(def);

			} else if (localname == "TypeDefinitionRecord") {

				RecordTypeDefinition def;

				_readRecordTypeDefinition(curelement, def);

				def.ns = schemeURI;

				md.addDefinition(def);

			} else if (localname == "TypeDefinitionRename") {

				RenameTypeDefinition def;

				_readRenameTypeDefinition(curelement, def);

				def.ns = schemeURI;

				md.addDefinition(def);

			} else if (localname == "TypeDefinitionSet") {

				SetTypeDefinition def;

				_readSetTypeDefinition(curelement, def);

				def.ns = schemeURI;

				md.addDefinition(def);

			} else if (localname == "TypeDefinitionStream") {

				StreamTypeDefinition def;

				_readDefinition(curelement, def);

				def.ns = schemeURI;

				md.addDefinition(def);

			} else if (localname == "TypeDefinitionString") {

				StringTypeDefinition def;

				_readStringTypeDefinition(curelement, def);

				def.ns = schemeURI;

				md.addDefinition(def);

			} else if (localname == "TypeDefinitionStrongObjectReference") {

				StrongReferenceTypeDefinition def;

				_readStrongReferenceTypeDefinition(curelement, def);

				def.ns = schemeURI;

				md.addDefinition(def);

			} else if (localname == "TypeDefinitionVariableArray") {

				VariableArrayTypeDefinition def;

				_readVariableTypeDefinition(curelement, def);

				def.ns = schemeURI;

				md.addDefinition(def);

			} else if (localname == "TypeDefinitionWeakObjectReference") {

				WeakReferenceTypeDefinition def;

				_readWeakReferenceTypeDefinition(curelement, def);

				def.ns = schemeURI;

				md.addDefinition(def);


			} else if (localname == "TypeDefinitionLenseSerialFloat") {

				LensSerialFloatTypeDefinition def;

				_readDefinition(curelement, def);

				def.ns = schemeURI;

				md.addDefinition(def);

			} else {

				/* TODO : error, unknown type */

			}

		} catch (XMLImporter::Exception e) {
			std::cout << e.what() << std::endl;
		}

		curelement = curelement->getNextElementSibling();
	}

	return;
}

