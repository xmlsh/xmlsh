# test of builtin set command
# check for 0 args
if [ $# -ne 0 ] ; then 
	echo -p error invalid call with $# args
	exit 1
fi


# check setting 1 or many args
set arg1
[ $# -eq 1 ] || { echo Failed ; exit 1 ; }

set arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11
[ $# -eq 11 ] || { echo -p error  Failed ; exit 1 ; }


A=astring
B=<[<xml/>]>
# check that variable gets put into environment and output with 0-arg set

echo '<builtin_set>'
set  | xpath '//variable[@name="A"]'
set  | xpath '//variable[@name="B"]'
echo '</builtin_set>'
# Test set --
set -- arg1
[ $# -eq 1 ] || { echo -p error Failed ; exit 1 ; }

set --
[ $# -eq 0 ] || { echo -p error Failed ; exit 1 ; }

exit 0
