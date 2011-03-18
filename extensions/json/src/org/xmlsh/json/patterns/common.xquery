module namespace common="http://www.xmlsh.org/jsonxml/common" ;
declare default element namespace 'http://www.xmlsh.org/jxml';
declare namespace xsl='http://www.w3.org/1999/XSL/Transform';
declare namespace jxon='http://www.xmlsh.org/jxon';

declare variable $common:annotations as document-node() external ;
declare variable $common:patterns    as document-node() external ;
declare variable $common:nl := "&#xA;" ;
declare variable $common:default_pattern as element(jxon:pattern) :=  
	  $common:patterns/jxon:patterns/jxon:pattern[@name eq $common:patterns/jxon:patterns/@default];



declare variable $common:nsmap := <nsmap>{
	for $ns at $pos in distinct-values($common:annotations//jxon:name[@uri ne '']/@uri/string())
	return 	
	
	if( $ns eq 'http://www.w3.org/XML/1998/namespace' ) then 
		<ns prefix="xml" uri="{$ns}" /> 
	else
		<ns prefix="{concat( 'ns' , $pos )}" uri="{$ns}" /> 
	}
	</nsmap>;


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
		( common:gettypepatterns( common:parent_type( $typename )) , $pattern )

};


(: Get the configuration pattern element coresponding to this pattern :)
declare function common:getpattern( $e as element() ) as element(jxon:pattern)
{

	(: Get the self or nearest parents <pattern> element or types :)
	let $json := $e/ancestor-or-self::jxon:element|$e/ancestor-or-self::jxon:attribute,
		$patterns := (
		(: document pattern if any :)
		$common:annotations/jxon:document/jxon:pattern,
		(: child patterns :)
		for $n in $json return
			( 
				common:gettypepatterns( common:qname( $n/jxon:type )  ) ,  $n/jxon:pattern 
			),
			(: Synthesize a nameless pattern element for all direct children :)
			let $c := ($e/jxon:value|$e/jxon:text|$e/jxon:json_name|$e/jxon:children|$e/jxon:attributes)
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


declare function common:type_decl( $type as xs:QName ) as element(jxon:type_decl)
{
	$common:annotations/jxon:document/jxon:type_decl[common:qname(jxon:name) eq $type]

};
declare function common:parent_type( $type as xs:QName ) as xs:QName?
{
	common:qname(   common:type_decl($type)/jxon:basetype ) 

};

declare function common:item_type( $type as xs:QName ) as xs:QName?
{
	common:qname(   common:type_decl($type)/jxon:itemtype ) 

};

declare function common:json_value( $type as xs:string , $wrap as xs:string ) as element(jxon:value)
{
	
	<jxon:value type="{$type}" wrap="{$wrap}" />

};

declare function common:json_wrap_type( $pattern as element( jxon:pattern )  , $type as xs:QName?) as xs:string 
{
	if( $pattern/jxon:value/@wrap ne 'schema' ) then
		$pattern/jxon:value/@wrap 
	else
	if( empty($type) ) then
		"none"
	else
	let $decl := common:type_decl( $type )
	return
		if( $decl/@variety eq 'atomic' ) then 'none'
		else
		if( $decl/@variety eq 'list' ) then 'array'
		else
		if( exists($decl/jxon:basetype ) ) then 
			common:json_wrap_type( $pattern , common:parent_type( $type ) )
		else
		if( exists( $decl/jxon:itemtype ) ) then
			common:json_wrap_type( $pattern , common:item_type($type ) )
		else
			'none'

};

declare function common:json_atomic_type( $pattern as element(jxon:pattern) , $type as xs:QName? ) as xs:string
{

	if( $pattern/jxon:value/@type ne 'schema' ) then
		$pattern/jxon:value/@type 

	else
	if( empty($type) ) then
		"string"
	else
	if( $type = 
	   ( xs:QName("xs:decimal") ,
	   	 xs:QName("xs:double"),
	     xs:QName("xs:integer" ) ,
		 xs:QName("xs:float") ) )
	 then
	 	"number"
	else 
	if( $type = xs:QName("xs:boolean") ) then 
		"boolean"
	else
	let
		$decl := common:type_decl($type) 
		return
		if( exists($decl/jxon:basetype ) ) then 
			common:json_atomic_type( $pattern , common:parent_type( $type ) )
		else
		if( exists( $decl/jxon:itemtype ) ) then
			common:json_atomic_type( $pattern , common:item_type($type ) )
		else
			'string'
};





declare function common:json_type( $pattern as element(jxon:pattern) , $type as xs:QName? ) as element(jxon:value)
{
	if( $pattern/jxon:value/@type ne 'schema' ) then
		$pattern/jxon:value

	else
	if( empty($type) ) then
		common:json_value("string" , "none")
	else
		common:json_value( common:json_atomic_type($pattern,$type) , common:json_wrap_type($pattern,$type) )


};




(: Get the JSON or overridden type for an element/attribute :)

declare function common:json_text_type( $e as element(),  $pattern as element(jxon:pattern) ) as element(jxon:value)
{
	if( empty($e/jxon:type ) ) then
		common:json_value("string" , "none" )
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
		<array>
			<xsl:for-each select="tokenize(.,' ')">
			{
				element { $type/@type/string() } 
				{
					<xsl:value-of select="."/>
				}
			}
			</xsl:for-each>
		</array>

	else
		element { $type/@type/string() } 
		{
			<xsl:value-of select="."/>
		}

	

};


(: Create an xpath name for a single name element 
   For names with no namespace use the localname
   For names with a namespace use ns:localname
   :)
declare function common:xpath_name( $name as element(jxon:name) ) as xs:string
{
	if( $name/@uri eq '' )  then 
		$name/@localname
	else
		concat( $common:nsmap//ns[@uri eq $name/@uri]/@prefix , ":" , $name/@localname )
	
};


(: Generate a match string for an element :)
declare function common:match_elem( $name as element() , $e as element() ) as xs:string
{

	fn:string-join( 
		for $n in ( $e/ancestor::*/jxon:name , $name) 
		return common:xpath_name($n) , "/" )


};

(: The Attribute name for use in match or select strings :)
declare function common:attr_name( $name as element(jxon:name) )
{
	concat( "@" , common:xpath_name($name)  )

};


(: Generate a match string for an attribute :)
declare function common:match_attr( $name as element() , $e as element() ) as xs:string
{
	fn:string-join( (

	for $parent in $e/ancestor::*/jxon:name
	return common:xpath_name($parent) , 
	
	common:attr_name($name) ) , "/" )


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
		concat("member[@name='" , $name , "']")
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
				then concat("/object/member[@name eq '" , $pattern/jxon:children/@name,"']/array/object/" )
			else
				"/object/"
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
		"/object/" )


};

declare function common:element_by_ref( $ref as element(jxon:name) ) as element(jxon:element )
{
	let $name := common:qname($ref)
	return
	$common:annotations/jxon:document/jxon:element[common:qname(jxon:name) eq $name ]



};

(: From http://fgeorges.blogspot.com/2006/08/add-namespace-node-to-element-in.html :)

declare function common:add-ns-nodes(
    $elem   as element(),
    $prefix as xs:string,
    $ns-uri as xs:string
  ) as element()
{

  element { QName($ns-uri, concat($prefix, ":x")) }{ $elem }/*
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