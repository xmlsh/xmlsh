# Test of source (.) command

if [ -z "$_SOURCE" ] ; then
	echo top level invocation of $0
	_SOURCE=source
	. $0 
	exit 0
else
	echo sourced invocation of $0
	echo _SOURCE is _SOURCE
fi
unset _SOURCE
