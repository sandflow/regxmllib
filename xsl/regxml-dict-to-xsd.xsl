<?xml version="1.0" encoding="UTF-8"?>

<!--Copyright (c) 2014, Pierre-Anthony Lemieux (pal@sandflow.com)-->
<!--All rights reserved.-->
<!---->
<!--Redistribution and use in source and binary forms, with or without-->
<!--modification, are permitted provided that the following conditions are met:-->
<!---->
<!--* Redistributions of source code must retain the above copyright notice, this-->
<!--list of conditions and the following disclaimer.-->
<!--* Redistributions in binary form must reproduce the above copyright notice,-->
<!--this list of conditions and the following disclaimer in the documentation-->
<!--and/or other materials provided with the distribution.-->
<!---->
<!--THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"-->
<!--AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE-->
<!--IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE-->
<!--ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE-->
<!--LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR-->
<!--CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF-->
<!--SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS-->
<!--INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN-->
<!--CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)-->
<!--ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE-->
<!--POSSIBILITY OF SUCH DAMAGE.-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns="http://www.w3.org/2001/XMLSchema"
    xmlns:md="http://www.smpte-ra.org/schemas/2001-1b/2013/metadict"
    xmlns:common="http://exslt.org/common"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    exclude-result-prefixes="common md">

    <xsl:output method="xml" encoding="UTF-8" indent="yes"/>


    <xsl:template match="/md:Baseline">

        <xsl:variable name="reg" select="/*/md:SchemeURI/text()"/>
        
        <xsl:variable name="ns-container">
            <xsl:element name="reg:dummy" namespace="{$reg}"/>
        </xsl:variable>
        
         <schema>
             
             <xsl:attribute name="elementFormDefault">qualified</xsl:attribute>
             
             <xsl:attribute name="attributeFormDefault">unqualified</xsl:attribute>
             
             <xsl:attribute name="targetNamespace"><xsl:value-of select="$reg"/></xsl:attribute>
             
             <!-- horrible hack to get around a Xalan bug in a horrible hack to get around the lack of xsl:namespace in XSLT 1.0 -->
             
             <!-- <xsl:for-each select="common:node-set($ns-container)//*/namespace::node()"> 
                 <xsl:copy /> 
             </xsl:for-each> 
             -->
             
             <xsl:copy-of select="common:node-set($ns-container)//*/namespace::reg"/>
             

            
                        
            <import namespace="http://www.w3.org/1999/xlink"
                schemaLocation="http://www.w3.org/1999/xlink.xsd"/>

            <xsl:comment>Generic ST 2001-1 type definitions</xsl:comment>

            <simpleType name="TargetType">
                <union memberTypes="token">
                    <simpleType>
                        <restriction base="reg:AUID"/>
                    </simpleType>
                    <simpleType>
                        <xs:restriction base="reg:PackageIDType"/>
                    </simpleType>
                    <simpleType>
                        <restriction base="string">
                            <pattern value="([^\s]+\s)?[^\s]+"/>
                        </restriction>
                    </simpleType>
                </union>
            </simpleType>
            <simpleType name="ByteOrderType">
                <restriction base="string">
                    <enumeration value="BigEndian"/>
                    <enumeration value="LittleEndian"/>
                </restriction>
            </simpleType>
            <simpleType name="HexByteArrayType">
                <restriction base="string">
                    <pattern value=" (\s*[0-9a-fA-F][0-9a-fA-F])*\s*"/>
                </restriction>
            </simpleType>

            <xsl:comment>Generic ST 2001-1 attribute definitions</xsl:comment>

            <attribute name="uid" type="reg:TargetType"/>
            <attribute name="byteOrder" type="reg:ByteOrderType"/>
            <attribute name="stream" type="ENTITY"/>
            <attribute name="actualType" type="reg:TargetType"/>
            <attribute name="escaped" type="boolean"/>
            <attribute name="path" type="string"/>

            <xsl:comment>ST 2001-1 meta dictionary definitions</xsl:comment>

             
            <xsl:apply-templates select="md:MetaDefinitions/*"/>
            
        </schema>

    </xsl:template>
    
    <!-- Rule 4 -->

    <xsl:template match="md:ClassDefinition">
        
        <!--<xsl:message>Rule 4</xsl:message>-->
        
        <xs:element>
            <xsl:attribute name="name">
                <xsl:value-of select="md:Symbol"/>
            </xsl:attribute>

            <xsl:if test="md:IsConcrete = 'false'">
                <xsl:attribute name="abstract">true</xsl:attribute>
            </xsl:if>

            <complexType>
                <!-- <complexContent>-->
                <all>
                    <xsl:call-template name="Rule_4_1">
                        <xsl:with-param name="class" select="."/>
                    </xsl:call-template>
                </all>

                <!-- <attribute ref="reg:path" use="optional"/>  -->
                <xsl:if test="0">
                    <attribute ref="reg:uid" use="required"/>
                </xsl:if>


                <!--</complexContent>-->
            </complexType>

        </xs:element>


    </xsl:template>

    <xsl:template name="Rule_4_1">

        <!-- Rule 4.1 -->

        <xsl:param name="class"/>
        
        <!--<xsl:message>Rule 4.1</xsl:message>-->
        <!--<xsl:message><xsl:value-of select="$class/md:Identification"/></xsl:message>-->

        <xsl:if test="$class/md:ParentClass">
            
            <!--<xsl:message>Rule 4.1(1)</xsl:message>-->
           
            <xsl:call-template name="Rule_4_1">
                <xsl:with-param name="class"
                    select="//md:ClassDefinition[md:Identification = $class/md:ParentClass]"
                />
            </xsl:call-template>
        </xsl:if>

        <xsl:for-each
            select="//md:PropertyDefinition[md:MemberOf = $class/md:Identification]">
            <xs:element ref="reg:{md:Symbol}">
                
      <!--          <xsl:message>Rule 4.1(2)</xsl:message>-->

                <xsl:if test="md:IsOptional = 'true'">
                    <xsl:attribute name="minOccurs">0</xsl:attribute>
                </xsl:if>

            </xs:element>
        </xsl:for-each>

    </xsl:template>

    <xsl:template match="md:PropertyDefinition[md:Symbol = 'ByteOrder']">

        <!--<xsl:message>Rule 5.1</xsl:message>-->

        <!-- Rule 5.1 -->
        <xs:element>
            <xsl:attribute name="name">
                <xsl:value-of select="md:Symbol"/>
            </xsl:attribute>

            <xsl:attribute name="type">reg:ByteOrderType</xsl:attribute>

        </xs:element>

    </xsl:template>

    <!-- Rule 5 -->
    <xsl:template match="md:PropertyDefinition | /md:PropertyAliasDefinition">
        
        <!--<xsl:message>Rule 5</xsl:message>-->

                <xs:element name="{md:Symbol}" type="reg:{../md:*[md:Identification = current()/md:Type]/md:Symbol}"> </xs:element>
    </xsl:template>

    <!-- Rule 6.1 -->

    <xsl:template match="md:TypeDefinitionCharacter">

        <!--<xsl:message>Rule 6.1</xsl:message>-->

        <complexType name="{md:Symbol}">
            <simpleContent>
                <extension base="string">
                    <attribute ref="reg:escaped" use="optional"/>
                </extension>
            </simpleContent>
        </complexType>

        <element name="{md:Symbol}" type="reg:{md:Symbol}"/>
    </xsl:template>


    <!-- Rule 6.2 -->

    <xsl:template match="md:TypeDefinitionEnumeration">

        <!--<xsl:message>Rule 6.2</xsl:message>-->

        <simpleType name="{md:Symbol}">
            <restriction base="token">
                <xsl:for-each select="md:Elements/md:Name">
                    <enumeration>
                        <xsl:attribute name="value">
                            <xsl:value-of select="."/>
                        </xsl:attribute>
                    </enumeration>
                </xsl:for-each>
            </restriction>
        </simpleType>

        <element name="{md:Symbol}" type="reg:{md:Symbol}"/>
    </xsl:template>

    <!-- Rule 6.3 -->

    <xsl:template match="md:TypeDefinitionExtendibleEnumeration">
        
        <!--<xsl:message>Rule 6.3</xsl:message>-->

        <simpleType name="{md:Symbol}">
            <union memberTypes="reg:AUID">
                <simpleType>
                    <restriction base="token">
                        <xsl:for-each
                            select="../md:ExtendibleEnumerationElement[md:ElementOf = current()/md:Identification]">
                            <enumeration value="{Name}"/>
                        </xsl:for-each>
                    </restriction>
                </simpleType>
            </union>
        </simpleType>

        <element name="{md:Symbol}" type="reg:{md:Symbol}"/>
    </xsl:template>

    <!-- Rule 6.4 -->

    <xsl:template match="md:TypeDefinitionFixedArray">

        <!--<xsl:message>Rule 6.4</xsl:message>-->

        <xsl:variable name="elementtype"
            select="../md:*[md:Identification = current()/md:ElementType]"/>



        <xsl:choose>
            <xsl:when test="name($elementtype) = 'md:TypeDefinitionStrongObjectReference'">

                <complexType name="{md:Symbol}">
                    <choice minOccurs="{md:ElementCount}" maxOccurs="{md:ElementCount}">

                        <xsl:call-template name="Rule_6_4_a">
                            <xsl:with-param name="class" select="../md:ClassDefinition[md:Identification = $elementtype/md:ReferencedType]"/>
                        </xsl:call-template>
                    </choice>
                </complexType>
            </xsl:when>
            <xsl:otherwise>
                <complexType name="{md:Symbol}">
                    <sequence>
                        <element ref="reg:{$elementtype/md:Symbol}" minOccurs="{md:ElementCount}"
                            maxOccurs="{md:ElementCount}"/>
                    </sequence>
                </complexType>
            </xsl:otherwise>
        </xsl:choose>

        <element name="{md:Symbol}" type="reg:{md:Symbol}"/>
    </xsl:template>

    <xsl:template name="Rule_6_4_a">

        <xsl:param name="class"/>
        
        <!--<xsl:message>Rule 6.4_a</xsl:message>-->

        <xsl:if test="$class">

            <xsl:if test="$class/md:IsConcrete = 'true'">
                <element ref="reg:{$class/md:Symbol}"/>
            </xsl:if>

            <xsl:for-each select="../md:ClassDefinition[md:ParentClass = $class/md:Identification]">
                <xsl:call-template name="Rule_6_4_a">
                    <xsl:with-param name="class" select="."/>
                </xsl:call-template>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

    <!-- Rule 6.5 -->

    <xsl:template match="md:TypeDefinitionIndirect">
        
        <!--<xsl:message>Rule 6.5</xsl:message>-->

        <complexType name="{md:Symbol}">
            <complexContent mixed="true">
                <restriction base="anyType">
                    <sequence>
                        <any minOccurs="0" maxOccurs="unbounded" processContents="skip"/>
                    </sequence>
                    <attribute ref="reg:actualType" use="required"/>
                    <attribute ref="reg:escaped" use="optional"/>
                </restriction>
            </complexContent>
        </complexType>

    </xsl:template>

    <!-- Rule 6.6 -->

    <xsl:template match="md:TypeDefinitionInteger">
        
        <!--<xsl:message>Rule 6.6</xsl:message>-->


        <simpleType name="{md:Symbol}">
            <union>
                <simpleType>
                    <restriction base="{type}">
                        <xsl:attribute name="base">
                            <xsl:choose>

                                <xsl:when test="md:Size = 1">
                                    <xsl:choose>
                                        <xsl:when test="md:IsSigned = 'true'">byte</xsl:when>
                                        <xsl:otherwise>unsignedByte</xsl:otherwise>
                                    </xsl:choose>


                                </xsl:when>
                                <xsl:when test="md:Size = 2">
                                    <xsl:choose>
                                        <xsl:when test="md:IsSigned = 'true'">short</xsl:when>
                                        <xsl:otherwise>unsignedShort</xsl:otherwise>
                                    </xsl:choose>


                                </xsl:when>
                                <xsl:when test="md:Size = 4">
                                    <xsl:choose>
                                        <xsl:when test="md:IsSigned = 'true'">integer</xsl:when>
                                        <xsl:otherwise>unsignedInt</xsl:otherwise>
                                    </xsl:choose>


                                </xsl:when>
                                <xsl:when test="md:Size = 8">
                                    <xsl:choose>
                                        <xsl:when test="md:IsSigned = 'true'">long</xsl:when>
                                        <xsl:otherwise>unsignedLong</xsl:otherwise>
                                    </xsl:choose>

                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:message terminate="yes">Bad integer type.</xsl:message>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>
                    </restriction>
                </simpleType>
                <simpleType>
                    <restriction base="string">
                        <pattern>
                            <xsl:attribute name="value">

                                <xsl:choose>

                                    <xsl:when test="md:Size = 1">0x[0-9a-fA-F]{1,2}</xsl:when>
                                    <xsl:when test="md:Size = 2">0x[0-9a-fA-F]{1,4}</xsl:when>
                                    <xsl:when test="md:Size = 4">0x[0-9a-fA-F]{1,8}</xsl:when>
                                    <xsl:when test="md:Size = 8">0x[0-9a-fA-F]{1,16}</xsl:when>
                                    <xsl:otherwise>
                                        <xsl:message terminate="yes">Bad integer type.</xsl:message>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>

                        </pattern>
                    </restriction>
                </simpleType>
            </union>
        </simpleType>

        <element name="{md:Symbol}" type="reg:{md:Symbol}"/>

    </xsl:template>

    <!-- Rule 6.7 -->

    <xsl:template match="md:TypeDefinitionOpaque">
        
        <!--<xsl:message>Rule 6.7</xsl:message>-->

        <complexType name="{md:Symbol}">
            <simpleContent>
                <extension base="reg:HexByteArrayType">

                    <attribute ref="reg:actualType" use="required"/>
                    <attribute ref="reg:byteOrder" use="required"/>
                </extension>
            </simpleContent>
        </complexType>

    </xsl:template>

    <!-- Rule 6.8 -->
    
    <xsl:template match="md:TypeDefinitionRecord">
        
        <!--<xsl:message>Rule 6.8</xsl:message>-->

        <complexType name="{md:Symbol}">
            <sequence>
                <xsl:for-each select="md:Members/md:Name">
                    
                    <element name="{.}" type="reg:{//md:*[md:Identification = (current()/following-sibling::md:Type)[1]]/md:Symbol}">

                    </element>
                </xsl:for-each>

            </sequence>
        </complexType>

        <element name="{md:Symbol}" type="reg:{md:Symbol}"/>
    </xsl:template>

    <xsl:template match="md:TypeDefinitionRecord[md:Symbol = 'AUID']">
        
        <!--<xsl:message>Rule 6.8b</xsl:message>-->

        <simpleType name="AUID">
            <restriction base="xs:anyURI">
                <pattern value="urn:smpte:ul:([0-9a-fA-F]{{8}}\.){{3}}[0-9a-fA-F]{{8}}"/>
                <pattern value="urn:uuid:[0-9a-fA-F]{{8}}-([0-9a-fA-F]{{4}}-){{3}}[0-9a-fA-F]{{12}}"
                />
            </restriction>
        </simpleType>

        <element name="{md:Symbol}" type="reg:{md:Symbol}"/>
    </xsl:template>


    <xsl:template match="md:TypeDefinitionRecord[md:Symbol = 'DateStruct']">
        
        <!--<xsl:message>Rule 6.8c</xsl:message>-->

        <simpleType name="DateStruct">
            <union>
                <simpleType>
                    <restriction base="date">
                        <pattern value=".+(((\+|\-)\d\d:\d\d)|Z)"/>
                    </restriction>
                </simpleType>
                <simpleType>
                    <restriction base="xs:string">
                        <enumeration value="0000-00-00Z"/>
                    </restriction>
                </simpleType>
            </union>
        </simpleType>

        <element name="{md:Symbol}" type="reg:{md:Symbol}"/>
    </xsl:template>

    <xsl:template match="md:TypeDefinitionRecord[md:Symbol = 'PackageIDType']">

        <simpleType name="PackageIDType">
            <restriction base="string">
                <pattern value=" urn:smpte:umid:([0-9a-fA-F]{{8}}\.){{7}}[0-9a-fA-F]{{8}}"/>
            </restriction>
        </simpleType>

        <element name="{md:Symbol}" type="reg:{md:Symbol}"/>
    </xsl:template>

    <xsl:template match="md:TypeDefinitionRecord[md:Symbol = 'Rational']">

        <simpleType name="Rational">
            <restriction base="string">
                <pattern>
                    <xsl:attribute name="value">\-?\d{1,10}(/\-?\d{1,10})?</xsl:attribute>
                </pattern>
            </restriction>
        </simpleType>

        <element name="{md:Symbol}" type="reg:{md:Symbol}"/>
    </xsl:template>

    <xsl:template match="md:TypeDefinitionRecord[md:Symbol = 'TimeStruct']">

        <simpleType name="TimeStruct">
            <union>
                <simpleType>
                    <restriction base="time">
                        <pattern value=".+(((\+|\-)\d\d:\d\d)|Z)"/>
                    </restriction>
                </simpleType>
                <simpleType>
                    <restriction base="string">
                        <enumeration value="00:00:00Z"/>
                    </restriction>
                </simpleType>
            </union>
        </simpleType>

        <element name="{md:Symbol}" type="reg:{md:Symbol}"/>
    </xsl:template>


    <xsl:template match="md:TypeDefinitionRecord[md:Symbol = 'TimeStamp']">

        <simpleType name="TimeStamp">
            <union>

                <simpleType>
                    <restriction base="dateTime">
                        <pattern value=".+(((\+|\-)\d\d:\d\d)|Z)"/>
                    </restriction>
                </simpleType>
                <simpleType>
                    <restriction base="string">
                        <enumeration value="0000-00-00T00:00:00Z"/>
                    </restriction>
                </simpleType>
            </union>
        </simpleType>

        <element name="{md:Symbol}" type="reg:{md:Symbol}"/>
    </xsl:template>

    <xsl:template match="md:TypeDefinitionRecord[md:Symbol = 'VersionType']">

        <simpleType name="VersionType">
            <restriction base="string">
                <pattern>
                    <xsl:attribute name="value">\-?\d{1,3}\.\-?\d{1,3}</xsl:attribute>
                </pattern>
            </restriction>
        </simpleType>

        <element name="{md:Symbol}" type="reg:{md:Symbol}"/>

    </xsl:template>

    <!-- Rule 6.9 -->

    <xsl:template match="md:TypeDefinitionRename">
        
<!--        <xsl:message>Rule 6.9</xsl:message>-->
        
        <xsl:variable name="rename-type-def" select="../md:*[md:Identification = current()/md:RenamedType]"/>

        <xsl:variable name="rename-schema-result">
            <xsl:apply-templates select="//md:*[md:Identification = current()/md:RenamedType]"/>
        </xsl:variable>
        

        <xsl:choose>
            
            <!-- bug in XSLTC: apply templates strips namespaces -->
            
            <xsl:when test="local-name(common:node-set($rename-schema-result)/*[1]) = 'complexType'">
                <complexType name="{md:Symbol}">
                    <complexContent>
                        <extension base="reg:{$rename-type-def/md:Symbol}"/>
                    </complexContent>
                </complexType>
            </xsl:when>

            <xsl:otherwise>
                <simpleType name="{md:Symbol}">
                    <restriction base="reg:{$rename-type-def/md:Symbol}"/>

                </simpleType>
            </xsl:otherwise>
        </xsl:choose>
        
        <element name="{md:Symbol}" type="reg:{md:Symbol}"/>

    </xsl:template>

    <!-- Rule 6.10 -->
  
  
    <xsl:template match="md:TypeDefinitionSet">
        
<!--        <xsl:message>Rule 6.10</xsl:message>-->

        <xsl:variable name="elementtype"
            select="../md:*[md:Identification = current()/md:ElementType]"/>

        <xsl:choose>
            <xsl:when test="local-name($elementtype) = 'TypeDefinitionStrongObjectReference'">

                <!-- Rule 6.10.1 -->

                <complexType name="{md:Symbol}">
                    <choice minOccurs="0" maxOccurs="unbounded">
                        
                        <xsl:call-template name="Rule_6_4_a">
                            <xsl:with-param name="class" select="../md:ClassDefinition[md:Identification = $elementtype/md:ReferencedType]"/>
                        </xsl:call-template>
                    </choice>
                </complexType>
            </xsl:when>
            <xsl:otherwise>

                <!-- Rule 6.10.2 -->

                <complexType name="{md:Symbol}">
                    <sequence>
                        <element ref="reg:{$elementtype/md:Symbol}" minOccurs="0" maxOccurs="unbounded"/>
                    </sequence>
                </complexType>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <!-- Rule 6.11 -->

    <xsl:template match="md:TypeDefinitionStream">
        
<!--        <xsl:message>6.11</xsl:message>-->

        <complexType name="{md:Symbol}">
            <attribute ref="reg:stream" use="optional"/>
            <attribute ref="xlink:href" use="optional"/>
            <attribute ref="reg:byteOrder" use="optional"/>
        </complexType>

        <element name="{md:Symbol}" type="reg:{md:Symbol}"/>

    </xsl:template>

    <!-- Rule 6.12 -->

    <xsl:template match="md:TypeDefinitionString">
        
<!--        <xsl:message>Rule 6.12</xsl:message>
-->
        <complexType name="{md:Symbol}">
            <simpleContent>
                <extension base="string">
                    <attribute ref="reg:escaped" use="optional"/>
                </extension>
            </simpleContent>
        </complexType>

    </xsl:template>

    <!-- Rule 6.13 -->

    <xsl:template match="md:TypeDefinitionStrongObjectReference">
        
        <!--<xsl:message>6.13</xsl:message>-->

        <complexType name="{md:Symbol}">
            <choice minOccurs="0" maxOccurs="unbounded">

                <xsl:call-template name="Rule_6_4_a">
                    <xsl:with-param name="class"
                        select="../md:ClassDefinition[md:Symbol = current()/md:ReferencedType]"/>
                </xsl:call-template>
            </choice>
        </complexType>

    </xsl:template>

    <!-- Rule 6.14 -->

    <xsl:template match="md:TypeDefinitionVariableArray">
        
        <!--<xsl:message>6.14</xsl:message>-->
        
        
        <xsl:variable name="elementtype"
            select="../md:*[md:Identification = current()/md:ElementType]"/>
        
        
        

        <xsl:choose>

            <!-- Rule 6.14.1 -->

            <xsl:when test="local-name($elementtype) = 'TypeDefinitionStrongObjectReference'">

                <complexType name="{md:Symbol}">
                    <choice minOccurs="0" maxOccurs="unbounded">

                        <xsl:call-template name="Rule_6_4_a">
                            <xsl:with-param name="class" select="../md:ClassDefinition[md:Identification = $elementtype/md:ReferencedType]"/>
                        </xsl:call-template>
                    </choice>
                </complexType>
            </xsl:when>

            <!-- Rule 6.14.2 -->

            <xsl:when test="local-name($elementtype) = TypeDefinitionCharacter or contains($elementtype/md:Symbol, 'StringArray')">
                <complexType name="{md:Symbol}">
                    <sequence>
                        <element ref="reg:{$elementtype/md:Symbol}" minOccurs="0" maxOccurs="unbounded"/>
                    </sequence>
                </complexType>
            </xsl:when>


            <!-- Rule 6.14.3 -->

            <xsl:when test="$elementtype/md:Symbol = 'DataValue'">
                <simpleType name="DataValue">
                    <restriction base="reg:HexByteArrayType"/>
                </simpleType>
            </xsl:when>

            <!-- Rule 6.14.4 -->

            <xsl:otherwise>
                <complexType name="{md:Symbol}">
                    <sequence>
                        <element ref="reg:{$elementtype/md:Symbol}" minOccurs="0" maxOccurs="unbounded"/>
                    </sequence>
                </complexType>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <!-- Rule 6.15 -->

    <xsl:template match="md:TypeDefinitionWeakObjectReference">

        <simpleType name="{md:Symbol}">
            <restriction base="reg:TargetType"/>
        </simpleType>
        
        <element name="{md:Symbol}" type="reg:{md:Symbol}"/>

    </xsl:template>

    <!-- ignore -->

    <xsl:template match="md:ExtendibleEnumerationElement">
        
        <!--<xsl:message>Rule Extendible</xsl:message>-->
        
    </xsl:template>

</xsl:stylesheet>
