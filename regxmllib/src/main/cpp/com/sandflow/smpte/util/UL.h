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
 
#ifndef COM_SANDFLOW_SMPTE_UTIL_UL_H
#define COM_SANDFLOW_SMPTE_UTIL_UL_H

#include <string>
#include <algorithm>
#include <cstring>


class UL {

public:

	/*
	 * Members
	 */

	unsigned char value[16];

	/*
	* Methods
	*/

	static bool urnToBytes(const std::string &urn, unsigned char(&ul)[16]);

	static std::string bytesToString(const unsigned char ul[16]);

	/*
	 * @throws std::invalid_argument
	 */

	UL(const std::string &urn);

	UL(const unsigned char ul[16]);

	UL(const char *urn);

	UL();

	UL(const UL &other);


	enum ByteMasks {
		IGNORE_VERSION = 0xFEFF,
		IGNORE_GROUP_CODING = 0xFBFF
	};

	bool equals(const UL &ul, unsigned int bytemask = 0xFFFF) const;

	UL& operator=(const UL & src) {

		if (&src != this) {
			std::copy(src.value, src.value + 16, this->value);
		}

		return *this;
	}

	UL& operator=(const unsigned char src[]) {
		std::copy(src, src + 16, this->value);

		return *this;
	}

	bool operator<(const UL& other) const {

		return std::memcmp(this->value, other.value, 16) < 0;

	}


	/* TODO: define an interface? */

	std::string to_string() const;

	enum DesignatorByteValue {
		CATEGORY_DESIGNATOR_BYTE = 4,
		REGISTRY_DESIGNATOR_BYTE = 5,
		VERSION_BYTE = 7
	};

	unsigned char getValueOctet(int i) const {
		return value[i];
	}

	void setValueOctet(int i, unsigned char v) {
		this->value[i] = v;
	}

	/**
	* @return true if the UL is a Key for a KLV Group (see SMPTE ST 336)
	*/
	bool isGroup() const;

	/**
	* @return true if the UL is a Key for a KLV Local Set (see SMPTE ST 336)
	*/
	bool isLocalSet() const;

	/**
	* @return The value of the Category Designator byte of the UL
	*/
	unsigned char getCategoryDesignator() const;

	/**
	* @return The value of the Registry Designator byte of the UL
	*/
	unsigned char getRegistryDesignator() const;

	unsigned char getVersion() const {
		return value[VERSION_BYTE];
	}

};

#endif