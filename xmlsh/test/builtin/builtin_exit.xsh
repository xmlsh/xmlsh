# Test exit
# If called with 0 arguments then test
# else exit with arg


if [ $# -eq 0 ] ; then 
	$0 1
	echo return value $?
	$0 2
	echo return value $?
	$0 3 
	echo return value $?
	$0 4
	echo return value $?
	$0 5
	echo return value $?
# Try explicit exit code
	$0 100
	if [ $? -ne 100 ] ; then 
		echo Invalid exit value expected 100
		exit 1;
	fi 
	exit 0
        echo Should never get here - if 
fi

echo Exit test $1
if [ $1 -eq 1 ] ; then 
	exit $1
elif [ $1 -eq 2 ] ; then 
	while true ; do
		exit 0
		echo Should not get here - while
	done
elif [ $1 -eq 3 ] ; then 
	until false ; do
		exit 0	
		echo Should not get here - until
	done
elif [ $1 -eq 4 ] ; then 
	exit 0 && echo should not get here - "&&"

elif [ $1 -eq 5 ] ; then 
	case foo in 
	bar ) echo Bad match ;;
	foo ) exit 0 ; echo should not get here - case ;;
	foo ) echo should not get here either - case ;;
	esac 
	echo Should not get here - case2

else 
	exit $1
        echo Should never get here - else
fi

# Tests of various loop constructs to exit




echo Really should never get here
exit 2