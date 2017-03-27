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
 
#ifndef COM_SANDFLOW_UTIL_DOMHELPER_H
#define COM_SANDFLOW_UTIL_DOMHELPER_H

#include <xercesc/util/TransService.hpp>
#include <xercesc/dom/DOM.hpp>
#include <string>

XERCES_CPP_NAMESPACE_USE


namespace DOMHelper {

	class XMLChStr : public TranscodeFromStr {

	public:

		XMLChStr(const char *str) : TranscodeFromStr((XMLByte*)str, strlen(str), "utf-8"), src(str) {}

		const std::string& ostr() const { return this->src; }

		const char* c_str() const { return this->src.c_str(); }

	private:

		std::string src;

	};

	class fromUTF8 : public TranscodeFromStr {

	public:

		fromUTF8(const char *str) : TranscodeFromStr((XMLByte*)str, strlen(str), "utf-8"), src(str) {}
		fromUTF8(const std::string &str) : TranscodeFromStr((XMLByte*)str.c_str(), str.size(), "utf-8"), src(str) {}

		const std::string& ostr() const { return this->src; }

		const char* c_str() const { return this->src.c_str(); }

		operator const XMLCh*() const { return this->str(); }

	private:

		const std::string src;

	};

	class toUTF8 : public TranscodeToStr {

	public:

		toUTF8(const XMLCh *str) : TranscodeToStr(str, "utf-8"), src(str) {}

		const XMLCh* ostr() const { return this->src; }

		const char* c_str() const { return (const char*) this->str(); }

		operator const char*() const { return (char*) this->str(); }

	private:

		const XMLCh * src;

	};


	DOMElement* getElementByTagNameNS(DOMElement *parent, const XMLCh *namespaceURI, const XMLCh *localName);


	const XMLCh* getElementTextContentByTagNameNS(DOMElement *parent, const XMLCh *namespaceURI, const XMLCh *localName);

}

#endif