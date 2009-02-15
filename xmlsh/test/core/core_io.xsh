# test core IO, redirect

if [ -z "$TMPDIR" -o ! -d "$TMPDIR" ] ; then
	echo Temp directory required TMPDIR: $TMPDIR
	exit 1;
fi

F=$TMPDIR/test.core_io

echo foo > $F
echo foo >> $F
cat $F
cat < $F
cat < "$F"
rm $F


# Output to dev null
echo foo > /dev/null || echo Cant output to /dev/null
F=notempty
read F < /dev/null

[ -z "$F" ] || echo read didnt succeed