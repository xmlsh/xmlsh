# Test of source (.) command

if [ -z "$_SOURCE" ] ; then
	echo top level invocation of $(xfile -b $0)
	_SOURCE=source
	. $0 
	echo _VAR is $_VAR 	
	unset _SOURCE
	unset _VAR
	
	# Try using source command instead of .
	_SOURCE=source
	source $0 
	echo _VAR is $_VAR 	
	unset _SOURCE
	unset _VAR
else
	echo sourced invocation of $(xfile -b $0)
	echo _SOURCE is $_SOURCE
	_VAR=var
fi
