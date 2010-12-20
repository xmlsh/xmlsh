module namespace full="http://www.xmlsh.org/jsonxml/full" ;
declare namespace xsl='http://www.w3.org/1999/XSL/Transform';
import module namespace common = "http://www.xmlsh.org/jsonxml/common"  at "common.xquery" ;


declare function full:tojson_name( $e as element(name) ) as xs:string
{
	common:json_name( $e   ) 
};


declare function full:tojson_element( $e as element(element) )
{

let $match := common:match_elem( $e/name , $e )
return (

comment { "full:tojson_element" } ,
<xsl:template match="{$match}" priority="{common:priority($e)}">
		<MEMBER name="{full:tojson_name($e/name)}">
			<OBJECT>
			<xsl:if test="@*">
				<MEMBER name="_attributes">
					<OBJECT>
						{ (: Only apply to attributes which are marked as full :) 
						   for $a in $e/attribute[ common:getjson( . )/@pattern eq 'full' ]
						   return
						   	<xsl:apply-templates select="{  common:attr_name( $a/name ) }"/>
						}							
					</OBJECT>
				</MEMBER>
			</xsl:if>
			{ (: Apply attributes which are not full  :)
			   for $a in $e/attribute[ common:getjson( . )/@pattern ne 'full' ]
			   return
			   	<xsl:apply-templates select="{  common:attr_name( $a/name ) }"/>
			}							



			<xsl:if test="node() except @*">	
				
				<MEMBER name="_children">
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
		<STRING>
			<xsl:value-of select="."/>
		</STRING>
	</xsl:template>
)

};

declare function full:tojson_attribute( $e as element(attribute) )
{	

let $match := common:match_attr( $e/name , $e )
return 
(
comment { "full:tojson_attribute" } ,
	<xsl:template match="{$match}" mode="#all"  priority="{common:priority($e)}">
		<MEMBER name="{full:tojson_name($e/name) }">
			<STRING>
				<xsl:value-of select="."/>
			</STRING>
		</MEMBER>
	</xsl:template>

)
};


declare function full:tojson_document( $e as element(document) )
{
	<xsl:template match="document-node()">
			<xsl:apply-templates select="*" mode="wrap"/>
	</xsl:template>

};


declare function full:tojson( $node as element() ) as node()*
{
	typeswitch( $node) 
	case	$e as element(element) 
		return full:tojson_element( $e ) 
	case	$a as element(attribute)
		return full:tojson_attribute($a )
	case	$d as element(document)
		return full:tojson_document( $d )
	default
		return ()
		
};




declare function full:toxml_element( $e as element(element) )
{
	comment { concat(" full:toxml_element for " , $e/name/@localname ) },
	let $match := common:match_json( $e/name , $e )
	return 
	(
	<xsl:template match="{$match}/OBJECT" >
			<xsl:apply-templates select="*" />
	</xsl:template>
	,

	<xsl:template match="{$match}/OBJECT/MEMBER[@name eq '_children']">
		<xsl:apply-templates select="ARRAY/*"/>
	</xsl:template>,
	
	<xsl:template match="{$match}/STRING | {$match}/NUMBER">
			<xsl:value-of select="string()"/>
	</xsl:template>,
	<xsl:template match="{$match}">
		<xsl:element name="{$e/name/@localname}" namespace="{$e/name/@uri}">
			<xsl:apply-templates select="*"/>
		</xsl:element>

	</xsl:template>
	
    )

};

declare function full:toxml_attribute( $e as element(attribute) )
{	

  comment { concat(" full:toxml_attribute for " , $e/name/@localname ) },
  	text{ "&#x0a;" } , 
	let $match := common:match_json( () , $e )
	return 
		<xsl:template match="{$match}/OBJECT/MEMBER[@name eq '_attributes']/OBJECT/{common:member_name($e/name)}">
			<xsl:attribute name="{$e/name/@localname}" namespace="{$e/name/@uri}">
					<xsl:apply-templates select="*"/>
			</xsl:attribute>
		</xsl:template>


};


declare function full:toxml_document( $e as element(document) )
{

	<xsl:template  match="/OBJECT">
		<xsl:apply-templates select="*"/>
	</xsl:template>,

	<xsl:template  match="ARRAY">
	
	</xsl:template>

};

declare function full:toxml( $node as element() ) as node()*
{
	typeswitch( $node) 
	case	$e as element(element) 
		return full:toxml_element( $e ) 
	case	$a as element(attribute)
		return full:toxml_attribute($a )
	case	$d as element(document)
		return full:toxml_document( $d )
	default
		return ()
		
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