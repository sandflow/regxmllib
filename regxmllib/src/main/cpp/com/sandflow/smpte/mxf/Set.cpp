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

#include "com/sandflow/smpte/mxf/Set.h"
#include "Set.h"
#include "com/sandflow/smpte/klv/MemoryTriplet.h"

namespace rxml {

	const UL Set::INSTANCE_UID_ITEM_UL = "urn:smpte:ul:060e2b34.01010101.01011502.00000000";

	const UL & Set::getKey() const
	{
		return this->key;
	}

	const std::vector<Triplet*>& Set::getItems() const
	{
		return this->items;
	}

	void Set::fromGroup(const Group & g) {

		const std::vector<Triplet*>& src_items = g.getItems();

		if (!hasInstanceUID(g)) {

			throw new MXFException("Group is missing an instance ID property");

		}

		for (std::vector<Triplet*>::const_iterator it = src_items.begin(); it < src_items.end(); it++) {

			if ((*it)->getKey().isUL() && INSTANCE_UID_ITEM_UL.equals((*it)->getKey().asUL(), UL::IGNORE_VERSION)) {

				this->instanceID = (*it)->getValue();

			}

			MemoryTriplet *t = new MemoryTriplet(**it);

			items.push_back(t);

		}

		this->key = g.getKey();


	}

	bool Set::hasInstanceUID(const Group & g)
	{
		const std::vector<Triplet*>& src_items = g.getItems();

		for (std::vector<Triplet*>::const_iterator it = src_items.begin(); it < src_items.end(); it++) {

			if ((*it)->getKey().isUL() && INSTANCE_UID_ITEM_UL.equals((*it)->getKey().asUL(), UL::IGNORE_VERSION)) {

				return true;

			}

		}

		return false;
	}

	const UUID& Set::getInstanceID() const {
		return this->instanceID;
	}

	Set::Set(const Group & g) {
		fromGroup(g);
	}

	Set::~Set() {
		clearItems();
	}

	Set::Set() {}

	Set & Set::operator=(const Set &src) {

		this->instanceID = src.instanceID;
		this->key = src.key;

		clearItems();

		for (std::vector<Triplet*>::size_type i = 0; i < src.items.size(); i++) {

		}

		for (std::vector<Triplet*>::const_iterator it = src.items.begin(); it != src.items.end(); ++it) {
			this->items.push_back(new MemoryTriplet(**it));
		}

		return *this;
	}

	void Set::clearItems() {
		for (std::vector<Triplet*>::iterator it = this->items.begin(); it != this->items.end(); ++it) {
			delete *it;
		}

		this->items.clear();
	}
}