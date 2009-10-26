# throw on error
# Test -e across scripts


if [ $# -gt 0 ] ; then
	echo in script arg count $#
	if [ $# -gt 1 ] ; then 
		shift 
		$0 "$@"
		exit 0;
	fi 
	[ $1 = "ignore" ] || echo Next command should fail 
	false
	
	[ $1 = "ignore" ] && exit 0 
	echo Fail SNH - after throw

fi

set -e 
try {
	
	$0 foo bar spam test
} catch E {
	echo Success caught $E
} 

# Test if calling script with condition doesnt throw
try {
	
	! $0 ignore
	echo Success 
} catch E {
	echo Fail - should not have caught error
} 