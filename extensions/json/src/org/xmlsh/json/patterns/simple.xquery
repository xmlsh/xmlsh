module namespace simple="http://www.xmlsh.org/jsonxml/simple" ;
import module namespace common = "http://www.xmlsh.org/jsonxml/common"  at "common.xquery" ;
declare namespace xsl='http://www.w3.org/1999/XSL/Transform';

declare function simple:tojson_element( $e as element(element) )
{

let $match := common:match_elem( $e/name , $e )
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
		<MEMBER name="{{local-name(.)}}">
			<xsl:choose>
				<!-- No attributes or child elements - jump to text  -->
				<xsl:when test="empty(@*|*)">
					<xsl:apply-templates select="node()"/>
				</xsl:when>
				<!-- Otherwise need to make an object out of this -->
				<xsl:otherwise>
					<OBJECT>

						<!-- For each element and attribute make a member -->
						<xsl:for-each select="@*|*">
							<xsl:apply-templates select="."/>
						</xsl:for-each>
						
						<!-- Wrap text in a _text node -->
						<xsl:if test="text()">
							<MEMBER name="_text">
								<xsl:apply-templates select="text()"/>
							</MEMBER>
						</xsl:if>
					</OBJECT>
				</xsl:otherwise>
			</xsl:choose>
		</MEMBER>
	</xsl:template>
,
	<xsl:template  match="{$match}[ not(attribute()) and not(element()) ]" priority="{common:priority($e) + 1 }">
		<MEMBER name="{{local-name(.)}}">
			<xsl:apply-templates select="node()"/>
			
		</MEMBER>
	</xsl:template>,
	
	<xsl:template match="{$match}/text()" mode="#all" priority="{common:priority($e)}">
		<STRING>
			<xsl:value-of select="."/>
		</STRING>
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
		<MEMBER name="{{local-name(.)}}">
			<STRING>
				<xsl:value-of select="."/>
			</STRING>
		</MEMBER>
	</xsl:template>
)
};


declare function simple:tojson_document( $e as element(document) )
{
	<xsl:template match="document-node()">
			<xsl:apply-templates select="*" mode="wrap"/>
	</xsl:template>

};


declare function simple:tojson( $node as element() ) as node()*
{
	typeswitch( $node) 
	case	$e as element(element) 
		return simple:tojson_element( $e ) 
	case	$a as element(attribute)
		return simple:tojson_attribute($a )
	case	$d as element(document)
		return simple:tojson_document( $d )
	default
		return ()
		
};




declare function simple:toxml_document( $e as element(document) )
{

	<xsl:template  match="/OBJECT">
		<xsl:apply-templates select="*"/>
	</xsl:template>,

	<xsl:template  match="ARRAY">
	
	</xsl:template>
};


declare function simple:toxml_element( $e as element(element) )
{
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
		<xsl:element name="{{@name}}">
			<xsl:apply-templates select="*"/>
		</xsl:element>

	</xsl:template>,
	<xsl:template match="{$match}/OBJECT/MEMBER[@name='_text']">
			<xsl:value-of select="string()" />
	</xsl:template>
	
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
		<xsl:attribute name="{{@name}}">
			<xsl:apply-templates select="*"/>
		</xsl:attribute>

	</xsl:template>
    )

};
declare function simple:toxml( $node as element() ) as node()*
{
	typeswitch( $node) 
	case	$e as element(element) 
		return simple:toxml_element( $e ) 
	case	$a as element(attribute)
		return simple:toxml_attribute($a )
	case	$d as element(document)
		return simple:toxml_document( $d )
	default
		return ()
		
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