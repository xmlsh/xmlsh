# core_vars.xsh
# test variables


# clear just in case they were exported
unset A AA

echo NO $A variable
echo NO $AA variable
echo NO ${A} variable
echo NO ${AA} variable

A=foo
AA=bar

echo $A variable
echo $AA variable
echo ${AA} variable

set A $A "$A"

if [ $3 != foo -o $3 != "foo" ] ; then
	echo $3 should be foo
fi


# Test $()

A=$(echo '<foo/>' | xcat)
echo $A 


# Test local variable setting for simple commands
unset A
A=B eval 'echo A is $A'
[ -z "$A" ] || echo A should not be set


exit 0