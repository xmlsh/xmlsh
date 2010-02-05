# Test require command
# First implemented in version 1.0.1

echo entering require $*
# If called recursively then fail conditionally and explictily

if [ $# -gt 0 ] ; then 
	require 99999 || echo Correctly failed require 99999
	require 99999
	echo SNH - require should have exited 
	exit 1
fi

require	# should succeed
require || echo Require with no args should have succeeded
require && echo Require succeeded with no args 
require 1.0.1 
require 1.0.1 && echo Require 1.0.1 succeeded
require 1.0.2 || echo Require 1.0.2 failed correctly

# should fail - trap it
try {
	$0 test 
} catch  e 
{ 
	echo Correctly caught failed require in sub 
	echo $e 
}
