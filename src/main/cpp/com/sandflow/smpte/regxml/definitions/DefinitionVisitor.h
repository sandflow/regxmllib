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
 
#ifndef COM_SANDFLOW_SMPTE_REGXML_DEFINITIONS_DEFINITIONVISITOR
#define COM_SANDFLOW_SMPTE_REGXML_DEFINITIONS_DEFINITIONVISITOR

namespace rxml {

	struct ClassDefinition;
	struct PropertyDefinition;
	struct PropertyAliasDefinition;
	struct EnumerationTypeDefinition;
	struct CharacterTypeDefinition;
	struct RenameTypeDefinition;
	struct RecordTypeDefinition;
	struct LensSerialFloatTypeDefinition;
	struct IntegerTypeDefinition;
	struct StrongReferenceTypeDefinition;
	struct WeakReferenceTypeDefinition;
	struct StringTypeDefinition;
	struct WeakReferenceTypeDefinition;
	struct ExtendibleEnumerationTypeDefinition;
	struct OpaqueTypeDefinition;
	struct IndirectTypeDefinition;
	struct StreamTypeDefinition;
	struct SetTypeDefinition;
	struct VariableArrayTypeDefinition;
	struct FixedArrayTypeDefinition;


	class DefinitionVisitor {

	public:

		virtual ~DefinitionVisitor() {};

		virtual void visit(const ClassDefinition &def) = 0;
		virtual void visit(const PropertyDefinition &def) = 0;
		virtual void visit(const PropertyAliasDefinition &def) = 0;
		virtual void visit(const EnumerationTypeDefinition &def) = 0;
		virtual void visit(const CharacterTypeDefinition &def) = 0;
		virtual void visit(const StringTypeDefinition &def) = 0;
		virtual void visit(const RenameTypeDefinition &def) = 0;
		virtual void visit(const RecordTypeDefinition &def) = 0;
		virtual void visit(const ExtendibleEnumerationTypeDefinition &def) = 0;
		virtual void visit(const LensSerialFloatTypeDefinition &def) = 0;
		virtual void visit(const IntegerTypeDefinition &def) = 0;
		virtual void visit(const StrongReferenceTypeDefinition &def) = 0;
		virtual void visit(const WeakReferenceTypeDefinition &def) = 0;
		virtual void visit(const VariableArrayTypeDefinition &def) = 0;
		virtual void visit(const FixedArrayTypeDefinition &def) = 0;
		virtual void visit(const OpaqueTypeDefinition &def) = 0;
		virtual void visit(const IndirectTypeDefinition &def) = 0;
		virtual void visit(const StreamTypeDefinition &def) = 0;
		virtual void visit(const SetTypeDefinition &def) = 0;
	};

	/* TODO: refactor in its own file */

	class NullDefinitionVisitor : public DefinitionVisitor {

	public:

		virtual void visit(const ClassDefinition &def) {};
		virtual void visit(const PropertyDefinition &def) {};
		virtual void visit(const PropertyAliasDefinition &def) {};
		virtual void visit(const EnumerationTypeDefinition &def) {};
		virtual void visit(const CharacterTypeDefinition &def) {};
		virtual void visit(const StringTypeDefinition &def) {};
		virtual void visit(const RenameTypeDefinition &def) {};
		virtual void visit(const RecordTypeDefinition &def) {};
		virtual void visit(const ExtendibleEnumerationTypeDefinition &def) {};
		virtual void visit(const LensSerialFloatTypeDefinition &def) {};
		virtual void visit(const IntegerTypeDefinition &def) {};
		virtual void visit(const StrongReferenceTypeDefinition &def) {};
		virtual void visit(const WeakReferenceTypeDefinition &def) {};
		virtual void visit(const VariableArrayTypeDefinition &def) {};
		virtual void visit(const FixedArrayTypeDefinition &def) {};
		virtual void visit(const OpaqueTypeDefinition &def) {};
		virtual void visit(const IndirectTypeDefinition &def) {};
		virtual void visit(const StreamTypeDefinition &def) {};
		virtual void visit(const SetTypeDefinition &def) {};

	};

	template<typename D> class DefinitionKindVisitor : public NullDefinitionVisitor {

	protected:

		DefinitionKindVisitor(const Definition &def) : iskind(false) {
			def.accept(*this);
		}

		virtual void visit(const D &def) {
			this->iskind = true;
		}

		bool iskind;

		template<class U> friend bool instance_of(const Definition &def);
	};

	template<typename D> bool instance_of(const Definition &def) {
		return DefinitionKindVisitor<D>(def).iskind;
	}
}

#endif