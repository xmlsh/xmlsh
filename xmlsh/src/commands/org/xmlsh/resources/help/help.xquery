(:
 : Format the help text for a command
 :)

declare variable $command := ./@name/string();

declare variable $lf := "&#xA;";
declare variable $longest-option := fn:max( .//option/arg/string-length(.) );


declare function local:pad( $s as xs:string? , $w as xs:integer )
{
	if( empty($s)) then "" else 
	let $p := $w - fn:string-length($s)
	return
		fn:string-join((
			$s,
			for $i in 1 to $p return " " ),"")


};

declare function local:print( $es as node()* , $indent as xs:string  ) as xs:string*
{
	for $e in $es
	return 
	typeswitch( $e )
	case element(synopsis) return ( $command," - " , local:print($e/node(),$indent),$lf)
	case element(usage)	   return ( "Usage:" ,$lf, local:print($e/node() , "  "))
	case element(para)	   return ( $indent , fn:normalize-space($e/text()) , $lf )
	case element(options)  return ( $lf,"Options:", $lf , local:print( $e/* , " ") , $lf )
	case element(arg)      return  ( $indent , local:pad( string($e) , $longest-option + 1 )  )
	case element(option)   return  local:print( $e/* , " " )
	case element(command)  return local:print($e/* , $indent )
	case element(function)  return local:print($e/* , $indent )

	case text() 		   return fn:normalize-space($e)
   
	default	return ()
	

};


fn:string-join((local:print( ./node() ,"" ),$lf,"For more information see: " ,$lf, ./@url/string() ),"")
(: Stylus Studio meta-information - (c) 2004-2009. Progress Software Corporation. All rights reserved.

<metaInformation>
	<scenarios>
		<scenario default="yes" name="Scenario1" userelativepaths="yes" externalpreview="no" useresolver="yes" url="commands.xml" outputurl="" processortype="saxon" tcpport="0" profilemode="0" profiledepth="" profilelength="" urlprofilexml=""
		          commandline="" additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext="" host="" port="1759511456" user="" password="" validateoutput="no"
		          validator="internal" customvalidator="">
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