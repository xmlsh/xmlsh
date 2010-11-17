(:
 : Format the help text for a command
 :)

declare variable $lf := "&#xA;";

declare variable $prefix as xs:string external ;
declare variable $module as xs:string external ;

declare function local:name( $c )
{
	if( $prefix eq '' ) then 
		$c
	else
		concat( $prefix , ":" , $c )

};

declare function local:commands($root) as xs:string*
{
 for $c in $root//(command|function) 
   order by $c/@name
   return concat(local:name($c/@name),if( local-name($c) eq "function" ) then "()" else "" )
};

declare function local:pad( $s as xs:string? , $w as xs:integer )
{
	if( empty($s)) then "" else 
	let $p := $w - fn:string-length($s)
	return
		fn:string-join((
			$s,
			for $i in 1 to $p return " " ),"")


};


let $cs := local:commands(/),
    $nw := 3

return
fn:string-join((
   "Module: " , $module , $lf ,
   for $i in 0 to (count($cs) idiv $nw) 
   return ( "  ",
   fn:string-join(
   		for $n in 1 to $nw  
		return local:pad($cs[ $i*$nw + $n  ],20)
		, " " ),$lf )

   ,$lf),"")(: Stylus Studio meta-information - (c) 2004-2009. Progress Software Corporation. All rights reserved.

<metaInformation>
	<scenarios>
		<scenario default="yes" name="Scenario1" userelativepaths="yes" externalpreview="no" useresolver="yes" url="commands.xml" outputurl="" processortype="saxon" tcpport="0" profilemode="0" profiledepth="" profilelength="" urlprofilexml=""
		          commandline="" additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext="" host="" port="-2147483590" user="" password="" validateoutput="no"
		          validator="internal" customvalidator="">
			<parameterValue name="prefix" value="''"/>
			<parameterValue name="module" value="'Core Commands'"/>
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