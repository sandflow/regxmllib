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

#include "PartitionPack.h"
#include "MXFException.h"
#include "com/sandflow/util/strformat.h"

void PartitionPack::fromTriplet(const Triplet & t) {

	if (!t.getKey().isUL()) {

		throw new MXFException("Triplet key " + strf::to_string(t.getKey()) + " is not a UL");
	}

	UL ul = t.getKey().asUL();

	switch (ul.getValueOctet(14)) {

	case 0x01:
		status = OPEN_INCOMPLETE;
		break;
	case 0x02:
		status = CLOSED_INCOMPLETE;
		break;
	case 0x03:
		status = OPEN_COMPLETE;
		break;
	case 0x04:
		status = CLOSED_COMPLETE;
		break;
	default:
		throw MXFException("Illegal Partition Pack");
	}

	switch (ul.getValueOctet(13)) {
	case 0x02:
		kind = HEADER;

		break;
	case 0x03:
		kind = BODY;

		break;
	case 0x04:
		kind = FOOTER;

		if (this->status == OPEN_COMPLETE
			|| this->status == OPEN_INCOMPLETE) {
			throw MXFException("Open Footer Partition");
		}

		break;
	default:
		throw MXFException("Illegal Partition Pack");
	}

	/* TODO : rename MXFInputStream */

	membuf mb((char*)t.getValue(), t.getLength());

	MXFInputStream kis(&mb);

	try {

		this->majorVersion = kis.readUnsignedShort();

		this->minorVersion = kis.readUnsignedShort();

		this->kagSize = kis.readUnsignedLong();

		this->thisPartition = kis.readUnsignedLongLong();

		this->previousPartition = kis.readUnsignedLongLong();

		this->footerPartition = kis.readUnsignedLongLong();

		this->headerByteCount = kis.readUnsignedLongLong();

		this->indexByteCount = kis.readUnsignedLongLong();

		this->indexSID = kis.readUnsignedLong();

		this->bodyOffset = kis.readUnsignedLongLong();

		this->bodySID = kis.readUnsignedLong();

		this->operationalPattern = kis.readUL();

		this->essenceContainers = kis.readBatch<ULAdapter, UL>();

	} catch (std::exception e) {
		throw new MXFException("Error reading partition pack");
	}

}

const UL PartitionPack::KEY = "urn:smpte:ul:060e2b34.02050101.0d010201.01010000";


bool PartitionPack::isPartitionPack(const UL & key) {


	return KEY.equals(key, 0xfef9);
}

bool PartitionPack::isPartitionPack(const AUID & key) {

	return key.isUL() && KEY.equals(key.asUL(), 0xfef9);
}