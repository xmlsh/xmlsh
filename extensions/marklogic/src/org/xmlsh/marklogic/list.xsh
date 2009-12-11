# list
if [ $# -eq 0 ] ; then 
	:query -q 'collection()/base-uri()'
elif [ "$1" = "-r" ] ; then 
 	shift ;
	for dir in $* ; do
		:query -q <{{
declare variable $dir external;
xdmp:directory($dir,"infinity")/base-uri()
}}> -v dir $dir
done

else
	for dir in $* ; do
		:query -q <{{
declare variable $dir external;
xdmp:directory($dir)/base-uri()
}}> -v dir $dir
done

fi