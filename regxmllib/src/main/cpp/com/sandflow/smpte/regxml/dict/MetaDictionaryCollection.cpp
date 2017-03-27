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

#include "MetaDictionaryCollection.h"

const Definition * MetaDictionaryCollection::getDefinition(const AUID & auid) const {

	for (std::map<std::string, MetaDictionary*>::const_iterator it = dicts.begin(); it != dicts.end(); it++) {
		const Definition *def = it->second->getDefinition(auid);

		if (def != NULL) {
			return def;
		}
	}

	return NULL;
}

/**
* Retrieves a definition from the collection based on its symbol
* @param namespace Namespace of the definition
* @param symbol Symbol of the definition
* @return Definition, or null if none found
*/

const Definition * MetaDictionaryCollection::getDefinition(std::string ns, const std::string & symbol) const {

	std::map<std::string, MetaDictionary*>::const_iterator it = dicts.find(ns);

	if (it != dicts.end()) {
		return it->second->getDefinition(symbol);
	}

	return NULL;
}

/**
* Adds a MetaDictionary to the collection.
*
* @param metadictionary MetaDictionary to be added
*/

void MetaDictionaryCollection::addDictionary(MetaDictionary * metadictionary) {

	std::map<std::string, MetaDictionary*>::const_iterator it = dicts.find(metadictionary->getSchemeURI());

	if (it == dicts.end()) {
		dicts[metadictionary->getSchemeURI()] = metadictionary;
	}

}

const std::map<std::string, MetaDictionary*>& MetaDictionaryCollection::getDictionatries() const
{
	return this->dicts;
}


/**
* Determines whether a meta dictionary with the specified namespace exists
*
* @param namespace Namespace sought
* @return true if namespace is covered
*/

bool MetaDictionaryCollection::hasNamespace(const std::string & ns) const {
	return dicts.find(ns) != dicts.end();
}

std::set<AUID> MetaDictionaryCollection::getSubclassesOf(const AUID & identification) const {

	std::set<AUID> subclasses;

	for (std::map<std::string, MetaDictionary*>::const_iterator it = dicts.begin(); it != dicts.end(); it++) {

		std::set<AUID> defs = it->second->getSubclassesOf(identification);

		subclasses.insert(defs.begin(), defs.end());
	}

	return subclasses;
}

std::set<AUID> MetaDictionaryCollection::getMembersOf(const AUID & identification) const {
	std::set<AUID> members;

	for (std::map<std::string, MetaDictionary*>::const_iterator it = dicts.begin(); it != dicts.end(); it++) {

		std::set<AUID> defs = it->second->getMembersOf(identification);

		members.insert(defs.begin(), defs.end());
	}

	return members;
}
