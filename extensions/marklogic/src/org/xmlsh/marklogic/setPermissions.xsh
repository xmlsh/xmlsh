# set permissions
_optstr="x=execute:+,r=read:+,u=update:+,i=insert:+,R=recurse"
_opts=$<(xgetopts -i "c=connect:,t=text" -s -o $_optstr -- "$@")
_popts=$<(xgetopts -a -p "c=connect:,t=text" -ps  -i $_optstr -- "$@")
shift $?

_hasr=<[ $_opts//option[@name="R"] ]>

# Construct access list
_read=<[ for $u in $_opts//option[@name="r"]/value/string() 	return concat( "xdmp:permission('" , $u , "', 'read')" )]>
_write=<[ for $u in $_opts//option[@name="i"]/value/string() 	return concat( "xdmp:permission('" , $u , "', 'insert')" )]>
_update=<[ for $u in $_opts//option[@name="u"]/value/string() 	return concat( "xdmp:permission('" , $u , "', 'update')" )]>
_execute=<[ for $u in $_opts//option[@name="x"]/value/string() 	return concat( "xdmp:permission('" , $u , "', 'execute')" )]>

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
	"xdmp:document-set-permissions( " ,
	if( $_hasr) then "local:all($uri)" else "$uri" ,
	",(",  fn:string-join( ( $_read , $_write , $_update , $_execute ) , "," ) , "))" ) 
]>



for uri ; do
	:query $_pots -q "$_query" -v uri $uri
done


