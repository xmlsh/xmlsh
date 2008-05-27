# core_args.xsh
# test arguments
# call with > 1 args to run as a sub-test with $*


# man call
if [ $# -eq 0 ] ; then
	$0 10 a b c e f g h i j || {
		echo Failed args 10 test ;
		exit 1;
	}
	
	$0 20 a b c e f g h i j k l m n o p q r s t  || {
		echo Failed args 10 test ;
		exit 1;
	}
	echo success
	exit 0 
fi	 

if [ $1 -eq 10 ] ; then 
	if [ $# -ne 10 ] ; then
		echo wrong num of args
		exit 1;
	fi
	
	# check $10 syntax 
	[ $10 = j ] || exit 1
	shift 
	[ $9 = j ] || exit 1   
	exit 0
	
	
fi

if [ $1 -eq 20 ] ; then 
	if [ $# -ne 20 ] ; then
		echo wrong num of args
		exit 1;
	fi
	
	[ $20 = t ] || { echo Arg 20 isnt t ; exit 1 ; }
	shift 2 
	[ $18 = t ] || { echo Arg 18 isnt t ; exit 1 ; }
	
	exit 0;
	
fi

exit 0

		 