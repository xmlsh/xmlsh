module namespace full="http://www.xmlsh.org/jsonxml/full" ;
declare namespace xsl='http://www.w3.org/1999/XSL/Transform';

declare namespace jxon='http://www.xmlsh.org/jxon';
import module namespace common = "http://www.xmlsh.org/jsonxml/common"  at "common.xquery" ;




declare function full:tojson_element( $e as element(jxon:element) )
{

let $match := common:match_elem( $e/jxon:name , $e ),
    $json :=  common:getjson( $e ),
	$config := common:getconfig( $json ) 
return (

comment { "full:tojson_element" } ,
<xsl:template match="{$match}" priority="{common:priority($e)}">
		<MEMBER name="{common:json_name($e/jxon:name)}">
			<OBJECT>
			<xsl:if test="@*">
				<MEMBER name="{$config/jxon:attributes/string()}">
					<OBJECT>
						{ (: Only apply to attributes which are marked as full :) 
						   for $a in $e/jxon:attribute[ common:getjson( . )/@name eq 'full' ]
						   return
						   	<xsl:apply-templates select="{  common:attr_name( $a/jxon:name ) }"/>
						}							
					</OBJECT>
				</MEMBER>
			</xsl:if>
			{ (: Apply attributes which are not full  :)
			   for $a in $e/jxon:attribute[ common:getjson( . )/@name ne 'full' ]
			   return
			   	<xsl:apply-templates select="{  common:attr_name( $a/jxon:name ) }"/>
			}							



			<xsl:if test="node() except @*">	
				
				<MEMBER name="{$config/jxon:children/string()}">
					<ARRAY>
						<xsl:apply-templates select="node() except @*" mode="wrap"/>
					</ARRAY>
				</MEMBER>
			</xsl:if>
			</OBJECT>
		</MEMBER>
	</xsl:template>
	,
	<xsl:template match="{$match}" mode="wrap" priority="{common:priority($e)}">
		<OBJECT>
			<xsl:apply-templates select="."/>	
		
		</OBJECT>
	</xsl:template> 
	,
	<xsl:template match="{$match}/text()" mode="#all" priority="{common:priority($e)}">
		{ common:json_text_value( $e ) }
	</xsl:template>
)

};

declare function full:tojson_attribute( $e as element(jxon:attribute) )
{	

let $match := common:match_attr( $e/jxon:name , $e )
return 
(
comment { "full:tojson_attribute" } ,
	<xsl:template match="{$match}" mode="#all"  priority="{common:priority($e)}">
		<MEMBER name="{common:json_name($e/jxon:name) }">
			{ common:json_text_value( $e ) }
		</MEMBER>
	</xsl:template>

)
};





declare function full:toxml_element( $e as element(jxon:element) )
{
    let
		$json :=  common:getjson( $e ) ,
		$config := common:getconfig( $json ) 
	return (

	comment { concat(" full:toxml_element for " , $e/jxon:name/@localname ) },
	let $match := common:match_json( $e/jxon:name , $e )
	return 
	(
	<xsl:template match="{$match}/OBJECT" >
			<xsl:apply-templates select="*" />
	</xsl:template>
	,

	<xsl:template match="{$match}/OBJECT/MEMBER[@name eq '{$config/jxon:children/string()}']">
		<xsl:apply-templates select="ARRAY/*"/>
	</xsl:template>,
	
	<xsl:template match="{$match}/STRING | {$match}/NUMBER">
			<xsl:value-of select="string()"/>
	</xsl:template>,
	<xsl:template match="{$match}">
		<xsl:element name="{$e/jxon:name/@localname}" namespace="{$e/jxon:name/@uri}">
			<xsl:apply-templates select="*"/>
		</xsl:element>

	</xsl:template>
	
	
	)
	)

};

declare function full:toxml_attribute( $e as element(jxon:attribute) )
{	
	let
		$json :=  common:getjson( $e ),
		$config := common:getconfig( $json )
return (
  comment { concat(" full:toxml_attribute for " , $e/jxon:name/@localname ) },
  	text{ "&#x0a;" } , 
	let $match := common:match_json( () , $e )
	return 
		<xsl:template match="{$match}/OBJECT/MEMBER[@name eq '{$config/jxon:attributes/string()}']/OBJECT/{common:member_name($e/jxon:name)}">
			<xsl:attribute name="{$e/jxon:name/@localname}" namespace="{$e/jxon:name/@uri}">
					<xsl:apply-templates select="*"/>
			</xsl:attribute>
		</xsl:template>

	)
};


(: Stylus Studio meta-information - (c) 2004-2009. Progress Software Corporation. All rights reserved.

<metaInformation>
	<scenarios>
		<scenario default="yes" name="Scenario1" userelativepaths="yes" externalpreview="no" useresolver="yes" url="..\..\..\..\..\..\..\..\jsonxml\playing\books.jxml" outputurl="" processortype="datadirect" tcpport="0" profilemode="0" profiledepth=""
		          profilelength="" urlprofilexml="" commandline="" additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext="" host="" port="0" user="" password=""
		          validateoutput="no" validator="internal" customvalidator="">
			<advancedProperties name="DocumentURIResolver" value=""/>
			<advancedProperties name="CollectionURIResolver" value=""/>
			<advancedProperties name="ModuleURIResolver" value=""/>
		</scenario>
	</scenarios>
	<MapperMetaTag>
		<MapperInfo srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/>
		<MapperBlockPosition></MapperBlockPosition>
		<TemplateContext></TemplateContext>
		<MapperFilter side="source"></MapperFilter>
	</MapperMetaTag>
</metaInformation>
:)