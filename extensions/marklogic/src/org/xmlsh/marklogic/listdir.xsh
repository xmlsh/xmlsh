# list
if [ $# -eq 0 ] ; then 
	:query -q <{{
declare namespace  prop="http://marklogic.com/xdmp/property";
for $d in 
xdmp:document-properties()//prop:directory/base-uri()
order by $d
return $d
	
}}>
else
	for dir in $* ; do
	:query -q <{{
declare variable $dir external;
for $d in 
	xdmp:directory-properties($dir)/base-uri()
order by $d
return $d
}}> -v dir $dir
done
fi