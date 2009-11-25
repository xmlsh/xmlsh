# list
if [ $# -eq 0 ] ; then 
	:query -q 'collection()/base-uri()'
else
	for dir in $* ; do
		:query -q <{{
declare variable $dir external;
xdmp:directory($dir)/base-uri()
}}> -v dir $dir
done
fi