/*
 * Copyright (c) 2014, Pierre-Anthony Lemieux (pal@sandflow.com)
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

package com.sandflow.smpte.regxml.dict.definitions;

/**
 * Abstract visitor class (of a Visitor Design Pattern) for MetaDictionary definitions. 
 */
public interface DefinitionVisitor {
    
    class VisitorException extends Exception {

        public VisitorException() {
            super();
        }

        public VisitorException(String string) {
            super(string);
        }

        public VisitorException(String string, Throwable thrwbl) {
            super(string, thrwbl);
        }

        public VisitorException(Throwable thrwbl) {
            super(thrwbl);
        }
        
    }

    public void visit(ClassDefinition def) throws VisitorException;
    public void visit(CharacterTypeDefinition def) throws VisitorException;
    public void visit(IntegerTypeDefinition def) throws VisitorException;
    public void visit(PropertyDefinition def) throws VisitorException;
    public void visit(ExtendibleEnumerationTypeDefinition def) throws VisitorException;
    public void visit(EnumerationTypeDefinition def) throws VisitorException;
    public void visit(FixedArrayTypeDefinition def) throws VisitorException;
    public void visit(IndirectTypeDefinition def) throws VisitorException;
    public void visit(OpaqueTypeDefinition def) throws VisitorException;
    public void visit(RecordTypeDefinition def) throws VisitorException;
    public void visit(RenameTypeDefinition def) throws VisitorException;
    public void visit(SetTypeDefinition def) throws VisitorException;
    public void visit(StreamTypeDefinition def) throws VisitorException;
    public void visit(StrongReferenceTypeDefinition def) throws VisitorException;
    public void visit(StringTypeDefinition def) throws VisitorException;
    public void visit(VariableArrayTypeDefinition def) throws VisitorException;
    public void visit(WeakReferenceTypeDefinition def) throws VisitorException;
    public void visit(PropertyAliasDefinition def) throws VisitorException;
    public void visit(FloatTypeDefinition def) throws VisitorException;
    public void visit(LensSerialFloatTypeDefinition def) throws VisitorException;
}
