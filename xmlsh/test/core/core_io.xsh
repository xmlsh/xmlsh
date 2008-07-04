# test core IO, redirect

if [ -z "$_TEMP" -o ! -d "$_TEMP" ] ; then
	echo Temp directory required _TEMP : $_TEMP
	exit 1;
fi

F=$_TEMP/test.core_io

echo foo > $F
cat $F
cat < $F
rm $F


