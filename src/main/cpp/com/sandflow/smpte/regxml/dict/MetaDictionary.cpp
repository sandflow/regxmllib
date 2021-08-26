/*
* Copyright (c) 2017, Pierre-Anthony Lemieux <pal@palemieux.com>
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

#include "MetaDictionary.h"
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
#include "com/sandflow/smpte/regxml/definitions/FloatTypeDefinition.h"

namespace rxml {

	class _AddDefinitionVisitor : public DefinitionVisitor {

	public:
		_AddDefinitionVisitor(MetaDictionary &md, EventHandler *ev = &NULL_EVENTHANDLER) : meta_dictionary(md), event_handler(ev) {};

		bool _visit(Definition* def) {

			const AUID defid = MetaDictionary::createNormalizedAUID(def->identification);

			if (meta_dictionary.getDefinition(defid) != NULL) {

				if (event_handler) event_handler->error("Duplicate definition AUID: ", def->identification.to_string(), "");

				return false;

			}

			if (meta_dictionary.getDefinition(def->symbol) != NULL) {

				if (event_handler) event_handler->error("Duplicate definition Symbol: ", def->symbol, "");

				return false;

			}

			meta_dictionary.definitionsByAUID[defid] = def;
			meta_dictionary.definitionsBySymbol[def->symbol] = def;
			meta_dictionary.definitions.push_back(def);

			return true;
		}

		virtual void visit(const ClassDefinition &def) {
			/* Copy and add */

			Definition *def_copy = new ClassDefinition(def);

			if (_visit(def_copy)) {

				/* add to member index */

				if (def.parentClass.is_valid()) {

					const AUID parentauid = MetaDictionary::createNormalizedAUID(def.parentClass.get());

					meta_dictionary.subclassesOf[parentauid].insert(def.identification);
				}

			} else {

				delete def_copy;

			}
		}

		virtual void visit(const PropertyDefinition &def) {

			/* Copy and add */

			Definition *def_copy = new PropertyDefinition(def);

			if (_visit(def_copy)) {

				/* add to member index */

				const AUID parentauid = MetaDictionary::createNormalizedAUID(def.memberOf);

				meta_dictionary.membersOf[parentauid].insert(def.identification);

			} else {

				delete def_copy;

			}


		}


		virtual void visit(const PropertyAliasDefinition & def) {
			const AUID parentauid = MetaDictionary::createNormalizedAUID(def.memberOf);

			meta_dictionary.membersOf[parentauid].insert(def.identification);
		}

		virtual void visit(const EnumerationTypeDefinition &def) {
			Definition *def_copy = new EnumerationTypeDefinition(def);

			if (! _visit(def_copy)) delete def_copy;
		}

		virtual void visit(const CharacterTypeDefinition &def) {
			Definition *def_copy = new CharacterTypeDefinition(def);

			if (!_visit(def_copy)) delete def_copy;
		}

		virtual void visit(const RenameTypeDefinition & def) {
			Definition *def_copy = new RenameTypeDefinition(def);

			if (!_visit(def_copy)) delete def_copy;
		}

		virtual void visit(const RecordTypeDefinition & def) {
			Definition *def_copy = new RecordTypeDefinition(def);

			if (!_visit(def_copy)) delete def_copy;
		}

		virtual void visit(const StringTypeDefinition & def) {
			Definition *def_copy = new StringTypeDefinition(def);

			if (!_visit(def_copy)) delete def_copy;
		}

		virtual void visit(const LensSerialFloatTypeDefinition & def) {
			Definition *def_copy = new LensSerialFloatTypeDefinition(def);

			if (!_visit(def_copy)) delete def_copy;
		}

		virtual void visit(const IntegerTypeDefinition & def) {
			Definition *def_copy = new IntegerTypeDefinition(def);

			if (!_visit(def_copy)) delete def_copy;
		}

		virtual void visit(const StrongReferenceTypeDefinition & def) {
			Definition *def_copy = new StrongReferenceTypeDefinition(def);

			if (!_visit(def_copy)) delete def_copy;
		}

		virtual void visit(const WeakReferenceTypeDefinition & def) {
			Definition *def_copy = new WeakReferenceTypeDefinition(def);

			if (!_visit(def_copy)) delete def_copy;
		}

		virtual void visit(const ExtendibleEnumerationTypeDefinition & def) {
			Definition *def_copy = new ExtendibleEnumerationTypeDefinition(def);

			if (!_visit(def_copy)) delete def_copy;
		}

		virtual void visit(const VariableArrayTypeDefinition & def) {
			Definition *def_copy = new VariableArrayTypeDefinition(def);

			_visit(def_copy);
		}

		virtual void visit(const FixedArrayTypeDefinition & def) {
			Definition *def_copy = new FixedArrayTypeDefinition(def);

			if (!_visit(def_copy)) delete def_copy;
		}

		virtual void visit(const OpaqueTypeDefinition & def) {
			Definition *def_copy = new OpaqueTypeDefinition(def);

			if (!_visit(def_copy)) delete def_copy;
		}

		virtual void visit(const IndirectTypeDefinition & def) {
			Definition *def_copy = new IndirectTypeDefinition(def);

			if (!_visit(def_copy)) delete def_copy;
		}

		virtual void visit(const StreamTypeDefinition & def) {
			Definition *def_copy = new StreamTypeDefinition(def);

			if (!_visit(def_copy)) delete def_copy;
		}

		virtual void visit(const SetTypeDefinition & def) {
			Definition *def_copy = new SetTypeDefinition(def);

			if (!_visit(def_copy)) delete def_copy;
		}

		virtual void visit(const FloatTypeDefinition& def) {
			Definition* def_copy = new FloatTypeDefinition(def);

			if (!_visit(def_copy)) delete def_copy;
		}

	private:
		MetaDictionary &meta_dictionary;
		EventHandler *event_handler;

	};


	MetaDictionary::MetaDictionary(const AUID & pSchemeID, const std::string & pSchemeURI) : schemeID(pSchemeID), schemeURI(pSchemeURI) {
	}

	MetaDictionary::MetaDictionary() {}

	MetaDictionary::~MetaDictionary() {

		for (std::vector<Definition*>::iterator it = definitions.begin(); it != definitions.end(); ++it) {
			delete *it;
		}

	}

	const Definition * MetaDictionary::getDefinition(const AUID & identification) const {

		std::map<AUID, Definition*>::const_iterator it = definitionsByAUID.find(createNormalizedAUID(identification));

		if (it == definitionsByAUID.end()) return NULL;

		return it->second;
	}

	const Definition *  MetaDictionary::getDefinition(const std::string & symbol) const
	{
		if (definitionsBySymbol.find(symbol) == definitionsBySymbol.end()) return NULL;

		return definitionsBySymbol.at(symbol);
	}

    const std::vector<Definition*>& MetaDictionary::getDefinitions() const {
		return this->definitions;
    }

	std::set<AUID> MetaDictionary::getSubclassesOf(const AUID & identification) const {

		const AUID norm_auid = MetaDictionary::createNormalizedAUID(identification);

		if (subclassesOf.find(norm_auid) == subclassesOf.end()) return std::set<AUID>();

		return subclassesOf.at(norm_auid);
	}

	std::set<AUID> MetaDictionary::getMembersOf(const AUID & identification) const {

		const AUID norm_auid = MetaDictionary::createNormalizedAUID(identification);

		if (membersOf.find(norm_auid) == membersOf.end()) return std::set<AUID>();

		return membersOf.at(norm_auid);
	}

	void MetaDictionary::setSchemeID(const AUID & pSchemeID) {
		this->schemeID = pSchemeID;
	}

	const std::string & MetaDictionary::getSchemeURI() const { return this->schemeURI; }

	void MetaDictionary::setSchemeURI(const std::string & pSchemeURI) {
		this->schemeURI = pSchemeURI;
	}

	void MetaDictionary::setDescription(const std::string & desc) {
		this->description = desc;
	}

	void MetaDictionary::addDefinition(const Definition & def, EventHandler * ev)
	{

		_AddDefinitionVisitor ad(*this);

		def.accept(ad);

	}

	UL MetaDictionary::createNormalizedUL(const UL & ul) {
		UL norm_ul = ul;

		/* set version to 0 */

		norm_ul.setValueOctet(7, 0);

		if (norm_ul.isGroup()) {

			/* set byte 6 to 0x7f */
			norm_ul.setValueOctet(5, 0x7f);

		}

		return norm_ul;
	}

	AUID MetaDictionary::createNormalizedAUID(const AUID & auid) {

		if (auid.isUL()) {
			return createNormalizedUL(auid.asUL());
		} else {
			return auid;
		}

	}

	bool MetaDictionary::normalizedAUIDEquals(const AUID & auidA, const AUID & auidB) {

		return createNormalizedAUID(auidA).equals(createNormalizedAUID(auidB));
	}

}