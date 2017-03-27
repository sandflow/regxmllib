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
 
#ifndef COM_SANDFLOW_SMPTE_REGXML_MXFFRAGMENTBUILDER_H
#define COM_SANDFLOW_SMPTE_REGXML_MXFFRAGMENTBUILDER_H

#include <string>
#include <com/sandflow/smpte/util/AUID.h>
#include <com/sandflow/smpte/regxml/dict/DefinitionResolver.h>
#include <com/sandflow/smpte/regxml/FragmentBuilder.h>
#include <xercesc/dom/DOM.hpp>
#include <iostream>
#include <com/sandflow/util/events/EventHandler.h>
#include <com/sandflow/util/events/NullEventHandler.h>

XERCES_CPP_NAMESPACE_USE


class MXFFragmentBuilder {

public:

	/**
	* Returns a DOM Document Fragment containing a RegXML Fragment rooted at
	* the first Header Metadata object with a class that descends from the
	* specified class.
	*
	* @param mxfpartition MXF partition, including the Partition Pack.
	* @param defresolver MetaDictionary definitions.
	* @param enumnameresolver Allows the local name of extendible enumeration
	* values to be inserted as comments. May be null.
	* @param evthandler Calls back the caller when an event occurs. Must not be null.
	* @param rootclasskey Root class of Fragment. The Preface class is used if null.
	* @param document DOM for which the Document Fragment is created. Must not be null.
	*
	* @return Document Fragment containing a single RegXML Fragment
	*/
	static DOMDocumentFragment* fromInputStream(
		std::istream &mxfpartition,
		const DefinitionResolver &defresolver,
		const FragmentBuilder::AUIDNameResolver *enumnameresolver,
		const AUID *rootclasskey,
		DOMDocument &document,
		EventHandler *ev = &NULL_EVENTHANDLER);

};

#endif