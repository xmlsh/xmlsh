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
rm $F


