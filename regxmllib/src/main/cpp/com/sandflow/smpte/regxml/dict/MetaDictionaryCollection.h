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
 
#ifndef COM_SANDFLOW_SMPTE_REGXML_DICT_METADICTIONARYCOLLECTION
#define COM_SANDFLOW_SMPTE_REGXML_DICT_METADICTIONARYCOLLECTION

#include "MetaDictionary.h"

namespace rxml {

	/**
	* A collection of multiple RegXML Metadictionary as specified in SMPTE ST 2001-1
	*/
	class MetaDictionaryCollection : public DefinitionResolver {

	private:

		std::map<std::string, MetaDictionary*> dicts;

	public:

		virtual const Definition* getDefinition(const AUID& auid) const;

		/**
		 * Retrieves a definition from the collection based on its symbol
		 * @param namespace Namespace of the definition
		 * @param symbol Symbol of the definition
		 * @return Definition, or null if none found
		 */
		const Definition* getDefinition(std::string ns, const std::string& symbol) const;

		/**
		 * Adds a MetaDictionary to the collection.
		 *
		 * @param metadictionary MetaDictionary to be added
		 */
		void addDictionary(MetaDictionary *metadictionary);

		const std::map<std::string, MetaDictionary*> &getDictionatries() const;

		/**
		 * Determines whether a meta dictionary with the specified namespace exists
		 *
		 * @param namespace Namespace sought
		 * @return true if namespace is covered
		 */
		bool hasNamespace(const std::string &ns) const;


		std::set<AUID> getSubclassesOf(const AUID &identification) const;

		std::set<AUID> getMembersOf(const AUID &identification) const;

	};
}

#endif