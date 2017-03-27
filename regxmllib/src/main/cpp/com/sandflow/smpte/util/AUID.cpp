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

#include "AUID.h"
#include "UUID.h"
#include <stdexcept>

static const std::string UUID_URN_PREFIX = "urn:uuid:";



bool AUID::urnToBytes(const std::string & urn, unsigned char(&bytes)[16])
{
	unsigned char auid[16];

	if (UL::urnToBytes(urn, bytes)) return true;

	if (UUID::urnToBytes(urn, auid)) {
		memcpy(bytes, auid + 8, 8);
		memcpy(bytes + 8, auid, 8);
		return true;
	}

	return false;
}

AUID::AUID(const std::string & urn) {

	if (!urnToBytes(urn, this->value)) {
		throw std::invalid_argument("Neither a UL nor a UUID");
	}

}


AUID::AUID(const char *urn) {
	std::string urn_str(urn);

	if (!urnToBytes(urn_str, this->value)) {
		throw std::invalid_argument("Neither a UL nor a UUID");
	}
}


AUID::AUID() {
	std::fill(this->value, this->value + 16, 0);
}

/**
* Instantiates a AUID from a 16-byte buffer
* @param auid 16-bytes
*/

AUID::AUID(const unsigned char auid[16]) {

	std::copy(auid, auid + 16, this->value);

}

std::string AUID::to_string() const {

	if (isUL()) {

		return this->asUL().to_string();

	} else {

		return this->asUUID().to_string();

	}

}


UL AUID::asUL() const {

	if (!isUL()) {
		throw new std::invalid_argument("AUID is not a UL");
	}

	return UL(this->value);
}

UUID AUID::asUUID() const  {

	if (!isUUID()) {
		throw new std::invalid_argument("AUID is not a UUID");
	}

	unsigned char tmp[16];

	memcpy(tmp + 8, this->value, 8);

	memcpy(tmp, this->value + 8, 8);

	return UUID(tmp);
}

bool AUID::equals(const AUID &other) const {

	if (this == &other) return true;

	return std::memcmp(this->value, other.value, 16) == 0;

}