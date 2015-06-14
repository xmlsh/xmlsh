# try/catch/finally test 2
# Test throw within a script

if [ $# -gt 0 ] ; then
	echo in script arg count $#
	if [ $# -gt 1 ] ; then 
		shift 
		$0 "$@"
		exit 0;
	fi 
	echo throwing $1 
	throw $1
	echo Fail SNH - after throw
fi

try {
	$0 foo bar spam test
} catch E {
	echo Success caught $E
} finally {
	echo Success in finally
}


func_throws () 
{
	echo in fun_throws
	throw $1
	echo Fail - SNH after throw
}


try {
	echo calling throw function
	func_throws <[ <foo/> ]>
	echo Fail - SNH after throw
} catch X
{
	echo Success caught $X
}



