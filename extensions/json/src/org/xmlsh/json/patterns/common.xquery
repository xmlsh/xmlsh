module namespace common="http://www.xmlsh.org/jsonxml/common" ;
declare default element namespace 'http://www.xmlsh.org/jxml';
declare namespace xsl='http://www.w3.org/1999/XSL/Transform';
declare namespace jxon='http://www.xmlsh.org/jxon';

declare variable $common:annotations as document-node() external ;
declare variable $common:patterns    as document-node() external ;
declare variable $common:nl := "&#xA;" ;
declare variable $common:default_pattern as element(jxon:pattern) :=  $common:patterns/jxon:patterns/jxon:pattern[@name eq $common:patterns/jxon:patterns/@default];

declare function common:dump( $e as element() , $pattern as element(jxon:pattern) ) 
{
	comment { 
	concat( $common:nl,
			local-name($e) , ": " , common:qname($e/jxon:name ), $common:nl , 
			"Config : " , $common:nl ,
			fn:string-join(
				for $a in $pattern/@* return
				concat("@" , node-name($a) , ": " , $a/string()) , $common:nl ) ,
			$common:nl ),
			fn:string-join(
				for $c in $pattern/* return
				  concat(node-name($c) , ": " , $c/string(), $common:nl ,
					 fn:string-join(
					 	for $ca in $c/@* return
					    concat("  @" , node-name($ca) , ": " , $ca/string() ) , $common:nl ))
			  , $common:nl 
	)
				
	}
};

(: Priority of match string for an element - use the depth of the element :)
declare function common:priority( $e as element( ) )
{
	count( $e/ancestor::*/jxon:name )
};

(: Given a prototype (root) and a list of parent/derived patterns generate a single pattern :)
declare function common:inherit( $e as element() , $prototype as element(jxon:pattern) , $ps as element(jxon:pattern)* ) as element(jxon:pattern)
{
	if( empty( $ps ) ) then 
		<jxon:pattern name="{$prototype/@name}" >
			{$prototype/*[fn:node-name(.) eq fn:node-name($e)]/*}
		</jxon:pattern>
	else

	<jxon:pattern name="{$prototype/@name}" >
	{
		for $elem in $prototype/*[fn:node-name(.) eq fn:node-name($e)]/*
		return
			( $elem , $ps/*[fn:node-name(.) eq fn:node-name($e)]/*[ (fn:node-name( . ) eq fn:node-name( $elem )) ] )[last()]
	}

	</jxon:pattern>
	
	

};

(: Get a list of all type and parent type patterns :)
declare function common:gettypepatterns( $typename as xs:QName? ) as element(jxon:pattern)*
{
	if( empty($typename) ) then () else
	
	let 
		$pattern :=  $common:annotations/jxon:document/jxon:type_decl[ common:qname(name) eq $typename ]/jxon:pattern
	return
		( common:gettypepatterns( common:parent_type( $typename ) ) , $pattern )

};


(: Get the configuration pattern element coresponding to this pattern :)
declare function common:getpattern( $e as element() ) as element(jxon:pattern)
{

	(: Get the self or nearest parents <pattern> element or types :)
	let $json := $e/ancestor-or-self::jxon:element|$e/ancestor-or-self::jxon:attribute,
		$patterns := (
		for $n in $json return
			( 
				common:gettypepatterns( common:qname( $n/jxon:type )  ) ,  $n/jxon:pattern 
			),
			(: Synthesize a nameless pattern element for all direct children :)
			let $c := ($e/jxon:value|$e/jxon:text|$e/jxon:json_name)
			return
			if( exists($c) ) then
				<jxon:pattern>
				{
					element { node-name($e) } { $c } 
				}				
				</jxon:pattern>
			else 
				()
				
				
		 )
		

	return 
		common:inherit( $e , $common:patterns/jxon:patterns/jxon:pattern[@name eq ($common:default_pattern/@name , $patterns/@name)[last()]] , $patterns )
	

};



(: construct a QName from a name :)
declare function common:qname( $name as element()? ) as xs:QName  ?
{
	if( empty( $name ) ) then () else 
	fn:QName( $name/@uri , $name/@localname )
};

declare function common:parent_type( $type as xs:QName ) as xs:QName?
{
	common:qname(   $common:annotations/jxon:document/jxon:type_decl[common:qname(jxon:name) eq $type]/jxon:basetype )

};

declare function common:json_value( $type as xs:string , $wrap as xs:string ) as element(jxon:value)
{
	<jxon:value type="{$type}" wrap="{$wrap}" />

};



declare function common:json_type( $pattern as element(jxon:pattern) , $type as xs:QName? ) as element(jxon:value)
{
	if( $pattern/jxon:value/@type ne 'schema' ) then
		$pattern/jxon:value

	else
	if( empty($type) ) then
		common:json_value("STRING" , "none")
	else
	if( $type = 
	   ( xs:QName("xs:decimal") , 
	     xs:QName("xs:integer" ) ,
		 xs:QName("xs:float") ) )
	 then
	 	common:json_value("NUMBER" , "none" )
	else 
		common:json_type( $pattern , common:parent_type( $type ) )

};




(: Get the JSON or overridden type for an element/attribute :)

declare function common:json_text_type( $e as element(),  $pattern as element(jxon:pattern) ) as element(jxon:value)
{
	if( empty($e/jxon:type ) ) then
		common:json_value("STRING" , "none" )
	else
	let $name := common:qname( $e/jxon:type )
	return
		common:json_type( $pattern , $name )


};

(: Format a basic json text value for a text node :)
declare function common:json_text_value( $e as element() , $pattern as element(jxon:pattern) )
{
	let $type := common:json_text_type( $e , $pattern )
	return 
	if( $type/@wrap eq 'array' ) then 
		<ARRAY>
			<xsl:for-each select="tokenize(.,' ')">
			{
				element { $type/@type/string() } 
				{
					<xsl:value-of select="."/>
				}
			}
			</xsl:for-each>
		</ARRAY>

	else
		element { $type/@type/string() } 
		{
			<xsl:value-of select="."/>
		}

	

};

(: Generate a match string for an element :)
declare function common:match_elem( $name as element() , $e as element() ) as xs:string
{

	fn:string-join( ($e/ancestor::*/jxon:name/@localname , $name/@localname) , "/" )


};

(: The Attribute name for use in match or select strings :)
declare function common:attr_name( $name as element(jxon:name) )
{
	concat( "@" , common:xml_qname($name)  )

};


(: Generate a match string for an attribute :)
declare function common:match_attr( $name as element() , $e as element() ) as xs:string
{
	fn:string-join( (

	for $parent in $e/ancestor::*/jxon:name
	return common:xml_qname($parent) , 
	
	common:attr_name($name) ) , "/" )


};

(: Full xmlname with prefix for matching names and attributes :)
declare function common:xml_qname( $name as element(jxon:name) ) as xs:string
{
	$name/@localname 


};

declare function common:clark_name( $name as element(jxon:name) ) as xs:string 
{
	if( exists($name/@uri) and $name/@uri ne ''  ) then 
		concat("{",$name/@uri,"}", $name/@localname )
	else
		$name/@localname


};



(: Construct the JSON name for an element or attribute :)
declare function common:json_name( $name as element(jxon:name) , $pattern as element(jxon:pattern) ) as xs:string
{
	if( $pattern/jxon:json_name ) then
		if( $pattern/jxon:json_name/@name ) then 
			$pattern/jxon:json_name/@name 
		else
			fn:replace( common:clark_name( $name ) , $pattern/jxon:json_name/@search , $pattern/jxon:json_name/@replace )
	else
		$name/@localname 
};




(: Construct a match string for a json member :)
declare function common:member_name( $e as element(jxon:name) , $pattern as element(jxon:pattern)  ) as xs:string 
{
	let $name := common:json_name( $e , $pattern )
	return 
		concat("MEMBER[@name='" , $name , "']")
};

(: 
  Construct a match string for a json object 
  For each level of nested (local) elements join with either /OBJECT/ or /OBJECT/ARRAY/

:)
declare function common:match_json( $name as element(jxon:name)? , $e as element() ) as xs:string
{
	fn:string-join( (
		for $a in $e/ancestor::jxon:element
		let $n := $a/jxon:name ,
		    $pattern := common:getpattern( $a )
		return (
			common:member_name( $n  , $pattern ) , 
			if( $pattern/jxon:children/@wrap eq 'object' )
				then concat("/OBJECT/MEMBER[@name eq '" , $pattern/jxon:children/@name,"']/ARRAY/OBJECT/" )
			else	"/OBJECT/"
		),
			common:member_name( $name , common:getpattern($e) ) 
		
	) , "" )


};


(: Construct a match string for a json object :)
declare function common:match_json_attribute( $name as element(jxon:name)? , $e as element() ) as xs:string
{
	fn:string-join( (
		for $parent in $e/ancestor::*[jxon:name]
		return common:member_name( $parent/jxon:name  , common:getpattern($parent) ) , 
		common:member_name( $name , common:getpattern($e) ) ) , 
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