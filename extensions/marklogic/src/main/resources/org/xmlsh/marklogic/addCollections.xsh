# set permissions
_optstr="uri:+,collection:+,R=recurse"
_opts=$<(xgetopts -i "c=connect:,t=text" -s -o $_optstr -- "$@")
_popts=$<(xgetopts -a -p "c=connect:,t=text" -ps  -i $_optstr -- "$@")
shift $?

_hasr=<[ $_opts//option[@name="R"] ]>

# Construct access list
_uris=<[ $_opts//option[@name="uri"]/value/string() ]>
_cols=<[  string-join( 
   concat('"' , $_opts//option[@name="collection"]/value/string() '"' ) , ',' ) 
]>

_query=<[ concat(
    'xquery version "1.0-ml" ; ',
    "declare variable $uri external ; ",
    
    "declare function local:all( $uri as xs:string? ) as xs:string* ",
    "{ ",
    "	$uri ,  ",
    "	xdmp:directory( $uri )/base-uri(),",
    "	for $d in  xdmp:directory-properties($uri)//prop:directory/base-uri() ",
    "    	return local:all( $d ) ",
	"}; ",
	"xdmp:document-add-collections( " ,
	if( $_hasr) then "local:all($uri)" else "$uri" ,
	",(",  fn:string-join( ( $_read , $_write , $_update , $_execute ) , "," ) , "))" ) 
]>



for uri ; do
	:query $_pots -q "$_query" -v uri $uri
done


