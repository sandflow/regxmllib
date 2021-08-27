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
 
#ifndef COM_SANDFLOW_SMPTE_REGXML_DICT_METADICTIONARY
#define COM_SANDFLOW_SMPTE_REGXML_DICT_METADICTIONARY

#include <string>
#include <vector>
#include <map>
#include <set>
#include <com/sandflow/smpte/regxml/definitions/Definition.h>
#include <com/sandflow/smpte/regxml/definitions/DefinitionVisitor.h>
#include <com/sandflow/smpte/util/AUID.h>
#include <com/sandflow/smpte/util/UUID.h>
#include <com/sandflow/util/events/EventHandler.h>
#include <com/sandflow/util/events/NullEventHandler.h>
#include "DefinitionResolver.h"


namespace rxml {

	class MetaDictionary : public DefinitionResolver {

	public:

		MetaDictionary(const AUID &pSchemeID, const std::string &pSchemeURI);

		MetaDictionary();

		~MetaDictionary();

		virtual const Definition* getDefinition(const AUID &identification) const;

		const Definition* getDefinition(const std::string &symbol) const;

		const std::vector<const Definition*>& getDefinitions() const;

		virtual std::set<AUID> getSubclassesOf(const AUID &identification) const;

		virtual std::set<AUID> getMembersOf(const AUID &identification) const;

		void setSchemeID(const AUID &pSchemeID);

		const std::string& getSchemeURI() const;;
		void setSchemeURI(const std::string &pSchemeURI);

		void setDescription(const std::string &desc);

		void addDefinition(const Definition &def, EventHandler *ev = &NULL_EVENTHANDLER);

		static UL createNormalizedUL(const UL &ul);

		static AUID createNormalizedAUID(const AUID &auid);

		static bool normalizedAUIDEquals(const AUID &auidA, const AUID &auidB);


	private:

		/* members */

		AUID schemeID;

		std::string schemeURI;

		std::string description;

		std::vector<const Definition*> definitions;

		std::map<AUID, const Definition*> definitionsByAUID;

		std::map<std::string, const Definition*> definitionsBySymbol;

		std::map<AUID, std::set<AUID> > membersOf;

		std::map<AUID, std::set<AUID> > subclassesOf;

		/* friends */

		friend class _AddDefinitionVisitor;
	};

}
#endif