 __   ___  __                        __  
|__) |__  / _` \_/ |\/| |    |    | |__) 
|  \ |___ \__> / \ |  | |___ |___ | |__) 


INTRODUCTION
============

regxmllib is a collection of tools and libraries for the creation of 
RegXML (SMPTE ST 2001-1) representations of MXF header metadata
(SMPTE ST 377-1). 

regxmllib is implemented in pure Java. Netbeans 8.0 is used for development.


PREREQUISITES
=============

Java 8 SDK

(recommended) Git

(recommended) Netbeans 8.0 or Ant

(recommended) SMPTE Metadata Registers (Types, Elements, Groups and Labels) (see [1] for a recent snapshot)

[1] https://kws.smpte.org/kws/groups/30mr/download/33216


QUICK START
===========

The following outputs to path PATH_TO_FRAGMENT an XML representation
of the header metadata of the MXF file at path PATH_TO_MXF_FILE 

* build the jar target using Netbeans or Ant

* retrieve the four SMPTE Metadata Registers
    
* build the metadictionaries from the SMPTE registers

    java -cp <PATH_TO_JAR> com.sandflow.smpte.tools.XMLRegistersToDict -e <PATH_TO_ELEMENTS_REG>
    -l <PATH_TO_LABELS_REG> -g <PATH_TO_GROUPS_REG> -t <PATH_TO_TYPES_REG> PATH_TO_DICT_DIR
  
* generate the RegXML fragment
    
    run java -cp <PATH_TO_JAR> com.sandflow.smpte.tools.RegXMLDump -all -d <PATH_TO_DICT1> <PATH_TO_DICT2> ...
    -i PATH_TO_MXF_FILE > PATH_TO_FRAGMENT


ARCHITECTURE
============

At the heart of regxmllib is the FragmentBuilder.fragmentFromTriplet() method
that creates an XML fragment from a KLV group given a
a RegXML metadictionary and a collection of KLV groups from which strong
references are resolved. The rules engine implemented in 
FragmentBuilder.fragmentFromTriplet() is intended to follow the rules specified
in ST 2001-1 as closely as possible. A sister method, XMLSchemaBuilder.fromDictionary(),
creates a matching XML Schema that can be used to validate RegXML fragments.

Metadictionaries can be imported and exported from XML documents that conform
to the schema specified in SMPTE ST 2001-1. They can also be created
from SMPTE Metadata Registers published in XML form.

regxmllib includes a minimal MXF and KLV parser library.


PACKAGES
========

com.sandflow.smpte.klv : Classes for processing SMPTE KLV triplets (ST 336)
    
com.sandflow.smpte.mxf: Classes for processing SMPTE MXF structures (ST 377-1)

com.sandflow.smpte.register: Classes for processing SMPTE metadata registers
    (ST 335, ST 395, ST 400, ST 2003)
    
com.sandfow.smpte.regxml: Classes for managing RegXML metadictionaries and
    creating RegXML representations of MXF structures
    
com.sandfow.smpte.tools: Application-level tools
    
com.sandfow.smpte.util: Utilities classes

TOOLS
=====

RegXMLDump: dumps either the first essence descriptor or the entire header
            metadata of an MXF file as a RegXML structure
            
XMLRegistersToDict: converts XML-based SMPTE metadata registers to a RegXML metadictionaries

GenerateXMLSchemaDocuments: generates XSDs for the SMPTE metadata registers
                            
GenerateDictionaryXMLSchema: generate XSDs for RegXML Fragments from the RegXML metadictionaries

BUILDING METADICTIONARIES
=========================

RegXML metadictionaries can be built directly from SMPTE metadata registers using the build
system.

* store all four SMPTE registers at:

    /input/xml-registers/Elements.xml
    /input/xml-registers/Types.xml
    /input/xml-registers/Labels.xml
    /input/xml-registers/Groups.xml

* run the build-regxml-dict target

* the metadictionaries are output at

    /output/regxml-dicts 
    

BUILDING REGXML XML SCHEMAS
===========================

XML Schemas for RegXML Fragments can be built directly from SMPTE metadata registers
using the build system.

* build the RegXML metadictionaries as described in BUILDING METADICTIONARIES

* run the build-regxml-schemas target

* the xml schemas are output at

    /output/regxml-schemas

BUILDING METADATA REGISTER SCHEMAS
==================================

The build-register-schemas build target can be used to generate XML Schemas for 
SMPTE Metadata Registers directly from the classes at com.sandflow.smpte.register.

* build the build-register-schemas target

* the Metadata Register schemas are output at

    /output/register-schemas 
    
UNIT TEST
=========

Unit testing is performed by generating RegXML fragments from sample files located
at [1] and registers located at [2]. The resulting RegXML fragments are compared
to reference RegXML fragments located at [3].

Reference RegXML fragments can regenerated using by building the build-reference-test-files target.

[1] /regxmllib/src/test/sample-files
[2] /regxmllib/src/test/reference-registers
[3] /regxmllib/src/test/reference-files 
    
                            
KNOWN ISSUES
============

Issues are tracked at [1]

[1] https://github.com/sandflow/regxmllib/issues


DIRECTORIES AND NOTABLE FILES
=============================

/regxmllib                              Source code and build artifacts

/regxmllib/build.xml                    Build script (Ant)

/regxmllib/build.properties             Constants, e.g. directory paths, used
                                        by the build script

/regxmllib/nbproject                    Netbeans project files

/regxmllib/src/config/repoversion.properties
                                        Java properties file containing the 
                                        a unique source code version generated using
                                        git by the build system

/regxmllib/src/main/resources/reg.xsd
                                        Common XML Schema definitions used when generating
                                        XML Schemas for RegXML Fragments
                                        
/regxmllib/src/test/reference-files     Reference RegXML fragment used for unit testing

/regxmllib/src/test/reference-registers
                                        Reference SMPTE registers used for unit testing

/regxmllib/src/test/sample-files
                                        Sample MXF files used for unit testing                                       
                                        
/output/register-schemas                Stub directory containing XML Schemas
                                        generated by the build system for the 
                                        SMPTE Metadata Registers                                        
                                        
/output/regxml-dicts                    Stub directory containing the RegXML metadictionaries
                                        generated by the build system, from the SMPTE Metadata
                                        Registers in /input/xml-registers
 
/output/regxml-schemas                  Stub directory containing XML Schemas for RegXML Fragments
                                        generated by the library from the RegXML metadictionaries
                                        in /output/regxml-dicts

/input/xml-registers                    Stub directory containing the (optional)
                                        SMPTE Metadata Registers used to build
                                        the metadictionaries
                                        

          