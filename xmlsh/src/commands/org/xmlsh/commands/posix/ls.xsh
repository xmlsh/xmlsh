# ls command - only handle no-option case

xls "$@" | xquery -q '

declare function local:name( $root as xs:string , $path as xs:string ) as xs:string
{
	if( $root eq "" ) then $path else concat( $root , "/" ,  $path )

};

declare function local:flags( $f as element(file) ) as xs:string
{	
	concat( 
	if( $f/@type eq "dir" ) then "d" else "-" ,
		
	if( $f/@readable eq "true"  ) then 
		"r"
	else
		"-",
	if( $f/@writable eq "true"  ) then 
		"w"
	else
		"-",
	if( $f/@executable eq "true"  ) then 
		"x"
	else
		"-"
	)
};
declare function local:pad( $padLen as xs:integer )
{
	fn:string-join((for $i in 1 to $padLen return " "), "")
};

declare function local:long( $root as xs:string , $f as element(file) ) as xs:string
{	
	concat( local:flags($f) , " " , 
	local:pad( 10 - string-length( $f/@length ) ),
	$f/@length ,
	 " " , fn:replace( $f/@mtime,"T", " " ), " " , local:name($root,$f/@name) )
};

declare function local:ls( $root as xs:string , $f as element(file) )
{
	(
	if( exists($f/@type ) ) then local:long($root , $f) 
	else 
		local:name($root,$f/@name) ,
	for $child in $f/file 
	return 
		local:ls( local:name($root,$f/@name) , $child )
	)

};

for $f in /dir/file
return
	local:ls( "" , $f )
	
  
'