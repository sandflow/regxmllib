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
 
#ifndef COM_SANDFLOW_SMPTE_MXF_SET_H
#define COM_SANDFLOW_SMPTE_MXF_SET_H

#include <com/sandflow/smpte/util/UL.h>
#include <com/sandflow/smpte/util/UUID.h>
#include <com/sandflow/smpte/klv/Triplet.h>
#include <com/sandflow/smpte/klv/Group.h>
#include "MXFException.h"
#include <vector>

class Set : public Group {

public:

	static const UL INSTANCE_UID_ITEM_UL;

	Set(const Group &g);

	Set();

	virtual ~Set();

	virtual Set & operator=(const Set &src);

	virtual const UL& getKey() const;

	virtual const std::vector<Triplet*>& getItems() const;

	void fromGroup(const Group &g);

	static bool hasInstanceUID(const Group &g);

	const UUID& getInstanceID() const;



private:

	std::vector<Triplet*> items;
	UL key;
	UUID instanceID;

	void clearItems();

};


#endif
