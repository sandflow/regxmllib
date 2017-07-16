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
 
#ifndef COM_SANDFLOW_SMPTE_MXF_PARTITIONPACK_H
#define COM_SANDFLOW_SMPTE_MXF_PARTITIONPACK_H

#include <com/sandflow/smpte/klv/Triplet.h>
#include <com/sandflow/smpte/klv/LocalTagRegister.h>
#include <com/sandflow/smpte/util/AUID.h>
#include <vector>
#include "MXFException.h"
#include "com/sandflow/util/membuf.h"
#include "com/sandflow/smpte/mxf/MXFStream.h"

namespace rxml {

	class ULAdapter {
	public:
		static UL fromValue(unsigned char* value, size_t len) {
			return UL(value);
		};
	};

	class PartitionPack {

	public:

		static const UL KEY;

		enum Kind { HEADER, BODY, FOOTER };
		enum Status { OPEN_INCOMPLETE, CLOSED_INCOMPLETE, OPEN_COMPLETE, CLOSED_COMPLETE };

		unsigned short majorVersion;
		unsigned short minorVersion;
		unsigned long kagSize;
		unsigned long long thisPartition;
		unsigned long long previousPartition;
		unsigned long long footerPartition;
		unsigned long long headerByteCount;
		unsigned long long indexByteCount;
		unsigned long indexSID;
		unsigned long long bodyOffset;
		unsigned long bodySID;
		AUID operationalPattern;
		std::vector<UL> essenceContainers;
		Kind kind;
		Status status;

		void fromTriplet(const Triplet &t);

		static bool isPartitionPack(const UL &key);

		static bool isPartitionPack(const AUID &key);

	};

}

#endif