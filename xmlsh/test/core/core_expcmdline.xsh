# Test expanding of command line args
. ../common
C=echo
$C loc() 
C="echo foo"
$C loc()
echo loc() next line should fail
"$C" loc()
echo loc() shold catch failure
"$C" loc || echo Success 
