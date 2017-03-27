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

#include "LocalSet.h"
#include "com/sandflow/util/membuf.h"
#include "com/sandflow/util/CountingStreamBuf.h"
#include "com/sandflow/smpte/klv/KLVStream.h"
#include "KLVException.h"
#include "com/sandflow/util/strformat.h"

LocalSet::LocalSet() {}

LocalSet::LocalSet(const Triplet & t, const LocalTagRegister & reg) {
	fromTriplet(t, reg);
}

LocalSet::~LocalSet()
{
	for (std::vector<Triplet*>::iterator it = items.begin(); it != items.end(); ++it) {
		delete *it;
	}
}

const UL& LocalSet::getKey() const {
	return key;
}

const std::vector<Triplet*>& LocalSet::getItems() const {
	return items;
}




void LocalSet::fromTriplet(const Triplet& t, const LocalTagRegister &reg) {

	if (!t.getKey().isUL()) {

		throw new KLVException("Triplet key " + strf::to_string(t.getKey()) + " is not a UL");
	}

	UL ul = t.getKey().asUL();

	if (!ul.isLocalSet()) {

		throw new KLVException("Triplet with key " + strf::to_string(t.getKey()) + " is not a Local Set");
	}

	membuf mb((char*) t.getValue(), (char*) t.getValue() + t.getLength());

	CountingStreamBuf csb(&mb);

	KLVStream kis(&csb);

	this->key = ul;
	this->items.clear();

	while (csb.getCount() < t.getLength() && kis.good()) {

		unsigned long localtag = 0;

		/* read local tag */
		switch (this->key.getRegistryDesignator() >> 3 & 3) {

			/* 1 byte length field */
		case 0:
			localtag = kis.readUnsignedByte();
			break;

			/* ASN.1 OID BER length field */
		case 1:
			localtag = kis.readBERLength();
			break;

			/* 2 byte length field */
		case 2:
			localtag = kis.readUnsignedShort();
			break;

			/* 4 byte length field */
		case 3:
			localtag = kis.readUnsignedLong();
			break;
		}

		long locallen = 0;

		/* read local length */
		switch (this->key.getRegistryDesignator() >> 5 & 3) {

			/* ASN.1 OID BER length field */
		case 0:
			locallen = kis.readBERLength();
			break;

			/* 1 byte length field */
		case 1:
			locallen = kis.readUnsignedByte();
			break;

			/* 2 byte length field */
		case 2:
			locallen = kis.readUnsignedShort();
			break;

			/* 4 byte length field */
		case 3:
			locallen = kis.readUnsignedLong();
			break;
		}

		/* does the localtag resolve */

		const AUID *auid = reg.getIdentification(localtag);

		if (!auid) {

			throw KLVException("No UL found for Local Tag " + strf::to_string(localtag) + " at Triplet with Key " + strf::to_string(t.getKey()));

		}

		/* TODO: make sure localen is not larger then max triplet len*/

		items.push_back(new MemoryTriplet(*auid, locallen, kis));

	}


}

bool LocalSet::isLocalSet(const Triplet & t)
{
	return t.getKey().isUL() && t.getKey().asUL().isLocalSet();
}
