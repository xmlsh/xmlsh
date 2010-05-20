# set permissions
_optstr="x=execute:+,r=read:+,u=update:+,i=insert:+"
_opts=$<(xgetopts -i "c=connect:,t=text" -s -o $_optstr -- "$@")
_popts=$<(xgetopts -a -p "c=connect:,t=text" -ps  -i $_optstr -- "$@")
shift $?



# Construct access list
_read=<[ for $u in $_opts//option[@name="r"]/value/string() 	return concat( "xdmp:permission('" , $u , "', 'read')" )]>
_write=<[ for $u in $_opts//option[@name="i"]/value/string() 	return concat( "xdmp:permission('" , $u , "', 'insert')" )]>
_update=<[ for $u in $_opts//option[@name="u"]/value/string() 	return concat( "xdmp:permission('" , $u , "', 'update')" )]>
_execute=<[ for $u in $_opts//option[@name="x"]/value/string() 	return concat( "xdmp:permission('" , $u , "', 'execute')" )]>


_query=<[ concat(
    "declare variable $uri external ; ",
	"xdmp:document-set-permissions( $uri , 
	( " ,  fn:string-join( ( $_read , $_write , $_update , $_execute ) , "," ) , "))" ) 
]>

for uri ; do
	# echo {$_query}
	:query $_pots -q {$_query} -v uri $uri 
done
