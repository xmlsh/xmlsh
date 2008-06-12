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



exit 0