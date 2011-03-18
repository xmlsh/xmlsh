declare namespace json='http://www.xmlsh.org/jsonxml';
declare namespace xsl='http://www.w3.org/1999/XSL/Transform';
declare namespace jxon='http://www.xmlsh.org/jxon';
declare default element namespace 'http://www.xmlsh.org/jxml';
import module namespace common = "http://www.xmlsh.org/jsonxml/common"  at "common.xquery" ;





declare function local:tojson_document( $e as element(jxon:document) , $config as element(jxon:pattern) )
{	
	$common:nl,
	<xsl:template match="document-node()">
			<xsl:apply-templates select="*" mode="wrap"/>
	</xsl:template>



};


declare function local:tojson_attribute( $e as element(jxon:attribute), $pattern as element(jxon:pattern) )
{	

let $match := common:match_attr( $e/jxon:name , $e )
return 
(
	common:dump( $e ,$pattern ),
	$common:nl,
	<xsl:template match="{$match}" mode="#all"  priority="{common:priority($e)}">
		<member name="{common:json_name($e/jxon:name , $pattern ) }">
			{ common:json_text_value( $e, $pattern ) }
		</member>
	</xsl:template>

)
};




declare function local:tojson_element( $e as element(jxon:element) , $pattern as element(jxon:pattern) )
{

let $match := common:match_elem( $e/jxon:name , $e )

return (
common:dump( $e ,$pattern ),
$common:nl,
<xsl:template match="{$match}" priority="{common:priority($e)}">
		<member name="{common:json_name($e/jxon:name, $pattern )}">
		{		

			(: Array wrapping for repeated children :)
			if( $pattern/jxon:children/@wrap eq 'array' ) then 
				<array>
				{ 
					if( xs:boolean($pattern/jxon:children/@omitChild)) then
						(: If there are node chidren then wrap them in objects :)
						<xsl:for-each select="*">
						{
							<xsl:for-each select="node()">
								<xsl:choose>
									<xsl:when test="self::text()">
										<xsl:apply-templates select="."/>
									</xsl:when>
									<xsl:when test="self::*">
										<xsl:apply-templates select="." mode="wrap"/>
									</xsl:when>
								</xsl:choose>

							</xsl:for-each>
						}
						</xsl:for-each>
					else
						
						<xsl:apply-templates select="*" mode="wrap"/>
				}
				
				</array>
			else

			(: If we wrap attributes or children in their own child object :)
			if( $pattern/jxon:attributes/@wrap eq 'object' 
			 or $pattern/jxon:children/jxon:value/@wrap eq 'object' ) then 
				<object>
					<xsl:if test="@*">
						<member name="{$pattern/jxon:attributes/@name}">
							<object>
								{ (: Only apply to attributes which are marked as full :) 
								   for $a in $e/jxon:attribute
								   return
								   	<xsl:apply-templates select="{  common:attr_name( $a/jxon:name ) }"/>
								}							
							</object>
						</member>
					</xsl:if>

					<xsl:if test="node() except @*">	
				
						<member name="{$pattern/jxon:children/@name}">
							<!-- Applies element and text templates -->
							<array>
								<xsl:apply-templates select="node() except @*" mode="wrap"/>
							</array>
						</member>
					</xsl:if>
				</object>
			(: "simple" mode do not wrap children :)
			else 





			<xsl:choose>
				<!-- No attributes or child elements - jump to text  -->
				<xsl:when test="empty(@*|*)">
					{ common:json_text_value($e,$pattern) }
				</xsl:when>
				<!-- Otherwise need to make an object out of this -->
				<xsl:otherwise>
					<object>

						<!-- For each element and attribute make a member -->
						<xsl:for-each select="@*|*">
							<xsl:apply-templates select="."/>
						</xsl:for-each>
						
						<!-- Wrap text in a _text node only for simple types -->
						{ 
							if( $e/@contentType eq "simple" and $pattern/jxon:text/@wrap eq 'object' )  then 
								<xsl:if test="string(.)">
								<member name="{$pattern/jxon:text/@name}">
										{ common:json_text_value($e,$pattern) }
								</member>
							</xsl:if>
							else
								()
						}
					</object>
				</xsl:otherwise>
			</xsl:choose>
		
		}
		</member>
	</xsl:template>
	,
	$common:nl,
	<xsl:template match="{$match}" mode="wrap" priority="{common:priority($e)}">
		<object xmlns="http://www.xmlsh.org/jxml">
			<xsl:apply-templates select="."/>	
		
		</object>
	</xsl:template> 
	, $common:nl,
	<xsl:template match="{$match}/text()" mode="#all" priority="{common:priority($e)}">
		{ common:json_text_value( $e ,$pattern) }
	</xsl:template>
)

};





(: Dynamic dispatch  :)
declare function local:tojson( $es as element()* )
{
	for $e in $es 
	let  
		 $pattern  := common:getpattern( $e )
	return
	(
	typeswitch( $e ) 
	case	$elem as element(jxon:element)
		return local:tojson_element( $elem , $pattern ) 

			
	case	$a as element(jxon:attribute)
		return local:tojson_attribute($a , $pattern )
	case	$d as element(jxon:document)
		return local:tojson_document( $d , $pattern )
	default
		return ()
	,
	
	local:tojson( $e/jxon:element | $e/jxon:attribute  )
	)
};

document {
	<xsl:stylesheet version="2.0"  xmlns="http://www.xmlsh.org/jxml">
	{
		for $ns in $common:nsmap//ns
		return
			attribute { fn:QName($ns/@uri, concat($ns/@prefix,":bogus"))} {}

	}
	
	
	{
	


		local:tojson( $common:annotations/jxon:document )

	}
	</xsl:stylesheet>
}



(: Stylus Studio meta-information - (c) 2004-2009. Progress Software Corporation. All rights reserved.

<metaInformation>
	<scenarios>
		<scenario default="no" name="books" userelativepaths="yes" externalpreview="no" useresolver="yes" url="" outputurl="" processortype="saxon" tcpport="0" profilemode="0" profiledepth="" profilelength="" urlprofilexml="" commandline=""
		          additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext="" host="" port="0" user="" password="" validateoutput="no" validator="internal"
		          customvalidator="">
			<parameterValue name="{http://www.xmlsh.org/jsonxml/common}patterns" value="doc('patterns.xml')"/>
			<parameterValue name="{http://www.xmlsh.org/jsonxml/common}annotations" value="doc('..\..\..\..\..\..\..\..\jsonxml\playing\all.xml')"/>
			<advancedProperties name="DocumentURIResolver" value=""/>
			<advancedProperties name="bSchemaAware" value="false"/>
			<advancedProperties name="bXml11" value="false"/>
			<advancedProperties name="CollectionURIResolver" value=""/>
			<advancedProperties name="iValidation" value="0"/>
			<advancedProperties name="bExtensions" value="true"/>
			<advancedProperties name="iWhitespace" value="0"/>
			<advancedProperties name="bTinyTree" value="false"/>
			<advancedProperties name="bWarnings" value="true"/>
			<advancedProperties name="bUseDTD" value="false"/>
			<advancedProperties name="ModuleURIResolver" value=""/>
		</scenario>
		<scenario default="no" name="jxml" userelativepaths="yes" externalpreview="no" useresolver="yes" url="" outputurl="" processortype="saxon" tcpport="0" profilemode="0" profiledepth="" profilelength="" urlprofilexml="" commandline=""
		          additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext="" host="" port="0" user="" password="" validateoutput="no" validator="internal"
		          customvalidator="">
			<parameterValue name="{http://www.xmlsh.org/jsonxml/common}patterns" value="doc('patterns.xml')"/>
			<parameterValue name="{http://www.xmlsh.org/jsonxml/common}annotations" value="doc('..\..\..\..\..\..\..\..\jsonxml\jxml\all.xml')"/>
			<advancedProperties name="DocumentURIResolver" value=""/>
			<advancedProperties name="bSchemaAware" value="false"/>
			<advancedProperties name="bXml11" value="false"/>
			<advancedProperties name="CollectionURIResolver" value=""/>
			<advancedProperties name="iValidation" value="0"/>
			<advancedProperties name="bExtensions" value="true"/>
			<advancedProperties name="iWhitespace" value="0"/>
			<advancedProperties name="bTinyTree" value="false"/>
			<advancedProperties name="bUseDTD" value="false"/>
			<advancedProperties name="bWarnings" value="true"/>
			<advancedProperties name="ModuleURIResolver" value=""/>
		</scenario>
		<scenario default="no" name="dx1" userelativepaths="yes" externalpreview="no" useresolver="yes" url="" outputurl="" processortype="saxon" tcpport="0" profilemode="0" profiledepth="" profilelength="" urlprofilexml="" commandline="" additionalpath=""
		          additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext="" host="" port="0" user="" password="" validateoutput="no" validator="internal" customvalidator="">
			<parameterValue name="{http://www.xmlsh.org/jsonxml/common}patterns" value="doc('patterns.xml')"/>
			<parameterValue name="{http://www.xmlsh.org/jsonxml/common}annotations" value="doc('..\..\..\..\..\..\..\..\jsonxml\dx\all.xml')"/>
			<advancedProperties name="DocumentURIResolver" value=""/>
			<advancedProperties name="bSchemaAware" value="false"/>
			<advancedProperties name="bXml11" value="false"/>
			<advancedProperties name="CollectionURIResolver" value=""/>
			<advancedProperties name="iValidation" value="0"/>
			<advancedProperties name="bExtensions" value="true"/>
			<advancedProperties name="iWhitespace" value="0"/>
			<advancedProperties name="bTinyTree" value="false"/>
			<advancedProperties name="bWarnings" value="true"/>
			<advancedProperties name="bUseDTD" value="false"/>
			<advancedProperties name="ModuleURIResolver" value=""/>
		</scenario>
		<scenario default="yes" name="docbook" userelativepaths="yes" externalpreview="no" useresolver="yes" url="" outputurl="" processortype="saxon" tcpport="0" profilemode="0" profiledepth="" profilelength="" urlprofilexml="" commandline=""
		          additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext="" host="" port="0" user="" password="" validateoutput="no" validator="internal"
		          customvalidator="">
			<parameterValue name="{http://www.xmlsh.org/jsonxml/common}patterns" value="doc('patterns.xml')"/>
			<parameterValue name="{http://www.xmlsh.org/jsonxml/common}annotations" value="doc('..\..\..\..\..\..\..\..\jsonxml\docbook\all.xml')"/>
			<advancedProperties name="DocumentURIResolver" value=""/>
			<advancedProperties name="bSchemaAware" value="false"/>
			<advancedProperties name="bXml11" value="false"/>
			<advancedProperties name="CollectionURIResolver" value=""/>
			<advancedProperties name="iValidation" value="0"/>
			<advancedProperties name="bExtensions" value="true"/>
			<advancedProperties name="iWhitespace" value="0"/>
			<advancedProperties name="bTinyTree" value="false"/>
			<advancedProperties name="bUseDTD" value="false"/>
			<advancedProperties name="bWarnings" value="true"/>
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