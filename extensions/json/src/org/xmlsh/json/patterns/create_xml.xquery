declare namespace json='http://www.xmlsh.org/jsonxml';
declare namespace xsl='http://www.w3.org/1999/XSL/Transform';
declare namespace jxon='http://www.xmlsh.org/jxon';
import module namespace common = "http://www.xmlsh.org/jsonxml/common"  at "common.xquery" ;



declare function local:toxml_document( $e as element(jxon:document) , $pattern as element(jxon:pattern) )
{

	<xsl:template  match="/OBJECT">
		<xsl:apply-templates select="*"/>
	</xsl:template>,

	<xsl:template  match="ARRAY">
	
	</xsl:template>



};







declare function local:toxml_attribute( $e as element(jxon:attribute),$pattern as element(jxon:pattern)  )
{	
	common:dump($e ,$pattern ),
	$common:nl,

	let $ppattern := common:getpattern( $e/.. )
	return
	( <!-- parent element -->,
		$common:nl,
		common:dump($e/..,$ppattern) ,
		$common:nl,


	if( $ppattern/jxon:attributes/@wrap ne 'none' ) then 
		let $match := common:match_json( () , $e )
		return 
			<xsl:template match="{$match}/OBJECT/MEMBER[@name eq '{$ppattern/jxon:attributes/@name}']/OBJECT/{common:member_name($e/jxon:name)}">
				<xsl:attribute name="{$e/jxon:name/@localname}" namespace="{$e/jxon:name/@uri}">
						<xsl:apply-templates select="*"/>
				</xsl:attribute>
			</xsl:template>
	else	
		let $match := common:match_json( $e/jxon:name , $e )
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
				<xsl:attribute name="{$e/jxon:name/@localname}" namespace="{$e/jxon:name/@uri}" >
					<xsl:apply-templates select="*"/>
				</xsl:attribute>

			</xsl:template>
		  )
	)
};







declare function local:toxml_element( $e as element(jxon:element) , $pattern as element(jxon:pattern) )
{
	common:dump( $e ,$pattern ),
	$common:nl,
	let $ppattern := common:getpattern( $e/.. )
	return

	let $match := common:match_json( $e/jxon:name , $e )
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
		<xsl:element name="{$e/jxon:name/@localname}" namespace="{$e/jxon:name/@uri}">
			<xsl:apply-templates select="*"/>
		</xsl:element>

	</xsl:template>,
	if( $pattern/jxon:text/@wrap eq 'object' )  then 
	<xsl:template match="{$match}/OBJECT/MEMBER[@name='{$pattern/jxon:text/@name}']">
		{
			if( $pattern/jxon:value/@wrap eq 'array' )  then
				<xsl:copy-of select="string-join( ARRAY/(NUMBER|STRING) , ' ')"/>			
			else
				<xsl:value-of select="string()" />
		}
	</xsl:template> else () ,
	if( $pattern/jxon:children/@wrap eq 'object' ) then 
	<xsl:template match="{$match}/OBJECT/MEMBER[@name eq '{$pattern/jxon:children/@name}']">
		<xsl:apply-templates select="ARRAY/*"/>
	</xsl:template>
	else () 
    )


};




(: Dynamic dispatch  :)
declare function local:toxml( $es as element()* )
{
	for $e in $es 
	let  
		 $pattern  := common:getpattern( $e )
	return
	(
	typeswitch( $e ) 
	case	$elem as element(jxon:element)
		return local:toxml_element( $elem , $pattern ) 
	case	$a as element(jxon:attribute)
		return local:toxml_attribute($a , $pattern )
	case	$d as element(jxon:document)
		return local:toxml_document( $d , $pattern )
	default
		return ()
	,

	local:toxml( $e/jxon:element | $e/jxon:attribute )
	)
};







document {
	<xsl:stylesheet version="2.0" >
	<xsl:strip-space elements="OBJECT MEMBER ARRAY" />
	{
		local:toxml( $common:annotations/jxon:document )

	}
	</xsl:stylesheet>
}



(: Stylus Studio meta-information - (c) 2004-2009. Progress Software Corporation. All rights reserved.

<metaInformation>
	<scenarios>
		<scenario default="yes" name="tojson" userelativepaths="yes" externalpreview="no" useresolver="yes" url="..\..\..\..\..\..\..\..\jsonxml\playing\all.xml" outputurl="" processortype="saxon" tcpport="0" profilemode="0" profiledepth=""
		          profilelength="" urlprofilexml="" commandline="" additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext="" host="" port="0" user="" password=""
		          validateoutput="no" validator="internal" customvalidator="">
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
	</scenarios>
	<MapperMetaTag>
		<MapperInfo srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/>
		<MapperBlockPosition></MapperBlockPosition>
		<TemplateContext></TemplateContext>
		<MapperFilter side="source"></MapperFilter>
	</MapperMetaTag>
</metaInformation>
:)