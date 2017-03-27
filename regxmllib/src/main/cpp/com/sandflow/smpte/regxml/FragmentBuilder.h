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
 
#ifndef COM_SANDFLOW_SMPTE_REGXML_FRAGMENTBUILDER_H
#define COM_SANDFLOW_SMPTE_REGXML_FRAGMENTBUILDER_H

#include <string>
#include "com/sandflow/smpte/util/AUID.h"
#include "com/sandflow/smpte/regxml/dict/DefinitionResolver.h"
#include "com/sandflow/smpte/klv/Group.h"
#include <xercesc/dom/DOM.hpp>
#include <iostream>
#include "com/sandflow/smpte/regxml/definitions/Definition.h"
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
#include "com/sandflow/util/events/EventHandler.h"
#include "com/sandflow/util/events/NullEventHandler.h"
#include "com/sandflow/util/strformat.h"
#include <com/sandflow/smpte/regxml/definitions/DefinitionVisitor.h>
#include "com/sandflow/smpte/mxf/MXFStream.h"
#include "com/sandflow/util/membuf.h"
#include <map>
#include "com/sandflow/smpte/mxf/Set.h"
#include "com/sandflow/util/DOMHelper.h"
#include <iomanip>


XERCES_CPP_NAMESPACE_USE

class FragmentBuilder {

public:

	static const std::string UNKNOWN_GROUP_ERROR_CODE;
	static const std::string UNKNOWN_PROPERTY_ERROR_CODE;
	static const std::string VERSION_BYTE_MISMATCH_ERROR_CODE;
	static const std::string UNEXPECTED_DEFINITION_ERROR_CODE;
	static const std::string CIRCULAR_STRONG_REFERENCE_ERROR_CODE;
	static const std::string UNEXPECTED_BYTE_ORDER_ERROR_CODE;
	static const std::string UNKNOWN_TYPE_ERROR_CODE;
	static const std::string MISSING_UNIQUE_ERROR_CODE;
	static const std::string MISSING_PRIMARY_PACKAGE_ERROR_CODE;
	static const std::string VALUE_LENGTH_MISMATCH_ERROR_CODE;
	static const std::string UNSUPPORTED_CHAR_TYPE_ERROR_CODE;
	static const std::string UNSUPPORTED_ENUM_TYPE_ERROR_CODE;
	static const std::string UNKNOWN_ENUM_VALUE_ERROR_CODE;
	static const std::string INVALID_IDAU_ERROR_CODE;
	static const std::string INVALID_INTEGER_VALUE_ERROR_CODE;
	static const std::string UNSUPPORTED_STRING_TYPE_ERROR_CODE;
	static const std::string INVALID_STRONG_REFERENCE_TYPE_ERROR_CODE;
	static const std::string STRONG_REFERENCE_NOT_FOUND_ERROR_CODE;

	class Exception : public std::runtime_error
	{
	public:
		Exception(std::string const& message) : std::runtime_error(message) {}
		Exception(const char* message) : std::runtime_error(message) {}
	};


	class AUIDNameResolver {

	public:

		std::string getLocalName(const AUID &enumid) { return ""; };

	};


	/**
	* Instantiates a FragmentBuilder. If the anamresolver argument is not null,
	* the FragmentBuilder will attempt to resolve the name of each AUID it
	* writes to the output and add it as an XML comment. If the evthandler
	* argument is not null, the Fragment builder will call back the caller with
	* events it encounters as it transforms a Triplet.
	*
	* @param defresolver Maps Group Keys to MetaDictionary definitions. Must
	* not be null;
	* @param setresolver Resolves Strong References to groups. Must not be
	* null.
	* @param anameresolver Resolves a AUID to a human-readable symbol. May be
	* null.
	* @param evthandler Calls back the caller when an event occurs. May be
	* null.
	*/
	FragmentBuilder(const DefinitionResolver &defresolver,
		const std::map<UUID, Set> &setresolver,
		const AUIDNameResolver *anameresolver = NULL,
		EventHandler *evthandler = &NULL_EVENTHANDLER);

	/**
	* Creates a RegXML Fragment, represented an XML DOM Document Fragment
	*
	* @param group KLV Group for which the Fragment will be generated.
	* @param document Document from which the XML DOM Document Fragment will be
	* created.
	*
	* @return XML DOM Document Fragment containing a single RegXML Fragment
	*
	* @throws KLVException
	* @throws com.sandflow.smpte.regxml.FragmentBuilder.RuleException
	*/
	DOMDocumentFragment* fromTriplet(const Group &group, DOMDocument &document);

private:

	/* CONST */

	static const UL INSTANCE_UID_ITEM_UL;
	static const UL AUID_UL;
	static const UL UUID_UL;
	static const UL DateStruct_UL;
	static const UL PackageID_UL;
	static const UL Rational_UL;
	static const UL TimeStruct_UL;
	static const UL TimeStamp_UL;
	static const UL VersionType_UL;
	static const UL ByteOrder_UL;
	static const UL Character_UL;
	static const UL Char_UL;
	static const UL UTF8Character_UL;
	static const UL ProductReleaseType_UL;
	static const UL Boolean_UL;
	static const UL PrimaryPackage_UL;
	static const UL LinkedGenerationID_UL;
	static const UL GenerationID_UL;
	static const UL ApplicationProductID_UL;

	static const std::string BYTEORDER_BE;
	static const std::string BYTEORDER_LE;


	static const std::string REGXML_NS;
	static const std::string XMLNS_NS;
	static const std::string UID_ATTR;
	static const std::string ACTUALTYPE_ATTR;

	/* METHODS */

	const Definition * findBaseTypeDefinition(const Definition *definition, const DefinitionResolver &defresolver);

	const Definition *findBaseDefinition(const Definition *definition);

	std::string getElementNSPrefix(const std::string &ns);

	std::vector<const PropertyDefinition*> getAllMembersOf(const ClassDefinition *cdef);

	static std::string generateISO8601Time(int hour, int minutes, int seconds, int millis);

	static std::string generateISO8601Date(int year, int month, int day);

	static void readCharacters(DOMElement *element, MXFInputStream &value, const CharacterTypeDefinition *definition, bool removeTrailingZeroes);

	void applyRule3(DOMNode *node, const Group &group);

	void applyRule4(DOMElement *element, MXFInputStream &value, const PropertyDefinition *propdef);

	void applyRule5(DOMElement *element, MXFInputStream &value, const Definition *definition);

	void applyRule5_1(DOMElement *element, MXFInputStream &value, const CharacterTypeDefinition *definition);

	void applyRule5_2(DOMElement *element, MXFInputStream &value, const EnumerationTypeDefinition *definition);

	void applyRule5_3(DOMElement *element, MXFInputStream &value, const ExtendibleEnumerationTypeDefinition* definition);

	void applyRule5_4(DOMElement *element, MXFInputStream &value, const FixedArrayTypeDefinition *definition);

	/* TODO: unsigned int or unsigned long for element count */

	void applyCoreRule5_4(DOMElement* element, MXFInputStream &value, const Definition *tdef, unsigned long elementcount);

	void applyRule5_5(DOMElement* element, MXFInputStream &value, const IndirectTypeDefinition *definition);

	void applyRule5_6(DOMElement *element, MXFInputStream &value, const IntegerTypeDefinition *definition);

	void applyRule5_7(DOMElement *element, MXFInputStream &value, const OpaqueTypeDefinition *definition);

	void applyRule5_8(DOMElement *element, MXFInputStream &value, const RecordTypeDefinition *definition);

	void applyRule5_9(DOMElement *element, MXFInputStream &value, const RenameTypeDefinition *definition);

	void applyRule5_10(DOMElement *element, MXFInputStream &value, const SetTypeDefinition *definition);

	void applyRule5_11(DOMElement *element, MXFInputStream &value, const StreamTypeDefinition *definition);

	void applyRule5_12(DOMElement *element, MXFInputStream &value, const StringTypeDefinition *definition);

	void applyRule5_13(DOMElement *element, MXFInputStream &value, const StrongReferenceTypeDefinition *definition);

	/*void applyRule5_alpha(DOMElement *element, MXFInputStream &value, const FloatTypeDefinition *definition) {


			double val = 0;

			switch (definition.getSize()) {
			case HALF:

				val = HalfFloat.toDouble(dis.readUnsignedShort());

				break;
			case SINGLE:
				val = dis.readFloat();
				break;
			case DOUBLE:
				val = dis.readDouble();
				break;
			}

			element.setTextContent(Double.toString(val));


	}*/

	void applyRule5_beta(DOMElement *element, MXFInputStream &value, const LensSerialFloatTypeDefinition *definition);

	void applyRule5_14(DOMElement *element, MXFInputStream &value, const VariableArrayTypeDefinition *definition);

	void applyRule5_15(DOMElement *element, MXFInputStream &value, const WeakReferenceTypeDefinition *typedefinition);

	/* MEMBERS */

	const DefinitionResolver &defresolver;
	const std::map<UUID, Set> &setresolver;
	const FragmentBuilder::AUIDNameResolver *anameresolver;
	std::map<std::string, std::string> nsprefixes;
	EventHandler *evthandler;

};

#endif