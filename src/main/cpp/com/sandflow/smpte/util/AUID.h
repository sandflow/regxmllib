/*
 * Copyright (c) 2017, Pierre-Anthony Lemieux (pal@sandflow.com)
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

#ifndef COM_SANDFLOW_SMPTE_UTIL_AUID_H
#define COM_SANDFLOW_SMPTE_UTIL_AUID_H

#include <string>
#include <exception>
#include <algorithm>
#include "UL.h"
#include "UUID.h"
#include <cstring>

namespace rxml {


	/**
	 * AUID as specified in SMPTE ST 377-1
	 */
	class AUID {



	public:

		unsigned char value[16];



		static bool urnToBytes(const std::string &urn, unsigned char(&ul)[16]);

		/**
		 * Creates a AUID from a UL or UUID URN.
		 * @param urn URN from which to create the AUID
		 * @throws std::invalid_argument
		 */
		AUID(const std::string &urn);

		/**
		* Creates a AUID from a UL or UUID URN.
		* @param urn URN from which to create the AUID
		* @throws std::invalid_argument
		*/
		AUID(const char *urn);

		AUID();

		/**
		 * Instantiates a AUID from a 16-byte buffer
		 * @param auid 16-bytes
		 */
		AUID(const unsigned char auid[16]);


		/**
		 * Instantiates a AUID from a UL
		 * @param ul UL from which to create the AUID
		 */
		AUID(const UL &ul) {
			*this = ul;
		}

		/**
		 * Instantiates a AUID from a UUID
		 * @param uuid UUID from which to create the AUID
		 */
		AUID(const UUID &uuid) {
			*this = uuid;
		}

		/**
		* Returns the UL underlying the AUID
		* @throws std::invalid_argument if the AUID is not a UL
		*/
		UL asUL() const;

		/**
		* Returns the UUID underlying the AUID
		* @throws std::invalid_argument if the AUID is not a UUID
		*/
		UUID asUUID() const;


		bool equals(const AUID &auid) const;

		std::string to_string() const;

		/**
		 * Is the AUID a UL?
		 * @return true if the AUID is a UL
		 */
		bool isUL() const {
			return (value[0] & 0x80) == 0;
		}

		/**
		 * Is the AUID a UUID?
		 * @return true if the AUID is a UUID
		 */
		bool isUUID() const {
			return !isUL();
		}

		AUID& operator=(const AUID & src) {

			if (&src != this) {
				std::copy(src.value, src.value + 16, this->value);
			}

			return *this;
		}

		AUID& operator=(const UL & src) {

			memcpy(this->value, src.value, 16);

			return *this;
		}

		AUID& operator=(const UUID & src) {

			memcpy(this->value, src.value + 8, 8);
			memcpy(this->value + 8, src.value, 8);

			return *this;
		}

		AUID& operator=(const unsigned char src[]) {
			std::copy(src, src + 16, this->value);

			return *this;
		}

		bool operator<(const AUID& other) const {

			return std::memcmp(this->value, other.value, 16) < 0;

		}

		/**
			* @return Normalized copy of the AUID
			*/
		AUID makeNormalized() const;

	};

}
#endif