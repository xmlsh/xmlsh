# Test of shift operator

echo Initial args list is $# $*
set arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg8 arg10 arg11 arg12 arg13
while [ $# -gt 0 ] ; do
	echo $1 
	shift
done

# Test shift of empty list
shift
echo final args list is $# $*

# Test shift of variable
a=(foo bar 1 2 3 4 5)
shift a 2
[ ${#a} -eq 5 ] || echo Shift Failed

exit 0
