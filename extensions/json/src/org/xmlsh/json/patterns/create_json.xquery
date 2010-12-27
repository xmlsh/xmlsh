declare namespace json='http://www.xmlsh.org/jsonxml';
declare namespace xsl='http://www.w3.org/1999/XSL/Transform';
declare namespace jxon='http://www.xmlsh.org/jxon';

import module namespace common = "http://www.xmlsh.org/jsonxml/common"  at "common.xquery" ;
import module namespace full = "http://www.xmlsh.org/jsonxml/full" at "full.xquery" ;
import module namespace simple = "http://www.xmlsh.org/jsonxml/simple" at "simple.xquery" ;



declare function local:tojson_element( $e as element(element) , $config as element(pattern) )
{
	if( $config/@name eq 'full' ) then
		full:tojson_element($e )
	else
		simple:tojson_element($e)
	
};

declare function local:tojson_attribute( $e as element(attribute) , $config as element(pattern) )
{
	if( $config/@name eq 'full' ) then
		full:tojson_attribute($e )
	else
		simple:tojson_attribute($e)
	
};

declare function local:tojson_document( $e as element(document) , $config as element(pattern) )
{
	<xsl:template match="document-node()">
			<xsl:apply-templates select="*" mode="wrap"/>
	</xsl:template>



};



(: Dynamic dispatch  :)
declare function local:tojson( $es as element()* )
{
	for $e in $es 
	let  $json := common:getjson( $e ) , 
		 $config  := common:getconfig( $json )
	return
	(
	typeswitch( $e ) 
	case	$elem as element(element)
		return local:tojson_element( $elem , $config ) 
	case	$a as element(attribute)
		return local:tojson_attribute($a , $config )
	case	$d as element(document)
		return local:tojson_document( $d , $config )
	default
		return ()
	,

	local:tojson( $e/element | $e/attribute )
	)
};

document {
	<xsl:stylesheet version="2.0" >
	{
		local:tojson( $common:annotations/document )

	}
	</xsl:stylesheet>
}



(: Stylus Studio meta-information - (c) 2004-2009. Progress Software Corporation. All rights reserved.

<metaInformation>
	<scenarios>
		<scenario default="yes" name="tojson" userelativepaths="yes" externalpreview="no" useresolver="yes" url="" outputurl="" processortype="saxon" tcpport="0" profilemode="0" profiledepth="" profilelength="" urlprofilexml="" commandline=""
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