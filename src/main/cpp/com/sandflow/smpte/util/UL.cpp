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

#include "UL.h"
#include <sstream>  
#include <iomanip>
#include <cstdio>
#include <stdexcept>

namespace rxml {

	static bool lowerCmp(char i, char j) {
		return (::towlower(i) == ::towlower(j));
	}

	bool UL::urnToBytes(const std::string &urn, unsigned char(&ul)[16]) {

		static const std::string UL_URN_PREFIX = "urn:smpte:ul:";

		if (urn.size() != 48) return false;

		if (std::equal(urn.begin(),
			urn.begin() + UL_URN_PREFIX.size(),
			UL_URN_PREFIX.begin(), lowerCmp) != true) return false;

		/* process as UL (urn:smpte:ul:xxxxxxxx.xxxxxxxx.xxxxxxxx.xxxxxxxx) */

		for (int i = 0; i < 4; i++) {

			for (int j = 0; j < 4; j++) {

				ul[4 * i + j] = (unsigned char)strtol(urn.substr(13 + i * 9 + 2 * j, 2).c_str(), NULL, 16);

			}
		}

		return true;

	}

	std::string UL::bytesToString(const unsigned char ul[16]) {

		char str[128];

		int r = snprintf(str, sizeof(str), "urn:smpte:ul:%02x%02x%02x%02x.%02x%02x%02x%02x.%02x%02x%02x%02x.%02x%02x%02x%02x",
			ul[0], ul[1], ul[2], ul[3],
			ul[4], ul[5], ul[6], ul[7],
			ul[8], ul[9], ul[10], ul[11],
			ul[12], ul[13], ul[14], ul[15]
		);

		if (r < 0) throw std::runtime_error("UL::bytesToString failed.");

		return str;

	}

	UL::UL(const std::string & urn)
	{
		if (UL::urnToBytes(urn, value)) return;

		throw std::invalid_argument("Neither a UL nor a UUID");

	}

	UL::UL(const unsigned char ul[16]) {
		std::memcpy(this->value, ul, 16);
	}

	UL::UL(const char * urn) {
		std::string urn_str(urn);

		if (!urnToBytes(urn_str, this->value)) {
			throw std::invalid_argument("Not a UL");
		}
	}

	UL::UL() {
		std::memset(this->value, 0, 16);
	}

	UL::UL(const UL & other) {
		*this = other;
	}

	bool UL::equals(const UL &other, unsigned int bytemask) const {

		if (this == &other) return true;

		for (int i = 0; i < 16; i++) {
			if ((bytemask & 0x8000) != 0 && this->value[i] != other.value[i]) {
				return false;
			}

			bytemask = bytemask << 1;
		}

		return true;
	}

	/**
	* @return The value of the Registry Designator byte of the UL
	*/
	unsigned char UL::getRegistryDesignator() const {
		return value[REGISTRY_DESIGNATOR_BYTE];
	}

	/**
	* @return true if the UL is a Key for a KLV Local Set (see SMPTE ST 336)
	*/
	bool UL::isLocalSet() const {
		return isGroup() && (getRegistryDesignator() & 7) == 3;
	}

	unsigned char UL::getCategoryDesignator() const
	{
		return value[CATEGORY_DESIGNATOR_BYTE];
	}




	std::string UL::to_string() const {

		return bytesToString(this->value);

	}


	bool UL::isGroup() const {
		return value[CATEGORY_DESIGNATOR_BYTE] == 2;
	}

		UL UL::makeNormalized() const {
			UL norm_ul(*this);

			/* set version to 0 */

			norm_ul.setValueOctet(7, 0);

			if (norm_ul.isGroup()) {

				/* set byte 6 to 0x7f */
				norm_ul.setValueOctet(5, 0x7f);

			}

			return norm_ul;
		}

}