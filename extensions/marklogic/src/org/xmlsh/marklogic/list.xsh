# list
if [ $# -eq 0 ] ; then 
	:query -q 'collection()/base-uri()'
elif [ "$1" = "-r" ] ; then 
 	shift ;
	for dir in $* ; do
		:query -q <{{
declare variable $dir external;
for $d in xdmp:directory($dir,"infinity")/base-uri()
order by $d
return $d

}}> -v dir $dir
done

else
	for dir in $* ; do
		:query -q <{{
declare variable $dir external;
for $d in xdmp:directory($dir)/base-uri()
order by $d
return $d
}}> -v dir $dir
done

fi