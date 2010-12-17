module namespace common="http://www.xmlsh.org/jsonxml/common" ;

declare function common:match_elem( $name as element() , $path as element(path)? ) as xs:string
{
	fn:string-join( ($path/name/@localname , $name/@localname) , "/" )


};


declare function common:match_attr( $name as element() , $path as element(path)? ) as xs:string
{
	fn:string-join( ($path/name/@localname , concat("@" , $name/@localname)) , "/" )


};


declare function common:member_name( $name  ) as xs:string  ?
{
	if( exists( $name) and $name/@localname ne '' ) then 
		concat("MEMBER[@name='" , $name/@localname , "']")
	else 
		()
};

declare function common:match_json( $name as element()? , $path as element(path)? ) as xs:string
{
	fn:string-join( (
		for $n in $path/name
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