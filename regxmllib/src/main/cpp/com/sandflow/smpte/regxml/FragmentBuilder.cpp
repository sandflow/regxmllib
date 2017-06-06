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

#include "FragmentBuilder.h"
#include "com/sandflow/smpte/util/IDAU.h"

const UL FragmentBuilder::INSTANCE_UID_ITEM_UL = "urn:smpte:ul:060e2b34.01010101.01011502.00000000";
const UL FragmentBuilder::AUID_UL = "urn:smpte:ul:060e2b34.01040101.01030100.00000000";
const UL FragmentBuilder::UUID_UL = "urn:smpte:ul:060e2b34.01040101.01030300.00000000";
const UL FragmentBuilder::DateStruct_UL = "urn:smpte:ul:060e2b34.01040101.03010500.00000000";
const UL FragmentBuilder::PackageID_UL = "urn:smpte:ul:060e2b34.01040101.01030200.00000000";
const UL FragmentBuilder::Rational_UL = "urn:smpte:ul:060e2b34.01040101.03010100.00000000";
const UL FragmentBuilder::TimeStruct_UL = "urn:smpte:ul:060e2b34.01040101.03010600.00000000";
const UL FragmentBuilder::TimeStamp_UL = "urn:smpte:ul:060e2b34.01040101.03010700.00000000";
const UL FragmentBuilder::VersionType_UL = "urn:smpte:ul:060e2b34.01040101.03010300.00000000";
const UL FragmentBuilder::ByteOrder_UL = "urn:smpte:ul:060e2b34.01010101.03010201.02000000";
const UL FragmentBuilder::Character_UL = "urn:smpte:ul:060e2b34.01040101.01100100.00000000";
const UL FragmentBuilder::Char_UL = "urn:smpte:ul:060e2b34.01040101.01100300.00000000";
const UL FragmentBuilder::UTF8Character_UL = "urn:smpte:ul:060e2b34.01040101.01100500.00000000";
const UL FragmentBuilder::ProductReleaseType_UL = "urn:smpte:ul:060e2b34.01040101.02010101.00000000";
const UL FragmentBuilder::Boolean_UL = "urn:smpte:ul:060e2b34.01040101.01040100.00000000";
const UL FragmentBuilder::PrimaryPackage_UL = "urn:smpte:ul:060e2b34.01010104.06010104.01080000";
const UL FragmentBuilder::LinkedGenerationID_UL = "urn:smpte:ul:060e2b34.01010102.05200701.08000000";
const UL FragmentBuilder::GenerationID_UL = "urn:smpte:ul:060e2b34.01010102.05200701.01000000";
const UL FragmentBuilder::ApplicationProductID_UL = "urn:smpte:ul:060e2b34.01010102.05200701.07000000";

const std::string FragmentBuilder::BYTEORDER_BE = "BigEndian";
const std::string FragmentBuilder::BYTEORDER_LE = "LittleEndian";


const std::string FragmentBuilder::REGXML_NS = "http://sandflow.com/ns/SMPTEST2001-1/baseline";
const std::string FragmentBuilder::XMLNS_NS = "http://www.w3.org/2000/xmlns/";
const std::string FragmentBuilder::ACTUALTYPE_ATTR = "actualType";
const std::string FragmentBuilder::UID_ATTR = "uid";

const Definition * FragmentBuilder::findBaseTypeDefinition(const Definition * definition, const DefinitionResolver & defresolver) {

	while (instance_of<RenameTypeDefinition>(*definition)) {
		definition = defresolver.getDefinition(((RenameTypeDefinition*)definition)->renamedType);
	}

	return definition;
}

/**
* Creates a RegXML Fragment, represented an XML DOM Document Fragment
*
* @param group KLV Group for which the Fragment will be generated.
* @param document Document from which the XML DOM Document Fragment will be
* created.
*
* @return XML DOM Document Fragment containing a single RegXML Fragment
*
* @throws KLVException
* @throws com.sandflow.smpte.regxml.FragmentBuilder.RuleException
*/

const Definition * FragmentBuilder::findBaseDefinition(const Definition * definition) {

	while (instance_of<RenameTypeDefinition>(*definition)) {
		definition = defresolver.getDefinition(((RenameTypeDefinition*)definition)->renamedType);
	}

	return definition;
}


/**
* Instantiates a FragmentBuilder. If the anamresolver argument is not null,
* the FragmentBuilder will attempt to resolve the name of each AUID it
* writes to the output and add it as an XML comment. If the evthandler
* argument is not null, the Fragment builder will call back the caller with
* events it encounters as it transforms a Triplet.
*
* @param defresolver Maps Group Keys to MetaDictionary definitions. Must
* not be null;
* @param setresolver Resolves Strong References to groups. Must not be
* null.
* @param anameresolver Resolves a AUID to a human-readable symbol. May be
* null.
* @param evthandler Calls back the caller when an event occurs. May be
* null.
*/

FragmentBuilder::FragmentBuilder(const DefinitionResolver & defresolver, const std::map<UUID, Set>& setresolver, const AUIDNameResolver * anameresolver, EventHandler * evthandler) : defresolver(defresolver), setresolver(setresolver), anameresolver(anameresolver), evthandler(evthandler) {
}

DOMDocumentFragment * FragmentBuilder::fromTriplet(const Group & group, DOMDocument & document) {

	DOMDocumentFragment *df = NULL;

	try {

		nsprefixes.clear();

		df = document.createDocumentFragment();

		applyRule3(df, group);

		/* NOTE: Hack to clean-up namespace prefixes */
		for (std::map<std::string, std::string>::const_iterator it = this->nsprefixes.begin(); it != this->nsprefixes.end(); it++) {

			((DOMElement*)df->getFirstChild())->setAttributeNS(DOMHelper::fromUTF8(XMLNS_NS), DOMHelper::fromUTF8("xmlns:" + it->second), DOMHelper::fromUTF8(it->first));
		}



	} catch (const std::exception &e) {

		UncaughtExceptionError err(
			e,
			strf::fmt(
				"Group key {}",
				group.getKey().to_string()
			)
		);

		evthandler->fatal(err);

	} catch (...) {

		UncaughtExceptionError err(
			strf::fmt(
				"Group key {}",
				group.getKey().to_string()
			)
		);

		evthandler->fatal(err);

	}

	return df;

}

std::string FragmentBuilder::getElementNSPrefix(const std::string & ns) {
	std::map<std::string, std::string>::const_iterator it = this->nsprefixes.find(ns);

	if (it == this->nsprefixes.end()) {

		std::string prefix = "r" + strf::to_string(this->nsprefixes.size());

		return this->nsprefixes[ns] = prefix;

	} else {

		return it->second;

	}
}


void FragmentBuilder::addInformativeComment(DOMElement *element, std::string comment) {
	element->appendChild(element->getOwnerDocument()->createComment(DOMHelper::fromUTF8(comment)));
}

void FragmentBuilder::appendCommentWithAUIDName(AUID auid, DOMElement* elem) {
	if (this->anameresolver != NULL) {

		const std::string *ename = this->anameresolver->getLocalName(auid);

		if (ename != NULL) {
			elem->appendChild(elem->getOwnerDocument()->createComment(DOMHelper::fromUTF8(*ename)));
		}

	}
}

void FragmentBuilder::applyRule3(DOMNode * node, const Group & group) {

	const Definition *definition = defresolver.getDefinition(group.getKey());

	if (definition == NULL) {

		evthandler->info(UnknownGroupError(group.getKey(), ""));

		return;
	}

	if (definition->identification.isUL() &&
		definition->identification.asUL().getVersion() != group.getKey().getVersion()) {



		evthandler->info(
			VersionByteMismatchError(
				group.getKey(),
				definition->identification.asUL(),
				strf::fmt(
					"Group UL {} in file does not have the same version byte as in the register",
					group.getKey().to_string()
				)
			)
		);

	}

	/* is the definition for a Class */

	if (!instance_of<ClassDefinition>(*definition)) {

		UnexpectedDefinitionError err(group.getKey(), "Class Definition", group.getKey().to_string());

		evthandler->error(err);

		return;

	}

	/* create the element */

	DOMElement *objelem = node->getOwnerDocument()->createElementNS(
		DOMHelper::fromUTF8(definition->ns),
		DOMHelper::fromUTF8(definition->symbol)
	);

	objelem->setPrefix(DOMHelper::fromUTF8(getElementNSPrefix(definition->ns)));

	node->appendChild(objelem);

	/* process the properties of the group */

	for (std::vector<Triplet*>::const_iterator item = group.getItems().begin(); item != group.getItems().end(); item++) {

		/* skip if the property is not defined in the registers */
		const Definition *itemdef = defresolver.getDefinition((*item)->getKey());

		if (itemdef == NULL) {

			UnknownPropertyError err((*item)->getKey(), "Group " + group.getKey().to_string());

			evthandler->info(err);

			addInformativeComment(
				objelem,
				strf::fmt(
					"Unknown property\nKey: {}\nData: {}",
					(*item)->getKey().to_string(),
					strf::bytesToString((*item)->getValue(), (*item)->getLength())
				)
			);

			continue;

		}

		/* make sure this is a property definition */
		if (!instance_of<PropertyDefinition>(*itemdef) && !instance_of<PropertyAliasDefinition>(*itemdef)) {

			UnexpectedDefinitionError err(itemdef->identification, "Property", "Group " + group.getKey().to_string());

			evthandler->warn(err);

			addInformativeComment(objelem, err.getReason());

			continue;
		}

		/* warn if version byte of the property does not match the register version byte  */
		if (itemdef->identification.isUL() && itemdef->identification.asUL().getVersion() != (*item)->getKey().asUL().getVersion()) {

			VersionByteMismatchError err((*item)->getKey(), itemdef->identification, "Group " + group.getKey().to_string());

			evthandler->info(err);

		}

		DOMElement *elem = node->getOwnerDocument()->createElementNS(
			DOMHelper::fromUTF8(itemdef->ns),
			DOMHelper::fromUTF8(itemdef->symbol)
		);

		objelem->appendChild(elem);

		elem->setPrefix(DOMHelper::fromUTF8(getElementNSPrefix(itemdef->ns)));

		/* write the property */

		membuf mb((char*)(*item)->getValue(), (*item)->getLength());

		MXFInputStream mis(&mb);

		applyRule4(elem, mis, (const PropertyDefinition*)itemdef);

		/* detect cyclic references  */
		if ((*item)->getKey().isUL() && INSTANCE_UID_ITEM_UL.equals((*item)->getKey().asUL(), UL::IGNORE_VERSION)) {

			const XMLCh* iidns = objelem->getLastChild()->getNamespaceURI();
			const XMLCh* iidname = objelem->getLastChild()->getLocalName();
			const XMLCh* iid = objelem->getLastChild()->getTextContent();

			/* look for identical instanceID in parent elements */
			DOMNode *parent = node;

			while (parent->getNodeType() == DOMNode::ELEMENT_NODE) {

				for (DOMNode *n = parent->getFirstChild(); n != NULL; n = n->getNextSibling()) {

					if (n->getNodeType() == DOMNode::ELEMENT_NODE
						&& XMLString::compareIString(iidname, n->getLocalName()) == 0
						&& XMLString::compareIString(iidns, n->getNamespaceURI()) == 0
						&& XMLString::compareIString(iid, n->getTextContent()) == 0) {

						CircularStrongReferenceError err(std::string(DOMHelper::toUTF8(iid)), "Group " + definition->symbol);

						evthandler->info(err);

						addInformativeComment((DOMElement*)n, err.getReason());

						return;
					}
				}

				parent = parent->getParentNode();
			}

		}

		/* add reg:uid if property is a unique ID */
		if (((PropertyDefinition*)itemdef)->uniqueIdentifier.is_valid() && ((PropertyDefinition*)itemdef)->uniqueIdentifier.get()) {

			DOMAttr *attr = node->getOwnerDocument()->createAttributeNS(
				DOMHelper::fromUTF8(REGXML_NS),
				DOMHelper::fromUTF8(UID_ATTR)
			);

			attr->setPrefix(DOMHelper::fromUTF8(getElementNSPrefix(REGXML_NS)));
			attr->setTextContent(elem->getTextContent());

			objelem->setAttributeNodeNS(attr);
		}

	}


}

void FragmentBuilder::applyRule4(DOMElement * element, MXFInputStream & value, const PropertyDefinition * propdef) {

	try {

		if (propdef->identification.equals(ByteOrder_UL)) {

			int byteorder;

			byteorder = value.readUnsignedShort();

			/* ISSUE: ST 2001-1 inverses these constants */
			if (byteorder == 0x4D4D) {

				element->setTextContent(DOMHelper::fromUTF8(BYTEORDER_BE));

			} else if (byteorder == 0x4949) {

				element->setTextContent(DOMHelper::fromUTF8(BYTEORDER_LE));

				UnexpectedByteOrderError err(ByteOrder_UL.to_string());

				evthandler->error(err);

				addInformativeComment(element, err.getReason());

			} else {
				throw new FragmentBuilder::UnknownByteOrderError(ByteOrder_UL.to_string());
			}

		} else {

			/*if (propdef instanceof PropertyAliasDefinition) {
			propdef = defresolver.getDefinition(((PropertyAliasDefinition)propdef).getOriginalProperty());
			}*/

			const Definition *tdef = findBaseTypeDefinition(defresolver.getDefinition(propdef->type), this->defresolver);

			/* return if no type definition is found */
			if (tdef == NULL) {

				throw UnknownTypeError(
					propdef->type,
					strf::fmt(
						"Property {} at Element {}",
						propdef->symbol,
						DOMHelper::toUTF8(element->getLocalName()).c_str()
					)
				);


			}

			if (propdef->identification.equals(PrimaryPackage_UL)) {

				/* EXCEPTION: PrimaryPackage is encoded as the Instance UUID of the target set
				but needs to be the UMID contained in the unique ID of the target set */
				UUID uuid = value.readUUID();

				/* is this a local reference through Instance ID? */

				std::map<UUID, Set>::const_iterator iset = setresolver.find(uuid);

				if (iset != setresolver.end()) {

					bool foundUniqueID = false;

					const std::vector<Triplet*> &items = iset->second.getItems();

					/* find the unique identifier in the group */
					for (std::vector<Triplet*>::const_iterator item = items.begin(); item != items.end(); item++) {

						const Definition *itemdef = defresolver.getDefinition((*item)->getKey());

						if (itemdef == NULL) {
							continue;
						}

						if (!(instance_of<PropertyDefinition>(*itemdef) || instance_of<PropertyAliasDefinition>(*itemdef))) {
							continue;
						}

						if (((PropertyDefinition*)itemdef)->uniqueIdentifier.is_valid() && ((PropertyDefinition*)itemdef)->uniqueIdentifier.get()) {

							membuf mb((char*)(*item)->getValue(), (*item)->getLength());

							MXFInputStream mis(&mb);

							applyRule4(element, mis, (PropertyDefinition*)itemdef);

							foundUniqueID = true;

							break;

						}

					}

					if (foundUniqueID != true) {

						throw MissingUniquePropertyError(
							PrimaryPackage_UL,
							strf::fmt(
								"Element {}",
								DOMHelper::toUTF8(element->getLocalName()).c_str()
							)
						);

					}

				} else {

					throw MissingPrimaryPackageError(
						uuid,
						strf::fmt(
							"Property {} at Element {}",
							propdef->symbol,
							DOMHelper::toUTF8(element->getLocalName()).c_str()
						)
					);


				}

			} else {

				if (propdef->identification.equals(LinkedGenerationID_UL)
					|| propdef->identification.equals(GenerationID_UL)
					|| propdef->identification.equals(ApplicationProductID_UL)) {

					/* EXCEPTION: LinkedGenerationID, GenerationID and ApplicationProductID
					are encoded using UUID */
					tdef = defresolver.getDefinition(UUID_UL);
				}

				applyRule5(element, value, tdef);
			}
		}

	} catch (const std::ios_base::failure &e) {

		IOError err(
			e,
			strf::fmt(
				"Property {} at Element {}",
				propdef->symbol,
				DOMHelper::toUTF8(element->getLocalName()).c_str()
			));


		evthandler->error(err);

		addInformativeComment(element, err.getReason());

	} catch (const Event &e) {

		evthandler->error(e);

		addInformativeComment(element, e.getReason());

	} catch (const std::exception &e) {

		UncaughtExceptionError err(
			e,
			strf::fmt(
				"Property {} at Element {}",
				propdef->symbol,
				DOMHelper::toUTF8(element->getLocalName()).c_str()
			)
		);

		evthandler->error(err);

		addInformativeComment(element, err.getReason());

	} catch (...) {

		UncaughtExceptionError err(
			strf::fmt(
				"Property {} at Element {}",
				propdef->symbol,
				DOMHelper::toUTF8(element->getLocalName()).c_str()
			)
		);

		evthandler->error(err);

		addInformativeComment(element, err.getReason());

	}

}

void FragmentBuilder::applyRule5(DOMElement * element, MXFInputStream & value, const Definition * definition) {

	try {

		if (instance_of<CharacterTypeDefinition>(*definition)) {

			applyRule5_1(element, value, (CharacterTypeDefinition*)definition);

		} else if (instance_of<EnumerationTypeDefinition>(*definition)) {

			applyRule5_2(element, value, (EnumerationTypeDefinition*)definition);

		} else if (instance_of<ExtendibleEnumerationTypeDefinition>(*definition)) {

			applyRule5_3(element, value, (ExtendibleEnumerationTypeDefinition*)definition);

		} else if (instance_of<FixedArrayTypeDefinition>(*definition)) {

			applyRule5_4(element, value, (FixedArrayTypeDefinition*)definition);

		} else if (instance_of<IndirectTypeDefinition>(*definition)) {

			applyRule5_5(element, value, (IndirectTypeDefinition*)definition);

		} else if (instance_of<IntegerTypeDefinition>(*definition)) {

			applyRule5_6(element, value, (IntegerTypeDefinition*)definition);

		} else if (instance_of<OpaqueTypeDefinition>(*definition)) {

			applyRule5_7(element, value, (OpaqueTypeDefinition*)definition);

		} else if (instance_of<RecordTypeDefinition>(*definition)) {

			applyRule5_8(element, value, (RecordTypeDefinition*)definition);

		} else if (instance_of<RenameTypeDefinition>(*definition)) {

			applyRule5_9(element, value, (RenameTypeDefinition*)definition);

		} else if (instance_of<SetTypeDefinition>(*definition)) {

			applyRule5_10(element, value, (SetTypeDefinition*)definition);

		} else if (instance_of<StreamTypeDefinition>(*definition)) {

			applyRule5_11(element, value, (StreamTypeDefinition*)definition);

		} else if (instance_of<StringTypeDefinition>(*definition)) {

			applyRule5_12(element, value, (StringTypeDefinition*)definition);

		} else if (instance_of<StrongReferenceTypeDefinition>(*definition)) {

			applyRule5_13(element, value, (StrongReferenceTypeDefinition*)definition);

		} else if (instance_of<VariableArrayTypeDefinition>(*definition)) {

			applyRule5_14(element, value, (VariableArrayTypeDefinition*)definition);

		} else if (instance_of<WeakReferenceTypeDefinition>(*definition)) {

			applyRule5_15(element, value, (WeakReferenceTypeDefinition*)definition);

			/*} else if (instance_of<FloatTypeDefinition>(*definition)) {

			applyRule5_alpha(element, value, (FloatTypeDefinition*)definition);*/

		} else if (instance_of<LensSerialFloatTypeDefinition>(*definition)) {

			applyRule5_beta(element, value, (LensSerialFloatTypeDefinition*)definition);

		} else {

			throw FragmentBuilder::Exception(
				strf::fmt(
					"Unknown Kind for Definition {} in Rule 5.",
					definition->symbol
				)
			);

		}

	} catch (const std::ios_base::failure &e) {

		IOError err(
			e,
			strf::fmt(
				"Definition {} at Element {}",
				definition->symbol,
				DOMHelper::toUTF8(element->getLocalName()).c_str()
			));


		evthandler->error(err);

		addInformativeComment(element, err.getReason());

	} catch (const Event &e) {

		evthandler->error(e);

		addInformativeComment(element, e.getReason());

	}

}

void FragmentBuilder::readCharacters(DOMElement * element, MXFInputStream & value, const CharacterTypeDefinition * definition, bool isSingleChar) {

	std::vector<char> sb;

	static const size_t chars_sz = 32;
	char chars[chars_sz];

	while (value.good()) {

		value.read(chars, chars_sz);

		sb.insert(sb.end(), chars, chars + value.gcount());

	}

	/* return if there is not text to add */

	if (sb.size() == 0) return;

	/* choose the character decoder */

	std::string codec;

	if (definition->identification.equals(Character_UL)) {

		if (value.getByteOrder() == MXFInputStream::BIG_ENDIAN_BYTE_ORDER) {

			codec = "UTF-16BE";

		} else {

			codec = "UTF-16LE";

		}

	} else if (definition->identification.equals(Char_UL)) {

		codec = "US-ASCII";

	} else if (definition->identification.equals(UTF8Character_UL)) {

		/* NOTE: Use of UTF-8 character encoding is specified in RP 2057 */

		codec = "UTF-8";

	} else {

		throw UnsupportedCharTypeError(definition->symbol,
			strf::fmt(
				"Element {}",
				DOMHelper::toUTF8(element->getLocalName()).c_str()
			)
		);

		/*evthandler->error(err);

		addInformativeComment(element, err.getReason());


		return;*/

	}

	TranscodeFromStr xmlstr((XMLByte*)sb.data(), sb.size(), codec.c_str());

	/* return if there is not text to add */

	if (xmlstr.length() == 0) return;

	element->setTextContent(xmlstr.str());

}

void FragmentBuilder::applyRule5_1(DOMElement * element, MXFInputStream & value, const CharacterTypeDefinition * definition) {

	readCharacters(element, value, definition, false /* do not remove trailing zeroes for a single char */);

}

void FragmentBuilder::applyRule5_2(DOMElement * element, MXFInputStream & value, const EnumerationTypeDefinition * definition) {

	const Definition *bdef = findBaseDefinition(defresolver.getDefinition(definition->elementType));

	if (!instance_of<IntegerTypeDefinition>(*bdef)) {

		throw UnsupportedEnumTypeError(definition->symbol,
			strf::fmt(
				"Enum {} at Element {}",
				definition->symbol,
				DOMHelper::toUTF8(element->getLocalName()).c_str()
			)
		);

		/*evthandler->error(err);

		addInformativeComment(element, err.getReason());

		return;*/
	}

	IntegerTypeDefinition* idef = (IntegerTypeDefinition*)bdef;

	if (idef->isSigned) {
		throw FragmentBuilder::Exception("Cannot handle signed Enumeration Definitions");
	}

	int len = 0;

	if (definition->identification.equals(ProductReleaseType_UL)) {

		/* EXCEPTION: ProductReleaseType_UL is listed as
		a UInt8 enum but encoded as a UInt16 */
		len = 2;

	} else {

		len = idef->size;

	}

	unsigned long bi = 0;


	switch (len) {

	case 1:
		bi = value.readUnsignedByte();
		break;

	case 2:
		bi = value.readUnsignedShort();
		break;

	case 4:
		bi = value.readUnsignedLong();
		break;

	default:
		throw FragmentBuilder::Exception("Enumerations Definitions wider than 4 bytes are not supported");

	}


	std::string str;

	if (definition->elementType.equals(Boolean_UL)) {

		/* find the "true" enum element */
		/* MXF can encode "true" as any value other than 0 */

		for (std::vector<EnumerationTypeDefinition::Element>::const_iterator it = definition->elements.begin();
			it != definition->elements.end();
			it++) {

			if ((bi == 0 && it->value == 0) || (bi != 0 && it->value == 1)) {
				str = it->name;
			}
		}

	} else {

		for (std::vector<EnumerationTypeDefinition::Element>::const_iterator it = definition->elements.begin();
			it != definition->elements.end();
			it++) {

			if (it->value == bi) {
				str = it->name;
			}
		}

	}

	if (str.size() == 0) {

		str = "UNDEFINED";

		UnknownEnumValueError err(bi,
			strf::fmt(
				"Enum {} at Element {}",
				definition->symbol,
				DOMHelper::toUTF8(element->getLocalName()).c_str()
			)
		);

		evthandler->error(err);

		addInformativeComment(element, err.getReason());

	}

	element->setTextContent(DOMHelper::fromUTF8(str));


}

void FragmentBuilder::applyRule5_3(DOMElement * element, MXFInputStream & value, const ExtendibleEnumerationTypeDefinition * definition) {

	UL ul = value.readUL();

	/* NOTE: ST 2001-1 XML Schema does not allow ULs as values for Extendible Enumerations, which
	defeats the purpose of the type. This issue could be addressed at the next revision opportunity. */
	element->setTextContent(DOMHelper::fromUTF8(ul.to_string()));

	this->appendCommentWithAUIDName(ul, element);

}

void FragmentBuilder::applyRule5_4(DOMElement * element, MXFInputStream & value, const FixedArrayTypeDefinition * definition) {

	if (definition->identification.equals(UUID_UL)) {

		UUID uuid = value.readUUID();

		element->setTextContent(DOMHelper::fromUTF8(uuid.to_string()));

	} else {

		const Definition* tdef = findBaseDefinition(defresolver.getDefinition(definition->elementType));

		applyCoreRule5_4(element, value, tdef, definition->elementCount);

	}
}

void FragmentBuilder::applyCoreRule5_4(DOMElement * element, MXFInputStream & value, const Definition * tdef, unsigned long elementcount) {

	for (unsigned long i = 0; i < elementcount; i++) {

		if (instance_of<StrongReferenceTypeDefinition>(*tdef)) {

			/* Rule 5.4.1 */
			applyRule5_13(element, value, (StrongReferenceTypeDefinition*)tdef);

		} else {

			/* Rule 5.4.2 */
			DOMElement *elem = element->getOwnerDocument()->createElementNS(
				DOMHelper::fromUTF8(tdef->ns),
				DOMHelper::fromUTF8(tdef->symbol)
			);

			elem->setPrefix(DOMHelper::fromUTF8(getElementNSPrefix(tdef->ns)));

			applyRule5(elem, value, tdef);

			element->appendChild(elem);

		}
	}
}

void FragmentBuilder::applyRule5_5(DOMElement * element, MXFInputStream & value, const IndirectTypeDefinition * definition) {

	/* see https://github.com/sandflow/regxmllib/issues/74 for a discussion on Indirect Type */
	KLVStream::ByteOrder bo;

	switch (value.readUnsignedByte()) {
	case 0x4c /* little endian */:
		bo = KLVStream::LITTLE_ENDIAN_BYTE_ORDER;
		break;
	case 0x42 /* big endian */:
		bo = KLVStream::BIG_ENDIAN_BYTE_ORDER;
		break;
	default:
		throw UnknownByteOrderError(
			strf::fmt(
				"Indirect Definition {} at Element {}",
				definition->symbol,
				DOMHelper::toUTF8(element->getLocalName()).c_str()
			)
		);
	}

	MXFInputStream orderedval(value.rdbuf(), bo);

	IDAU idau = orderedval.readIDAU();

	AUID auid = idau.asAUID();

	const Definition *def = defresolver.getDefinition(auid);

	if (def == NULL) {

		throw UnknownTypeError(
			auid.to_string(),
			strf::fmt(
				"Indirect Type {} at Element {}",
				definition->symbol,
				DOMHelper::toUTF8(element->getLocalName()).c_str()
			)
		);


	}

	// create reg:actualType attribute
	DOMAttr *attr = element->getOwnerDocument()->createAttributeNS(
		DOMHelper::fromUTF8(REGXML_NS),
		DOMHelper::fromUTF8(ACTUALTYPE_ATTR)
	);

	attr->setPrefix(DOMHelper::fromUTF8(getElementNSPrefix(REGXML_NS)));

	attr->setTextContent(DOMHelper::fromUTF8(def->symbol));
	element->setAttributeNodeNS(attr);

	applyRule5(element, orderedval, def);

}

void FragmentBuilder::applyRule5_6(DOMElement * element, MXFInputStream & value, const IntegerTypeDefinition * definition) {


	switch (definition->size) {
	case 1:

		if (definition->isSigned) {
			element->setTextContent(
				DOMHelper::fromUTF8(
					strf::to_string(value.readByte())
				)
			);
		} else {
			element->setTextContent(
				DOMHelper::fromUTF8(
					strf::to_string(value.readUnsignedByte())
				)
			);
		}

		break;
	case 2:
		if (definition->isSigned) {
			element->setTextContent(
				DOMHelper::fromUTF8(
					strf::to_string(value.readShort())
				)
			);
		} else {
			element->setTextContent(
				DOMHelper::fromUTF8(
					strf::to_string(value.readUnsignedShort())
				)
			);
		}
		break;
	case 4:
		if (definition->isSigned) {
			element->setTextContent(
				DOMHelper::fromUTF8(
					strf::to_string(value.readLong())
				)
			);
		} else {
			element->setTextContent(
				DOMHelper::fromUTF8(
					strf::to_string(value.readUnsignedLong())
				)
			);
		}
		break;
	case 8:
		if (definition->isSigned) {
			element->setTextContent(
				DOMHelper::fromUTF8(
					strf::to_string(value.readLongLong())
				)
			);
		} else {
			element->setTextContent(
				DOMHelper::fromUTF8(
					strf::to_string(value.readUnsignedLongLong())
				)
			);
		}
		break;
	default:
		throw FragmentBuilder::Exception("Unsupported Integer type");
	}

	/*if (value.gcount() == 0 || (!value.good())) {

		element->setTextContent(DOMHelper::fromUTF8("Nan"));

		FragmentEvent evt = new FragmentEvent(
		EventCodes.VALUE_LENGTH_MISMATCH,
		"No data",
		String.format(
		"Integer %s at Element %s",
		definition.getSymbol(),
		element.getLocalName()
		)
		);

		handleEvent(evt);

		addInformativeComment(element, evt.getReason());

	}*/


}

void FragmentBuilder::applyRule5_7(DOMElement * element, MXFInputStream & value, const OpaqueTypeDefinition * definition) {

	/* NOTE: Opaque Types are not used in MXF */
	throw FragmentBuilder::Exception("Opaque types are not supported.");

}

std::string FragmentBuilder::generateISO8601Time(int hour, int minutes, int seconds, int millis) {
	std::stringstream sb;

	sb << std::setw(2) << std::setfill('0') << hour << ":";
	sb << std::setw(2) << std::setfill('0') << minutes << ":";
	sb << std::setw(2) << std::setfill('0') << seconds;

	if (millis != 0) {

		sb << "." << std::setw(3) << std::setfill('0') << millis;
	}

	sb << "Z";

	return sb.str();
}

std::string FragmentBuilder::generateISO8601Date(int year, int month, int day) {

	std::stringstream sb;

	sb << std::setw(4) << std::setfill('0') << year << "-";

	sb << std::setw(2) << std::setfill('0') << month << "-";

	sb << std::setw(2) << std::setfill('0') << day;

	return sb.str();
}

void FragmentBuilder::applyRule5_8(DOMElement * element, MXFInputStream & value, const RecordTypeDefinition * definition) {

	if (definition->identification.equals(AUID_UL)) {

		AUID auid = value.readAUID();

		element->setTextContent(DOMHelper::fromUTF8(auid.to_string()));

		this->appendCommentWithAUIDName(auid, element);

	} else if (definition->identification.equals(DateStruct_UL)) {

		int year = value.readUnsignedShort();
		int month = value.readUnsignedByte();
		int day = value.readUnsignedByte();

		element->setTextContent(DOMHelper::fromUTF8(generateISO8601Date(year, month, day)));

	} else if (definition->identification.equals(PackageID_UL)) {

		UMID umid = value.readUMID();

		element->setTextContent(DOMHelper::fromUTF8(umid.to_string()));

	} else if (definition->identification.equals(Rational_UL)) {

		long numerator = value.readLong();
		long denominator = value.readLong();

		element->setTextContent(
			DOMHelper::fromUTF8(strf::fmt("{}/{}", strf::to_string(numerator), strf::to_string(denominator)))
		);

	} else if (definition->identification.equals(TimeStruct_UL)) {

		/*INFO: ST 2001-1 and ST 377-1 diverge on the meaning of 'fraction'.
		fraction is msec/4 according to 377-1 */
		int hour = value.readUnsignedByte();
		int minute = value.readUnsignedByte();
		int second = value.readUnsignedByte();
		int fraction = value.readUnsignedByte();

		element->setTextContent(DOMHelper::fromUTF8(generateISO8601Time(hour, minute, second, 4 * fraction)));

	} else if (definition->identification.equals(TimeStamp_UL)) {

		int year = value.readUnsignedShort();
		int month = value.readUnsignedByte();
		int day = value.readUnsignedByte();
		int hour = value.readUnsignedByte();
		int minute = value.readUnsignedByte();
		int second = value.readUnsignedByte();
		int fraction = value.readUnsignedByte();

		element->setTextContent(
			DOMHelper::fromUTF8(
				generateISO8601Date(year, month, day) + "T" +
				generateISO8601Time(hour, minute, second, 4 * fraction)
			)
		);

	} else if (definition->identification.equals(VersionType_UL)) {

		/* EXCEPTION: registers used Int8 but MXF specifies UInt8 */
		unsigned char major = value.readUnsignedByte();
		unsigned char minor = value.readUnsignedByte();

		element->setTextContent(
			DOMHelper::fromUTF8(strf::fmt("{}.{}", strf::to_string(major), strf::to_string(minor)))
		);

	} else {

		for (std::vector<RecordTypeDefinition::Member>::const_iterator it = definition->members.begin();
			it != definition->members.end();
			it++) {

			const Definition *itemdef = findBaseDefinition(defresolver.getDefinition(it->type));

			DOMElement *elem = element->getOwnerDocument()->createElementNS(
				DOMHelper::fromUTF8(definition->ns),
				DOMHelper::fromUTF8(it->name)
			);

			elem->setPrefix(DOMHelper::fromUTF8(getElementNSPrefix(definition->ns)));

			applyRule5(elem, value, itemdef);

			element->appendChild(elem);

		}

	}

}

void FragmentBuilder::applyRule5_9(DOMElement * element, MXFInputStream & value, const RenameTypeDefinition * definition) {

	const Definition *rdef = defresolver.getDefinition(definition->renamedType);

	applyRule5(element, value, rdef);

}

void FragmentBuilder::applyRule5_10(DOMElement * element, MXFInputStream & value, const SetTypeDefinition * definition) {

	const Definition* tdef = findBaseDefinition(defresolver.getDefinition(definition->elementType));

	unsigned long itemcount = value.readUnsignedLong();
	unsigned long itemlength = value.readUnsignedLong();

	applyCoreRule5_4(element, value, tdef, (unsigned long)itemcount);

}

void FragmentBuilder::applyRule5_11(DOMElement * element, MXFInputStream & value, const StreamTypeDefinition * definition) {

	throw FragmentBuilder::Exception("Rule 5.11 is not supported yet.");

}

void FragmentBuilder::applyRule5_12(DOMElement * element, MXFInputStream & value, const StringTypeDefinition * definition) {

	/* Rule 5.12 */
	const Definition *chrdef = findBaseDefinition(defresolver.getDefinition(definition->elementType));

	/* NOTE: ST 2001-1 implies that integer-based strings are supported, but
	does not described semantics.
	*/
	if (!instance_of<CharacterTypeDefinition>(*chrdef)) {

		throw UnsupportedStringTypeError(definition->symbol,
			strf::fmt(
				"Element {}",
				DOMHelper::toUTF8(element->getLocalName()).c_str()
			)
		);

		return;
	}

	readCharacters(
		element,
		value,
		(CharacterTypeDefinition*)chrdef,
		true /* remove trailing zeroes */
	);

}

void FragmentBuilder::applyRule5_13(DOMElement * element, MXFInputStream & value, const StrongReferenceTypeDefinition * definition) {

	const Definition *tdef = findBaseDefinition(defresolver.getDefinition(definition->referencedType));

	if (!instance_of<ClassDefinition>(*tdef)) {

		throw InvalidStrongReferenceTypeError(
			definition->symbol,
			strf::fmt(
				"Element {}",
				DOMHelper::toUTF8(element->getLocalName()).c_str()
			)
		);

	}

	UUID uuid = value.readUUID();

	std::map<UUID, Set>::const_iterator it = setresolver.find(uuid);

	if (it != setresolver.end()) {

		applyRule3(element, it->second);

	} else {

		throw MissingStrongReferenceError(
			uuid,
			strf::fmt(
				"Element {}",
				DOMHelper::toUTF8(element->getLocalName()).c_str()
			)
		);

	}

}

void FragmentBuilder::applyRule5_beta(DOMElement * element, MXFInputStream & value, const LensSerialFloatTypeDefinition * definition) {

	throw FragmentBuilder::Exception("Lens serial floats not supported.");

}

std::vector<const PropertyDefinition*> FragmentBuilder::getAllMembersOf(const ClassDefinition * cdef) {

	std::vector<const PropertyDefinition*> props;

	while (cdef) {

		std::set<AUID> members = defresolver.getMembersOf(cdef->identification);

		for (std::set<AUID>::const_iterator it = members.begin(); it != members.end(); it++) {
			props.push_back((const PropertyDefinition*)defresolver.getDefinition(*it));
		}

		if (cdef->parentClass.is_valid()) {
			cdef = (ClassDefinition*)defresolver.getDefinition(cdef->parentClass.get());
		} else {
			cdef = NULL;
		}

	}

	return props;
}

void FragmentBuilder::applyRule5_14(DOMElement * element, MXFInputStream & value, const VariableArrayTypeDefinition * definition) {

	const Definition *tdef = findBaseDefinition(defresolver.getDefinition(definition->elementType));

	if (definition->symbol == "DataValue") {

		/* RULE 5.14.2 */
		/* DataValue is string of octets, without number of elements or size of elements */

		std::stringstream ss;

		char c;

		while (value.get(c)) {
			ss << std::hex << std::setw(2) << std::setfill('0') << ((unsigned int)c & 0xFF);
		}

		element->setTextContent(DOMHelper::fromUTF8(ss.str()));

	} else {

		const Definition *base = findBaseDefinition(tdef);

		if (instance_of<CharacterTypeDefinition>(*base) || base->name.find("StringArray") != std::string::npos) {

			/* RULE 5.14.1 */
			/* INFO: StringArray is not used in MXF (ST 377-1) */
			throw FragmentBuilder::Exception("StringArray not supported.");

		} else {

			unsigned long itemcount = value.readLong();
			unsigned long itemlength = value.readLong();

			applyCoreRule5_4(element, value, tdef, itemcount);
		}

	}

}

void FragmentBuilder::applyRule5_15(DOMElement * element, MXFInputStream & value, const WeakReferenceTypeDefinition * typedefinition) {

	const ClassDefinition *classdef = (ClassDefinition*)defresolver.getDefinition(typedefinition->referencedType);

	const PropertyDefinition* uniquepropdef = NULL;

	std::vector<const PropertyDefinition*> props = getAllMembersOf(classdef);

	for (std::vector<const PropertyDefinition*>::const_iterator it = props.begin(); it != props.end(); it++) {
		if ((*it)->uniqueIdentifier.is_valid() && (*it)->uniqueIdentifier.get()) {
			uniquepropdef = *it;
			break;
		}
	}

	if (uniquepropdef == NULL) {

		throw MissingUniquePropertyError(
			classdef->identification,
			strf::fmt(
				"Definition {} at Element {}",
				typedefinition->symbol,
				DOMHelper::toUTF8(element->getLocalName()).c_str()
			)
		);

	}

	applyRule4(element, value, uniquepropdef);

}
