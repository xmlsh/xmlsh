# core_vars.xsh
# test variables

if [ -z "$TMPDIR" -o ! -d "$TMPDIR" ] ; then
	echo Temp directory required TMPDIR: $TMPDIR
	exit 1;
fi



# clear just in case they were exported
unset A AA

echo NO $A variable
echo NO $AA variable
echo NO ${A} variable
echo NO ${AA} variable

A=foo
AA=bar

echo $A variable
echo $AA variable
echo ${AA} variable

set A $A "$A"

if [ $3 != foo -o $3 != "foo" ] ; then
	echo $3 should be foo
fi


# Test $()
A=$(echo '<foo/>' | xcat)
echo $A 


# Test $(<)
F=$TMPDIR/test_core_vars.txt
echo foo bar > $F
echo $(<$F)
echo spam bletch >> $F
A=$(<$F)
echo $A




# Test local variable setting for simple commands
unset A
A=B eval 'echo A is $A'
[ -z "$A" ] || echo A should not be set


exit 0