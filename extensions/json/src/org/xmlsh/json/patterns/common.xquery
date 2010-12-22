module namespace common="http://www.xmlsh.org/jsonxml/common" ;
declare namespace xsl='http://www.w3.org/1999/XSL/Transform';

declare variable $common:annotations as document-node() external ;
declare variable $common:patterns    as document-node() external ;



(: Priority of match string for an element - use the depth of the element :)
declare function common:priority( $e as element( ) )
{
	count( $e/ancestor::*/name )
};


(: Get the self or nearest parents <json> element :)

declare function common:getjson( $e as element() ) as element(json)
{
	let $json := ($e/ancestor-or-self::*/json)[last()]
	return $json

};

(: construct a QName from a name :)
declare function common:qname( $name as element() ) as xs:QName 
{
	fn:QName( $name/@uri , $name/@localname )
};

declare function common:json_type( $e as element(json) , $type as xs:QName ) as xs:string
{
	if( $type = 
	   ( xs:QName("xs:decimal") , 
	     xs:QName("xs:integer" ) ,
		 xs:QName("xs:float") ) )
	 then
	 	"NUMBER"
	else
		"STRING"   	


};




(: Get the JSON or overridden type for an element/attribute :)

declare function common:json_text_type( $e as element() ) as xs:string
{
	if( $e/@contentType ne "simple" or empty($e/type ) ) then
		"STRING" 
	else
	let $name := common:qname( $e/type )
	return
		common:json_type( common:getjson($e) , $name )


};

(: Format a basic json text value for a text node :)
declare function common:json_text_value( $e as element() )
{

		element { common:json_text_type( $e ) } 
		{
			<xsl:value-of select="."/>
		}

};

(: Generate a match string for an element :)
declare function common:match_elem( $name as element() , $e as element() ) as xs:string
{

	fn:string-join( ($e/ancestor::*/name/@localname , $name/@localname) , "/" )


};

(: The Attribute name for use in match or select strings :)
declare function common:attr_name( $name as element(name) )
{
	concat( "@" , $name/@localname  )

};


(: Generate a match string for an attribute :)
declare function common:match_attr( $name as element() , $e as element() ) as xs:string
{
	fn:string-join( ($e/ancestor::*/name/@localname , common:attr_name($name) ) , "/" )


};


(: Construct the JSON name for an element or attribute :)
declare function common:json_name( $e as element(name)?  )
{
	if( empty( $e ) ) 
		then () 
	else
		$e/@localname 
};

(: Construct a match string for a json member :)
declare function common:member_name( $e as element(name) ?  ) as xs:string  ?
{
	let $name := common:json_name( $e )
	return
	if( $name ne '' ) then 
		concat("MEMBER[@name='" , $name , "']")
	else 
		()
};

(: Construct a match string for a json object :)
declare function common:match_json( $name as element()? , $e as element() ) as xs:string
{
	fn:string-join( (
		for $n in $e/ancestor::*/name
		return common:member_name( $n  ) , 
		common:member_name( $name ) ) , 
		"/OBJECT/" )


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