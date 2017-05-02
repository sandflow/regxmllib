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

#include "MXFFragmentBuilder.h"
#include "com/sandflow/smpte/klv/MemoryTriplet.h"
#include "com/sandflow/smpte/klv/LocalSet.h"
#include "com/sandflow/smpte/mxf/PartitionPack.h"
#include "com/sandflow/smpte/mxf/FillItem.h"
#include "com/sandflow/smpte/mxf/PrimerPack.h"
#include "com/sandflow/util/scoped_ptr.h"
#include "com/sandflow/util/CountingStreamBuf.h"
#include "com/sandflow/smpte/klv/Group.h"
#include "com/sandflow/smpte/mxf/Set.h"
#include "com/sandflow/smpte/klv/KLVException.h"
#include "com/sandflow/smpte/regxml/dict/MetaDictionary.h"

bool _findPartitionPack(std::istream &is, PartitionPack& pp) {
	MemoryTriplet t;

	do {

		t.fromStream(is);

		if (is.good() && PartitionPack::isPartitionPack(t.getKey())) {

			pp.fromTriplet(t);

			return true;

		}

	} while (is.good());

	return false;
}

bool _findPrimerPack(std::istream &is, PrimerPack& pp) {
	MemoryTriplet t;

	do {

		t.fromStream(is);

		if (is.good() && !FillItem::isFillItem(t.getKey()) && PrimerPack::isPrimerPack(t.getKey())) {

			pp.fromTriplet(t);

			return true;

		}

	} while (is.good());

	return false;
}

bool _findPrefaceSet(const std::map<UUID, Set> &setresolver, UUID &rootid) {

	static const UL PREFACE_KEY = "urn:smpte:ul:060e2b34.027f0101.0d010101.01012f00";

	for (std::map<UUID, Set>::const_iterator it = setresolver.begin(); it != setresolver.end(); it++) {

		if (PREFACE_KEY.equals(it->second.getKey(), UL::IGNORE_GROUP_CODING & UL::IGNORE_VERSION)) {

			rootid = it->second.getInstanceID();

			return true;

		}

	}

	return false;

}

bool _findFirstInstanceOfClass(const std::map<UUID, Set> &setresolver, const DefinitionResolver &defresolver, const AUID& superclass, UUID &objectid) {


	for (std::map<UUID, Set>::const_iterator it = setresolver.begin(); it != setresolver.end(); it++) {

		/* go up the class hierarchy */

		AUID classauid = it->second.getKey();

		while (true) {

			const Definition *def = defresolver.getDefinition(classauid);

			/* skip if not a class instance */

			if (def == NULL || !instance_of<ClassDefinition>(*def)) {
				break;
			}


			if (MetaDictionary::normalizedAUIDEquals(def->identification, superclass)) {

				objectid = it->second.getInstanceID();

				return true;

			} else if (((const ClassDefinition*)def)->parentClass.is_valid()) {

				/* get parent class and continue */

				classauid = ((const ClassDefinition*)def)->parentClass.get();

			} else {

				/* reached the top of the hierarchy */

				break;

			}
		}

	}

	return false;

}

DOMDocumentFragment* MXFFragmentBuilder::fromInputStream(
	std::istream &mxfpartition,
	const DefinitionResolver &defresolver,
	const FragmentBuilder::AUIDNameResolver *enumnameresolver,
	const AUID *rootclasskey,
	DOMDocument &document,
	EventHandler *ev) {

	static const UL INDEX_TABLE_SEGMENT_UL = "urn:smpte:ul:060e2b34.02530101.0d010201.01100100";


	CountingStreamBuf csb(mxfpartition.rdbuf());

	std::istream cis(&csb);

	/* look for the partition pack */

	PartitionPack pp;

	if (!_findPartitionPack(cis, pp)) {

		/* ERROR ABORT */

	}


	/* start counting header metadata bytes */

	csb.resetCount();

	/* look for the primer pack */


	PrimerPack primer;

	if (!_findPrimerPack(cis, primer)) {

		/* ERROR ABORT */

	}

	/* capture all local sets within the header metadata */

	std::map<UUID, Set> setresolver;

	while (csb.getCount() < pp.headerByteCount) {

		MemoryTriplet t(cis);

		if (!t.getKey().isUL()) {

			/* skip all non MXF groups */

			continue;

		} else if (INDEX_TABLE_SEGMENT_UL.equals(t.getKey().asUL(), UL::IGNORE_VERSION)) {

			/* stop if Index Table reached */

			/*MXFEvent evt = new MXFEvent(
				EventCodes.UNEXPECTED_STRUCTURE,
				"Index Table Segment encountered before Header Byte Count bytes read"
			);

			handleEvent(evthandler, evt);*/

			break;

		} else if (FillItem::isFillItem(t.getKey())) {

			/* skip fill items */
			continue;

		} else if (!LocalSet::isLocalSet(t)) {

			/* TODO: error, not a local set */

			continue;
		}

		try {

			LocalSet ls(t, primer);

			if (!Set::hasInstanceUID(ls)) {

				/* TODO: ERROR not a MXF Set */

				continue;

			}

			Set set(ls);

			if (setresolver.find(set.getInstanceID()) != setresolver.end()) {

				/* TODO: ERROR duplicate Set in header metadata */

				continue;

			}

			setresolver[set.getInstanceID()] = set;

		} catch (KLVException ke) {

			/* TODO: report error */

		}

	}

	UUID rootid;

	if (rootclasskey == NULL) {

		if (!_findPrefaceSet(setresolver, rootid)) {

			/* TODO: ERROR no preface set found */

			return NULL;

		}

	} else {

		if (!_findFirstInstanceOfClass(setresolver, defresolver, *rootclasskey, rootid)) {

			/* TODO: ERROR no preface set found */

			return NULL;

		}

	}

	FragmentBuilder fb(defresolver, setresolver, NULL, ev);

	return fb.fromTriplet(setresolver[rootid], document);
}