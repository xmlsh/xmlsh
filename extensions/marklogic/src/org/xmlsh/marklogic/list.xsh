# list
_opts=$<(xgetopts -i "c=connect:" -s -o "r=recurse" -- "$@")
_popts=$<(xgetopts -a -p "c=connect:" -ps -i "r=recurse" -- "$@")
shift $?

hasr=<[ exists($_opts//option[@name="r"]) ]>

if [ $hasr ] ; then 
	for dir in $* ; do
      :query $_popts -q <{{
		xquery version "1.0-ml";		
		declare variable $dir external;
		try {
			cts:uris( $dir , "document" )[fn:starts-with( .,$dir )]
		} catch( $e ) {
			for $d in xdmp:directory($dir,"infinity")/base-uri()
			order by $d
			return $d
		}
		
	}}> -v dir $dir
done

elif [ $# -eq 0 ] ; then  
	:query $_popts -q <{{
	xquery version "1.0-ml";
	try {
		cts:uris((),"document")
	} catch($e) {
		for $uri in collection()/base-uri() order by $uri return $uri
	}
}}>
else
	for dir in $* ; do
		:query $_popts -q <{{
xquery version "1.0-ml";
declare variable $dir external;
for $d in xdmp:directory($dir)/base-uri()
order by $d
return $d
}}> -v dir $dir
done

fi