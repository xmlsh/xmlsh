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
	:query -q "xdmp:directory-properties('$1')/base-uri()"
fi