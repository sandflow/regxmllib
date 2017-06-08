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

namespace rxml {

	class FragmentBuilder {

	public:

		/* EVENTS AND ERRORS */

		class UnknownGroupError : public Event {
		public:
			UnknownGroupError(const AUID &key, const std::string &where) :
				Event(
					"FragmentBuilder::UnknownGroupError",
					"Unknown Group ID " + key.to_string() + " encountered",
			where
				) {}
		};


		class UnknownPropertyError : public Event {
		public:
			UnknownPropertyError(const AUID &key, const std::string &where) :
				Event(
					"FragmentBuilder::UnknownPropertyError",
					"Unknown Property ID " + key.to_string() + " encountered",
			where
				) {}
		};

		class UnknownTypeError : public Event {
		public:
			UnknownTypeError(const AUID &key, const std::string &where) :
				Event(
					"FragmentBuilder::UnknownTypeError",
					"Unknown Type ID " + key.to_string() + " encountered",
			where
				) {}
		};

		class VersionByteMismatchError : public Event {
		public:
			VersionByteMismatchError(const AUID &actual_key, const AUID &dict_key, const std::string &where) :
				Event(
					"FragmentBuilder::VersionByteMismatchError",
					rxml::fmt(
						"The version byte of UL {} does not match the register value ({})",
						actual_key.to_string(),
						rxml::to_string(dict_key.asUL().getVersion())
					),
			where
				) {}
		};

		class UnexpectedDefinitionError : public Event {
		public:
			UnexpectedDefinitionError(const AUID &key, const std::string &expecteddef, const std::string &where) :
				Event(
					"FragmentBuilder::UnexpectedDefinitionError",
					"Definition ID " + key.to_string() + " is not a " + expecteddef + " definition",
			where
				) {}
		};

		class CircularStrongReferenceError : public Event {
		public:
			CircularStrongReferenceError(const std::string &setid, const std::string &where) :
				Event(
					"FragmentBuilder::CircularStrongReferenceError",
					"Circular Strong Reference to Set UID " + setid,
			where
				) {}
		};

		class MissingUniquePropertyError : public Event {
		public:
			MissingUniquePropertyError(const AUID &defid, const std::string &where) :
				Event(
					"FragmentBuilder::MissingUniquePropertyError",
					rxml::fmt(
						"Group definition {} has no IsUnique element.",
						defid.to_string()
					),
			where
				) {}
		};

		class MissingStrongReferenceError : public Event {
		public:
			MissingStrongReferenceError(const UUID &ref, const std::string &where) :
				Event(
					"FragmentBuilder::MissingStrongReferenceError",
					rxml::fmt(
						"Strong Reference target {} not found",
						ref.to_string()
					),
			where
				) {}
		};

		class MissingPrimaryPackageError : public Event {
		public:
			MissingPrimaryPackageError(const UUID &instanceid, const std::string &where) :
				Event(
					"FragmentBuilder::MissingPrimaryPackageError",
					rxml::fmt(
						"Target Primary Package with Instance UID {} not found.",
						instanceid.to_string()
					),
			where
				) {}
		};

		class UnknownEnumValueError : public Event {
		public:
			UnknownEnumValueError(long long value, const std::string &where) :
				Event(
					"FragmentBuilder::UnknownEnumValueError",
					"Undefined enumeration value: " + rxml::to_string(value),
			where
				) {}
		};

		class IOError : public Event {
		public:
			IOError(const std::ios::failure &failure, const std::string &where) :
				Event(
					"FragmentBuilder::IOError",
					failure.what(),
			where
				) {}
		};

		class UncaughtExceptionError : public Event {
		public:
			UncaughtExceptionError(const std::exception &e, const std::string &where) :
				Event(
					"FragmentBuilder::UncaughtExceptionError",
					e.what(),
			where
				) {}

			UncaughtExceptionError(const std::string &where) :
				Event(
					"FragmentBuilder::UncaughtExceptionError",
					"Unknown error",
			where
				) {}
		};

		class UnexpectedByteOrderError : public Event {
		public:
			UnexpectedByteOrderError(const std::string &where) :
				Event(
					"FragmentBuilder::UnexpectedByteOrderError",
					"ByteOrder set to little-endian: either the property is set"
					"incorrectly or the file does not conform to MXF. Processing will"
					"assume a big-endian byte order going forward.",
			where
				) {}
		};


		class UnknownByteOrderError : public Event {
		public:
			UnknownByteOrderError(const std::string &where) :
				Event(
					"FragmentBuilder::UnknownByteOrderError",
					"Unknown Byte Order value",
			where
				) {}
		};

		class UnsupportedCharTypeError : public Event {
		public:
			UnsupportedCharTypeError(const std::string &typesymbol, const std::string &where) :
				Event(
					"FragmentBuilder::UnsupportedCharTypeError",
					rxml::fmt(
						"Character type {} is not supported",
						typesymbol),
			where
				) {}
		};

		class UnsupportedStringTypeError : public Event {
		public:
			UnsupportedStringTypeError(const std::string &typesymbol, const std::string &where) :
				Event(
					"FragmentBuilder::UnsupportedStringTypeError",
					rxml::fmt(
						"String with element type {} is not supported",
						typesymbol),
			where
				) {}
		};

		class InvalidStrongReferenceTypeError : public Event {
		public:
			InvalidStrongReferenceTypeError(const std::string &typesymbol, const std::string &where) :
				Event(
					"FragmentBuilder::InvalidStrongReferenceTypeError",
					rxml::fmt(
						"Target {} of Strong Reference Type is not a class",
						typesymbol),
			where
				) {}
		};

		class UnsupportedEnumTypeError : public Event {
		public:
			UnsupportedEnumTypeError(const std::string &typesymbol, const std::string &where) :
				Event(
					"FragmentBuilder::UnsupportedEnumTypeError",
					rxml::fmt(
						"Enum type {} is not supported",
						typesymbol),
			where
				) {}
		};

		class Exception : public std::runtime_error {
		public:
			Exception(const char* what) : std::runtime_error(what) {}
			Exception(const std::string& what) : std::runtime_error(what) {}
		};

		class AUIDNameResolver {

		public:

			virtual const std::string *getLocalName(const AUID &enumid) const = 0;

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

		void readCharacters(DOMElement *element, MXFInputStream &value, const CharacterTypeDefinition *definition, bool removeTrailingZeroes);

		static void addInformativeComment(DOMElement * element, std::string comment);

		void appendCommentWithAUIDName(AUID auid, DOMElement * elem);

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

}
#endif