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

#include "PrimerPack.h"

namespace rxml {

	void PrimerPack::fromTriplet(const Triplet & t) {
		localtags.clear();

		membuf mb((char*)t.getValue(), t.getLength());

		MXFInputStream kis(&mb);

		try {

			unsigned long itemcount = kis.readUnsignedLong();

			kis.readUnsignedLong(); // item length

			for (unsigned long i = 0; i < itemcount; i++) {

				unsigned short tag = kis.readUnsignedShort();
				AUID auid = kis.readUL();

				localtags[tag] = auid;
			}

		} catch (std::exception e) {
			std::cout << e.what();
		}
	}

	const AUID * PrimerPack::getIdentification(unsigned long local_tag) const {
		std::map<unsigned long, AUID>::const_iterator it = localtags.find(local_tag);

		if (it == localtags.end()) {
			return NULL;
		} else {
			return &(it->second);
		}

	}

	const UL PrimerPack::KEY = "urn:smpte:ul:060e2b34.02050101.0d010201.01050100";


	bool PrimerPack::isPrimerPack(const UL & key) {


		return KEY.equals(key, UL::IGNORE_VERSION);
	}

	bool PrimerPack::isPrimerPack(const AUID & key) {

		return key.isUL() && KEY.equals(key.asUL(), UL::IGNORE_VERSION);
	}

}
