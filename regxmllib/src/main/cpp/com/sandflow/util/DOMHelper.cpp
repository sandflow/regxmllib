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

#include "DOMHelper.h"

namespace rxml {


	xercesc::DOMElement * DOMHelper::getElementByTagNameNS(xercesc::DOMElement * parent, const XMLCh * namespaceURI, const XMLCh * localName) {

		xercesc::DOMNode *child = parent->getFirstElementChild();

		while (child) {

			if (child->getNodeType() == xercesc::DOMNode::ELEMENT_NODE &&
				xercesc::XMLString::compareIString(child->getNodeName(), localName) == 0 &&
				xercesc::XMLString::compareIString(child->getNamespaceURI(), namespaceURI) == 0) {

				return (xercesc::DOMElement*)child;

			}

			child = child->getNextSibling();

		}

		return NULL;
	}

	const XMLCh * DOMHelper::getElementTextContentByTagNameNS(xercesc::DOMElement * parent, const XMLCh * namespaceURI, const XMLCh * localName) {
		xercesc::DOMElement* e = getElementByTagNameNS(parent, namespaceURI, localName);

		if (e) {

			return e->getTextContent();

		} else {

			return NULL;

		}

	}

}