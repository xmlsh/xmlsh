# core_vars.xsh
# test variables

if [ -z "$TMPDIR" -o ! -d "$TMPDIR" ] ; then
	echo Temp directory required TMPDIR: $TMPDIR
	exit 1;
fi





# turn off indentation for this test
set +indent


# clear just in case they were exported
unset A AA
unset I

# Test expended [] notation
A=(a b c)
I=2

echo "A[2] is" ${A[$I]} 

# Test empty variables expand to nothing
A=
B=test
C=

echo $A $B $C
set $A $B $C
echo $#
