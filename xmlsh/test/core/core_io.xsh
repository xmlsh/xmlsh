# test core IO, redirect

if [ -z "$TMPDIR" -o ! -d "$TMPDIR" ] ; then
	echo Temp directory required TMPDIR: $TMPDIR
	exit 1;
fi

rm -f $TMPDIR/*.core_io

F=$TMPDIR/test.core_io

echo foo > $F
echo foo >> $F
cat $F
cat < $F
cat < $(echo $F)

# test wildcard expansion
cat < $TMPDIR/*.core_io

rm $F


# Output to dev null
echo foo > /dev/null || echo Cant output to /dev/null
F=notempty
read F < /dev/null

[ -z "$F" ] || echo read didnt succeed

#
# Test 1>&2 and 2>&1 
#
F=$TMPDIR/test.core_io

# change stdout to stderr
echo error 2>$F 1>&2
read E < $F
[ $E = "error" ] || echo Failed to redirect to stderr
rm $F

# change stdout to stderr and then stderr to stdout
( echo error2 1>&2 ) >$F 2>&1
read E < $F
[ $E = "error2" ] || echo Failed to redirect to stdout
rm $F

# Test append to stderr
echo -p error line1 2>$F
echo -p error line2 2>>$F

cat $F
rm $F


