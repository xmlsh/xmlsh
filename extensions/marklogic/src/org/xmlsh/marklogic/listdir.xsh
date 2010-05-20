# list


_opts=$<(xgetopts -a -p "c=connect:" -ps -- "$@")
shift $?


declare namespace  prop="http://marklogic.com/xdmp/property";
if [ $# -eq 0 ] ; then 
	:query $_opts -q <{{
for $d in 
xdmp:document-properties()//prop:directory/base-uri()
order by $d
return $d
	
}}>
else
	for dir in $* ; do
	:query $_opts -q <{{
xquery version "1.0-ml";	
declare variable $dir external;
for $d in 
	xdmp:directory-properties($dir)//prop:directory/base-uri()
order by $d
return $d
}}> -v dir $dir
done
fi