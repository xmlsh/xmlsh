# try/catch/finally test 2
# Test throw within a script

if [ $# -gt 0 ] ; then
	echo In sub-script 
	throw $1
	echo Fail SNH - after throw
fi

try {
	$0 test
} catch E {
	echo Successs got $E
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



