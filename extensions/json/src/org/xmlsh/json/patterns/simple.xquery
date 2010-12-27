module namespace simple="http://www.xmlsh.org/jsonxml/simple" ;
import module namespace common = "http://www.xmlsh.org/jsonxml/common"  at "common.xquery" ;
declare namespace jxon='http://www.xmlsh.org/jxon';
declare namespace xsl='http://www.w3.org/1999/XSL/Transform';




declare function simple:tojson_element( $e as element(element) )
{

let $match := common:match_elem( $e/name , $e ),
    $json :=  common:getjson( $e ),
	$config := common:getconfig( $json )
return (
comment { "simple:tojson_element" } ,

(: Top objects which needs wrapping in an OBJECT :)
<xsl:template match="{$match}" mode="wrap" priority="{common:priority($e)}">
		<OBJECT>
			<xsl:apply-templates select="."/>	
		
		</OBJECT>
	</xsl:template>
,
(: Unwrapped elements turn into MEMBER :)
	<xsl:template  match="{$match}" priority="{common:priority($e)}">
		<MEMBER name="{common:json_name($e/name)}">
			<xsl:choose>
				<!-- No attributes or child elements - jump to text  -->
				<xsl:when test="empty(@*|*)">
					{ common:json_text_value($e) }
				</xsl:when>
				<!-- Otherwise need to make an object out of this -->
				<xsl:otherwise>
					<OBJECT>

						<!-- For each element and attribute make a member -->
						<xsl:for-each select="@*|*">
							<xsl:apply-templates select="."/>
						</xsl:for-each>
						
						<!-- Wrap text in a _text node only for simple types -->
						{ 
							if( $e/@contentType eq "simple" )  then 
								<xsl:if test="string(.)">
								<MEMBER name="{$config/text/string()}">
										{ common:json_text_value($e) }
								</MEMBER>
							</xsl:if>
							else
								()
						}
					</OBJECT>
				</xsl:otherwise>
			</xsl:choose>
		</MEMBER>
	</xsl:template>
	
	

)

};

declare function simple:tojson_attribute( $e as element(attribute) )
{	

let $match := common:match_attr( $e/name , $e )
return 
(
comment { "simple:tojson_attribute" } ,
(: All attribute values turn into members of the same name :)
	<xsl:template match="{$match}" mode="#all"  priority="{common:priority($e)}">
		<MEMBER name="{common:json_name($e/name)}">
			{ common:json_text_value( $e ) } 
		</MEMBER>
	</xsl:template>
)
};







declare function simple:toxml_element( $e as element(element) )
{
	let 
    	$json :=  common:getjson( $e ),
		$config := common:getconfig( $json )
	return (


  comment { concat(" simple:toxml_element for " , $e/name/@localname ) },
  	text{ "&#x0a;" } , 
	let $match := common:match_json( $e/name , $e )
	return 
	(
	<xsl:template match="{$match}/OBJECT" >
			<xsl:apply-templates select="*" />
	</xsl:template>
	,

	
	<xsl:template match="{$match}/STRING | {$match}/NUMBER">
			<xsl:value-of select="string()"/>
	</xsl:template>,
	<xsl:template match="{$match}">
		<xsl:element name="{$e/name/@localname}" namespace="{$e/name/@uri}">
			<xsl:apply-templates select="*"/>
		</xsl:element>

	</xsl:template>,
	<xsl:template match="{$match}/OBJECT/MEMBER[@name='{$config/text/string()}']">
			<xsl:value-of select="string()" />
	</xsl:template>

	
    )

	)

};
declare function simple:toxml_attribute( $e as element(attribute) )
{	
  comment { concat(" simple:toxml_attribute for " , $e/name/@localname ) },
  	text{ "&#x0a;" } , 
	let $match := common:match_json( $e/name , $e )
	return 
(
	<xsl:template match="{$match}/OBJECT" >
			<xsl:apply-templates select="*" />
	</xsl:template>
	,

	
	<xsl:template match="{$match}/STRING | {$match}/NUMBER">
			<xsl:value-of select="string()"/>
	</xsl:template>,
	<xsl:template match="{$match}">
		<xsl:attribute name="{$e/name/@localname}" namespace="{$e/name/@uri}" >
			<xsl:apply-templates select="*"/>
		</xsl:attribute>

	</xsl:template>
    )

};

(: Stylus Studio meta-information - (c) 2004-2009. Progress Software Corporation. All rights reserved.

<metaInformation>
	<scenarios>
		<scenario default="yes" name="Scenario1" userelativepaths="yes" externalpreview="no" useresolver="yes" url="" outputurl="" processortype="datadirect" tcpport="0" profilemode="0" profiledepth="" profilelength="" urlprofilexml="" commandline=""
		          additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext="" host="" port="0" user="" password="" validateoutput="no" validator="internal"
		          customvalidator="">
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