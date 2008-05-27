# Test exit
# If called with 0 arguments then test
# else exit with arg

if [ $# -eq 0 ] ; then 
	$0 1
	echo return value $?
	$0 2
	echo return value $?
	exit 0
else 
	exit $1
fi
